package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Predicate;

public final class BlockEspTracker {
    private static final int MAX_CHUNKS_PER_TICK = 4;

    private final HuzzConfigManager configManager;
    private final Mode mode;

    private final Map<Long, Map<Long, CachedBlock>> trackedBlocksByChunk = new HashMap<>();
    private final Deque<Long> pendingChunkScans = new ArrayDeque<>();

    private volatile List<EspBlock> highlightedBlocks = List.of();
    private List<String> lastTargets = List.of();
    private boolean indexedLoadedChunks;
    private long indexedWorldId = Long.MIN_VALUE;

    public BlockEspTracker(HuzzConfigManager configManager, Mode mode) {
        this.configManager = configManager;
        this.mode = mode;
    }

    public List<EspBlock> getHighlightedBlocks() {
        return highlightedBlocks;
    }

    public void clear() {
        trackedBlocksByChunk.clear();
        pendingChunkScans.clear();
        highlightedBlocks = List.of();
        lastTargets = List.of();
        indexedLoadedChunks = false;
        indexedWorldId = Long.MIN_VALUE;
    }

    public void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            if (!highlightedBlocks.isEmpty() || !trackedBlocksByChunk.isEmpty() || indexedLoadedChunks) {
                clear();
            }
            return;
        }

        HuzzConfig config = configManager.getConfig();
        if (!isEnabled(config)) {
            if (!highlightedBlocks.isEmpty() || !trackedBlocksByChunk.isEmpty() || indexedLoadedChunks) {
                clear();
            }
            return;
        }

        if (!syncTargets(config)) {
            highlightedBlocks = List.of();
            return;
        }

        if (indexedWorldId != client.world.getRegistryKey().getValue().hashCode()) {
            trackedBlocksByChunk.clear();
            pendingChunkScans.clear();
            highlightedBlocks = List.of();
            indexedLoadedChunks = false;
            indexedWorldId = client.world.getRegistryKey().getValue().hashCode();
        }

        pruneUnloadedChunks(client);
        queueLoadedChunksOnce(client);
        processPendingChunkScans(client, config);
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

        queueChunkScan(packet.getChunkX(), packet.getChunkZ());
        processPendingChunkScans(client, config);
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
        boolean tracers = mode == Mode.BLOCK ? config.isBlockEspTracers() : config.isStorageEspTracers();
        double playerX = client.player.getX();
        double playerY = client.player.getEyeY();
        double playerZ = client.player.getZ();
        List<CachedBlock> sorted = new ArrayList<>();

        for (Map<Long, CachedBlock> chunkMatches : trackedBlocksByChunk.values()) {
            for (CachedBlock cachedBlock : chunkMatches.values()) {
                double distanceSquared = distanceSquared(cachedBlock.blockPos(), playerX, playerY, playerZ);
                sorted.add(cachedBlock.withDistance(distanceSquared));
            }
        }

        if (sorted.isEmpty()) {
            return List.of();
        }

        sorted.sort(Comparator.comparingDouble(CachedBlock::distanceSquared));

        List<EspBlock> results = new ArrayList<>(sorted.size());
        for (int index = 0; index < sorted.size(); index++) {
            CachedBlock cachedBlock = sorted.get(index);
            results.add(new EspBlock(
                cachedBlock.blockPos(),
                cachedBlock.blockId(),
                cachedBlock.colorRgb(),
                tracers,
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

    private void processPendingChunkScans(MinecraftClient client, HuzzConfig config) {
        if (client.world == null) {
            return;
        }

        ClientChunkManager chunkManager = client.world.getChunkManager();
        int processed = 0;
        while (processed < MAX_CHUNKS_PER_TICK && !pendingChunkScans.isEmpty()) {
            long chunkKey = pendingChunkScans.removeFirst();
            int chunkX = unpackChunkX(chunkKey);
            int chunkZ = unpackChunkZ(chunkKey);
            WorldChunk chunk = chunkManager.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
            if (chunk == null) {
                trackedBlocksByChunk.remove(chunkKey);
                continue;
            }

            Map<Long, CachedBlock> chunkMatches = scanChunk(chunk, targets(config));
            if (chunkMatches.isEmpty()) {
                trackedBlocksByChunk.remove(chunkKey);
            } else {
                trackedBlocksByChunk.put(chunkKey, chunkMatches);
            }
            processed++;
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

    private void queueLoadedChunksOnce(MinecraftClient client) {
        if (indexedLoadedChunks || client.world == null || client.player == null) {
            return;
        }

        int viewDistance = client.options.getViewDistance().getValue();
        int centerChunkX = client.player.getBlockX() >> 4;
        int centerChunkZ = client.player.getBlockZ() >> 4;
        ClientChunkManager chunkManager = client.world.getChunkManager();
        for (int chunkX = centerChunkX - viewDistance; chunkX <= centerChunkX + viewDistance; chunkX++) {
            for (int chunkZ = centerChunkZ - viewDistance; chunkZ <= centerChunkZ + viewDistance; chunkZ++) {
                if (chunkManager.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false) != null) {
                    queueChunkScan(chunkX, chunkZ);
                }
            }
        }
        indexedLoadedChunks = true;
    }

    private void queueChunkScan(int chunkX, int chunkZ) {
        long chunkKey = chunkKey(chunkX, chunkZ);
        if (!pendingChunkScans.contains(chunkKey)) {
            pendingChunkScans.addLast(chunkKey);
        }
    }

    private void pruneUnloadedChunks(MinecraftClient client) {
        ClientChunkManager chunkManager = client.world.getChunkManager();
        trackedBlocksByChunk.entrySet().removeIf(entry -> {
            int chunkX = unpackChunkX(entry.getKey());
            int chunkZ = unpackChunkZ(entry.getKey());
            return chunkManager.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false) == null;
        });
        pendingChunkScans.removeIf(chunkKey -> {
            int chunkX = unpackChunkX(chunkKey);
            int chunkZ = unpackChunkZ(chunkKey);
            return chunkManager.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false) == null;
        });
    }

    private boolean syncTargets(HuzzConfig config) {
        List<String> targets = targets(config);
        if (!targets.equals(lastTargets)) {
            trackedBlocksByChunk.clear();
            pendingChunkScans.clear();
            highlightedBlocks = List.of();
            indexedLoadedChunks = false;
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

    private static long chunkKey(int chunkX, int chunkZ) {
        return ChunkPos.toLong(chunkX, chunkZ);
    }

    private static int unpackChunkX(long chunkKey) {
        return ChunkPos.getPackedX(chunkKey);
    }

    private static int unpackChunkZ(long chunkKey) {
        return ChunkPos.getPackedZ(chunkKey);
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

    public static boolean matchesTarget(Block block, List<String> blockIds) {
        return matcherOf(blockIds).matches(block);
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
