package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.registry.Registries;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Predicate;

public final class BlockEspTracker {
    private static final int MAX_RESULTS = 48;
    private static final int MAX_TRACERS = 16;

    private final HuzzConfigManager configManager;
    private final Mode mode;

    private final Map<Long, Map<Long, CachedBlock>> trackedBlocksByChunk = new HashMap<>();
    private volatile List<EspBlock> highlightedBlocks = List.of();
    private List<String> lastTargets = List.of();

    public BlockEspTracker(HuzzConfigManager configManager, Mode mode) {
        this.configManager = configManager;
        this.mode = mode;
    }

    public List<EspBlock> getHighlightedBlocks() {
        return highlightedBlocks;
    }

    public void clear() {
        trackedBlocksByChunk.clear();
        highlightedBlocks = List.of();
        lastTargets = List.of();
    }

    public void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            if (!highlightedBlocks.isEmpty() || !trackedBlocksByChunk.isEmpty()) {
                clear();
            }
            return;
        }

        HuzzConfig config = configManager.getConfig();
        if (!isEnabled(config)) {
            if (!highlightedBlocks.isEmpty() || !trackedBlocksByChunk.isEmpty()) {
                clear();
            }
            return;
        }

        if (!syncTargets(config)) {
            if (!highlightedBlocks.isEmpty()) {
                highlightedBlocks = List.of();
            }
            return;
        }

        pruneUnloadedChunks(client);
        highlightedBlocks = List.copyOf(collectVisibleBlocks(client, config));
    }

    public void recordChunkData(MinecraftClient client, ChunkDataS2CPacket packet) {
        if (client.world == null) {
            return;
        }

        HuzzConfig config = configManager.getConfig();
        if (!isEnabled(config) || !syncTargets(config)) {
            return;
        }

        ClientChunkManager chunkManager = client.world.getChunkManager();
        WorldChunk chunk = chunkManager.getChunk(packet.getChunkX(), packet.getChunkZ(), ChunkStatus.FULL, false);
        if (chunk == null) {
            return;
        }

        long chunkKey = chunkKey(packet.getChunkX(), packet.getChunkZ());
        Map<Long, CachedBlock> chunkMatches = scanChunk(chunk, targets(config));
        if (chunkMatches.isEmpty()) {
            trackedBlocksByChunk.remove(chunkKey);
        } else {
            trackedBlocksByChunk.put(chunkKey, chunkMatches);
        }
    }

    public void recordChunkDelta(ChunkDeltaUpdateS2CPacket packet) {
        HuzzConfig config = configManager.getConfig();
        if (!isEnabled(config) || !syncTargets(config)) {
            return;
        }

        packet.visitUpdates((blockPos, blockState) -> recordBlockState(config, blockPos.toImmutable(), blockState));
    }

    public void recordBlockUpdate(BlockUpdateS2CPacket packet) {
        HuzzConfig config = configManager.getConfig();
        if (!isEnabled(config) || !syncTargets(config)) {
            return;
        }

        recordBlockState(config, packet.getPos().toImmutable(), packet.getState());
    }

    private List<EspBlock> collectVisibleBlocks(MinecraftClient client, HuzzConfig config) {
        int range = mode == Mode.BLOCK ? config.getBlockEspRangeBlocks() : config.getStorageEspRangeBlocks();
        boolean tracers = mode == Mode.BLOCK ? config.isBlockEspTracers() : config.isStorageEspTracers();
        double rangeSquared = range * (double) range;
        double playerX = client.player.getX();
        double playerY = client.player.getEyeY();
        double playerZ = client.player.getZ();
        PriorityQueue<CachedBlock> nearestResults = new PriorityQueue<>(MAX_RESULTS, Comparator.comparingDouble(CachedBlock::distanceSquared).reversed());

        for (Map<Long, CachedBlock> chunkMatches : trackedBlocksByChunk.values()) {
            for (CachedBlock cachedBlock : chunkMatches.values()) {
                double distanceSquared = distanceSquared(cachedBlock.blockPos(), playerX, playerY, playerZ);
                if (distanceSquared > rangeSquared) {
                    continue;
                }

                offerNearest(nearestResults, cachedBlock.withDistance(distanceSquared), MAX_RESULTS);
            }
        }

        if (nearestResults.isEmpty()) {
            return List.of();
        }

        List<CachedBlock> sorted = new ArrayList<>(nearestResults);
        sorted.sort(Comparator.comparingDouble(CachedBlock::distanceSquared));

        List<EspBlock> results = new ArrayList<>(sorted.size());
        for (int index = 0; index < sorted.size(); index++) {
            CachedBlock cachedBlock = sorted.get(index);
            boolean drawTracer = tracers && index < MAX_TRACERS;
            results.add(new EspBlock(
                cachedBlock.blockPos(),
                cachedBlock.blockId(),
                cachedBlock.colorRgb(),
                drawTracer,
                cachedBlock.distanceSquared()
            ));
        }
        return results;
    }

    private void recordBlockState(HuzzConfig config, BlockPos blockPos, BlockState blockState) {
        long chunkKey = chunkKey(blockPos.getX() >> 4, blockPos.getZ() >> 4);
        Map<Long, CachedBlock> chunkMatches = trackedBlocksByChunk.computeIfAbsent(chunkKey, ignored -> new HashMap<>());
        long blockKey = blockPos.asLong();
        if (matcherOf(targets(config)).matches(blockState.getBlock())) {
            chunkMatches.put(blockKey, cachedBlock(blockPos, blockState.getBlock()));
            return;
        }

        chunkMatches.remove(blockKey);
        if (chunkMatches.isEmpty()) {
            trackedBlocksByChunk.remove(chunkKey);
        }
    }

    private Map<Long, CachedBlock> scanChunk(WorldChunk chunk, List<String> configuredTargets) {
        TargetMatcher matcher = matcherOf(configuredTargets);
        if (matcher.isEmpty()) {
            return Map.of();
        }

        Map<Long, CachedBlock> matches = new HashMap<>();
        if (mode == Mode.STORAGE) {
            for (BlockPos blockPos : chunk.getBlockEntityPositions()) {
                BlockState blockState = chunk.getBlockState(blockPos);
                if (matcher.matches(blockState.getBlock())) {
                    matches.put(blockPos.asLong(), cachedBlock(blockPos.toImmutable(), blockState.getBlock()));
                }
            }
            return matches;
        }

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
                        BlockState blockState = section.getBlockState(localX, localY, localZ);
                        if (!targetPredicate.test(blockState)) {
                            continue;
                        }

                        BlockPos blockPos = new BlockPos(baseChunkX + localX, worldY, worldZ);
                        matches.put(blockPos.asLong(), cachedBlock(blockPos, blockState.getBlock()));
                    }
                }
            }
        }

        return matches;
    }

    private void pruneUnloadedChunks(MinecraftClient client) {
        ClientChunkManager chunkManager = client.world.getChunkManager();
        trackedBlocksByChunk.entrySet().removeIf(entry -> {
            int chunkX = unpackChunkX(entry.getKey());
            int chunkZ = unpackChunkZ(entry.getKey());
            return chunkManager.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false) == null;
        });
    }

    private boolean syncTargets(HuzzConfig config) {
        List<String> targets = targets(config);
        if (!targets.equals(lastTargets)) {
            trackedBlocksByChunk.clear();
            lastTargets = targets;
        }
        return !targets.isEmpty();
    }

    private List<String> targets(HuzzConfig config) {
        return mode == Mode.BLOCK ? config.getBlockEspTargets() : config.getStorageEspTargets();
    }

    private boolean isEnabled(HuzzConfig config) {
        return mode == Mode.BLOCK ? config.isBlockEspEnabled() : config.isStorageEspEnabled();
    }

    private static void offerNearest(PriorityQueue<CachedBlock> results, CachedBlock candidate, int maxResults) {
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

    private static long chunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    private static int unpackChunkX(long chunkKey) {
        return (int) (chunkKey >> 32);
    }

    private static int unpackChunkZ(long chunkKey) {
        return (int) chunkKey;
    }

    private static double distanceSquared(BlockPos blockPos, double playerX, double playerY, double playerZ) {
        double dx = blockPos.getX() + 0.5D - playerX;
        double dy = blockPos.getY() + 0.5D - playerY;
        double dz = blockPos.getZ() + 0.5D - playerZ;
        return dx * dx + dy * dy + dz * dz;
    }

    private static CachedBlock cachedBlock(BlockPos blockPos, Block block) {
        String blockId = displayId(block);
        return new CachedBlock(blockPos, blockId, colorFor(blockId), 0.0D);
    }

    private static String displayId(Block block) {
        var id = Registries.BLOCK.getId(block);
        return "minecraft".equals(id.getNamespace()) ? id.getPath() : id.toString();
    }

    private static int colorFor(String blockId) {
        int hash = blockId.hashCode();
        float hue = ((hash >>> 1) & 1023) / 1023.0F;
        float saturation = 0.62F + (((hash >>> 11) & 0xFF) / 255.0F) * 0.25F;
        float brightness = 0.82F + (((hash >>> 19) & 0x7F) / 127.0F) * 0.16F;
        return Color.HSBtoRGB(hue, Math.min(0.92F, saturation), Math.min(0.98F, brightness));
    }

    private static TargetMatcher matcherOf(List<String> blockIds) {
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

    public enum Mode {
        BLOCK,
        STORAGE
    }

    public record EspBlock(BlockPos blockPos, String blockId, int colorRgb, boolean tracers, double distanceSquared) {
    }

    private record CachedBlock(BlockPos blockPos, String blockId, int colorRgb, double distanceSquared) {
        private CachedBlock withDistance(double newDistanceSquared) {
            return new CachedBlock(blockPos, blockId, colorRgb, newDistanceSquared);
        }
    }

    private record TargetMatcher(Set<String> namespacedIds, Set<String> blockPaths, boolean shulkerFamily) {
        private boolean isEmpty() {
            return namespacedIds.isEmpty() && blockPaths.isEmpty();
        }

        private boolean matches(Block block) {
            var id = Registries.BLOCK.getId(block);
            String fullId = id.toString();
            String path = id.getPath();
            return namespacedIds.contains(fullId)
                || blockPaths.contains(path)
                || (shulkerFamily && path.endsWith("shulker_box"));
        }
    }
}
