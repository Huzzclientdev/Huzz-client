package dev.david.huzzclient.feature;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Predicate;

final class WorldBlockScanner {
    private WorldBlockScanner() {
    }

    static List<FoundBlock> scanBlocks(MinecraftClient client, List<String> configuredTargets, int range, boolean storageOnly, int maxResults) {
        if (maxResults <= 0) {
            return List.of();
        }

        PriorityQueue<FoundBlock> nearestResults = new PriorityQueue<>(maxResults, Comparator.comparingDouble(FoundBlock::distanceSquared).reversed());
        scanMatchingBlocks(client, configuredTargets, range, storageOnly, (blockPos, block, distanceSquared) ->
            offerNearest(nearestResults, createEntry(blockPos, block, distanceSquared), maxResults)
        );

        List<FoundBlock> results = new ArrayList<>(nearestResults);
        results.sort((left, right) -> Double.compare(left.distanceSquared(), right.distanceSquared()));
        return List.copyOf(results);
    }

    static int countBlocks(MinecraftClient client, List<String> configuredTargets, int range, boolean storageOnly) {
        int[] count = new int[1];
        scanMatchingBlocks(client, configuredTargets, range, storageOnly, (blockPos, block, distanceSquared) -> count[0]++);
        return count[0];
    }

    private static void scanMatchingBlocks(
        MinecraftClient client,
        List<String> configuredTargets,
        int range,
        boolean storageOnly,
        MatchConsumer consumer
    ) {
        if (client.world == null || client.player == null) {
            return;
        }

        TargetMatcher matcher = TargetMatcher.of(configuredTargets);
        if (matcher.isEmpty()) {
            return;
        }

        double rangeSquared = range * range;
        int playerChunkX = client.player.getChunkPos().x;
        int playerChunkZ = client.player.getChunkPos().z;
        int chunkRadius = (range + 15) / 16;
        double playerX = client.player.getX();
        double playerY = client.player.getEyeY();
        double playerZ = client.player.getZ();
        ClientChunkManager chunkManager = client.world.getChunkManager();

        for (int chunkX = playerChunkX - chunkRadius; chunkX <= playerChunkX + chunkRadius; chunkX++) {
            for (int chunkZ = playerChunkZ - chunkRadius; chunkZ <= playerChunkZ + chunkRadius; chunkZ++) {
                WorldChunk chunk = chunkManager.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
                if (chunk == null) {
                    continue;
                }

                if (storageOnly) {
                    scanStorageChunk(chunk, matcher, playerX, playerY, playerZ, rangeSquared, consumer);
                } else {
                    scanBlockChunk(chunk, matcher, playerX, playerY, playerZ, rangeSquared, consumer);
                }
            }
        }
    }

    private static void scanStorageChunk(
        WorldChunk chunk,
        TargetMatcher matcher,
        double playerX,
        double playerY,
        double playerZ,
        double rangeSquared,
        MatchConsumer consumer
    ) {
        for (BlockPos blockPos : chunk.getBlockEntityPositions()) {
            BlockState state = chunk.getBlockState(blockPos);
            if (!matcher.matches(state.getBlock()) || !isWithinRange(blockPos, playerX, playerY, playerZ, rangeSquared)) {
                continue;
            }

            consumer.accept(blockPos.toImmutable(), state.getBlock(), distanceSquared(blockPos, playerX, playerY, playerZ));
        }
    }

    private static void scanBlockChunk(
        WorldChunk chunk,
        TargetMatcher matcher,
        double playerX,
        double playerY,
        double playerZ,
        double rangeSquared,
        MatchConsumer consumer
    ) {
        Predicate<BlockState> targetPredicate = state -> matcher.matches(state.getBlock());
        ChunkSection[] sections = chunk.getSectionArray();
        int baseChunkX = chunk.getPos().x << 4;
        int baseChunkZ = chunk.getPos().z << 4;
        int bottomY = chunk.getBottomY();

        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            ChunkSection section = sections[sectionIndex];
            if (section == null || section.isEmpty() || !section.hasAny(targetPredicate)) {
                continue;
            }

            int sectionBaseY = bottomY + sectionIndex * 16;
            for (int localY = 0; localY < 16; localY++) {
                int worldY = sectionBaseY + localY;
                for (int localZ = 0; localZ < 16; localZ++) {
                    int worldZ = baseChunkZ + localZ;
                    for (int localX = 0; localX < 16; localX++) {
                        BlockState state = section.getBlockState(localX, localY, localZ);
                        if (!targetPredicate.test(state)) {
                            continue;
                        }

                        BlockPos blockPos = new BlockPos(baseChunkX + localX, worldY, worldZ);
                        if (!isWithinRange(blockPos, playerX, playerY, playerZ, rangeSquared)) {
                            continue;
                        }

                        consumer.accept(blockPos, state.getBlock(), distanceSquared(blockPos, playerX, playerY, playerZ));
                    }
                }
            }
        }
    }

    private static void offerNearest(PriorityQueue<FoundBlock> results, FoundBlock candidate, int maxResults) {
        if (results.size() < maxResults) {
            results.offer(candidate);
            return;
        }
        if (results.isEmpty() || candidate.distanceSquared() >= results.peek().distanceSquared()) {
            return;
        }
        results.poll();
        results.offer(candidate);
    }

    private static boolean isWithinRange(BlockPos blockPos, double playerX, double playerY, double playerZ, double rangeSquared) {
        return distanceSquared(blockPos, playerX, playerY, playerZ) <= rangeSquared;
    }

    private static double distanceSquared(BlockPos blockPos, double playerX, double playerY, double playerZ) {
        double dx = blockPos.getX() + 0.5D - playerX;
        double dy = blockPos.getY() + 0.5D - playerY;
        double dz = blockPos.getZ() + 0.5D - playerZ;
        return dx * dx + dy * dy + dz * dz;
    }

    private static FoundBlock createEntry(BlockPos blockPos, Block block, double distanceSquared) {
        return new FoundBlock(blockPos, displayId(block), distanceSquared);
    }

    private static String displayId(Block block) {
        var id = Registries.BLOCK.getId(block);
        return "minecraft".equals(id.getNamespace()) ? id.getPath() : id.toString();
    }

    record FoundBlock(BlockPos blockPos, String blockId, double distanceSquared) {
    }

    @FunctionalInterface
    private interface MatchConsumer {
        void accept(BlockPos blockPos, Block block, double distanceSquared);
    }

    private record TargetMatcher(Set<String> namespacedIds, Set<String> blockPaths, boolean shulkerFamily) {
        static TargetMatcher of(List<String> blockIds) {
            LinkedHashSet<String> namespacedIds = new LinkedHashSet<>();
            LinkedHashSet<String> blockPaths = new LinkedHashSet<>();
            boolean shulkerFamily = false;

            for (String blockId : blockIds) {
                if (blockId == null || blockId.isBlank()) {
                    continue;
                }

                String normalized = blockId.trim();
                if (normalized.contains(":")) {
                    namespacedIds.add(normalized);
                    String[] split = normalized.split(":", 2);
                    if ("minecraft".equals(split[0])) {
                        blockPaths.add(split[1]);
                    }
                    shulkerFamily |= split[1].endsWith("shulker_box");
                } else {
                    blockPaths.add(normalized);
                    shulkerFamily |= normalized.endsWith("shulker_box");
                }
            }

            return new TargetMatcher(Set.copyOf(namespacedIds), Set.copyOf(blockPaths), shulkerFamily);
        }

        boolean isEmpty() {
            return namespacedIds.isEmpty() && blockPaths.isEmpty();
        }

        boolean matches(Block block) {
            var id = Registries.BLOCK.getId(block);
            String fullId = id.toString();
            String path = id.getPath();
            return namespacedIds.contains(fullId)
                || blockPaths.contains(path)
                || (shulkerFamily && path.endsWith("shulker_box"));
        }
    }
}
