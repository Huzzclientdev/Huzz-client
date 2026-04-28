package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PlayerChunkLoadTracker {
    private final HuzzConfigManager configManager;
    private final Map<Long, Long> firstSeenMissingAt = new HashMap<>();
    private final Map<Long, LoadedChunk> loadedChunks = new HashMap<>();
    private long indexedWorldId = Long.MIN_VALUE;

    public PlayerChunkLoadTracker(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public List<LoadedChunk> getLoadedChunks() {
        return loadedChunks.values().stream()
            .sorted(Comparator.comparingInt(LoadedChunk::chunkX).thenComparingInt(LoadedChunk::chunkZ))
            .toList();
    }

    public void clear() {
        firstSeenMissingAt.clear();
        loadedChunks.clear();
        indexedWorldId = Long.MIN_VALUE;
    }

    public void tick(MinecraftClient client) {
        HuzzConfig config = configManager.getConfig();
        if (!config.isPlayerChunkFinderEnabled() || client.world == null || client.player == null) {
            if (!firstSeenMissingAt.isEmpty() || !loadedChunks.isEmpty()) {
                clear();
            }
            return;
        }

        long worldId = client.world.getRegistryKey().getValue().hashCode();
        if (indexedWorldId != worldId) {
            clear();
            indexedWorldId = worldId;
        }

        int viewDistance = client.options.getViewDistance().getValue();
        int centerX = client.player.getBlockX() >> 4;
        int centerZ = client.player.getBlockZ() >> 4;
        ClientChunkManager chunkManager = client.world.getChunkManager();
        long now = Util.getMeasuringTimeMs();

        for (int chunkX = centerX - viewDistance; chunkX <= centerX + viewDistance; chunkX++) {
            for (int chunkZ = centerZ - viewDistance; chunkZ <= centerZ + viewDistance; chunkZ++) {
                long key = ChunkPos.toLong(chunkX, chunkZ);
                if (chunkManager.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false) == null) {
                    firstSeenMissingAt.putIfAbsent(key, now);
                }
            }
        }

        firstSeenMissingAt.keySet().removeIf(key -> isOutsideRenderDistance(key, centerX, centerZ, viewDistance));
        loadedChunks.keySet().removeIf(key -> isOutsideRenderDistance(key, centerX, centerZ, viewDistance));
    }

    public void recordChunkData(ChunkDataS2CPacket packet) {
        HuzzConfig config = configManager.getConfig();
        if (!config.isPlayerChunkFinderEnabled()) {
            return;
        }

        long now = Util.getMeasuringTimeMs();
        long key = ChunkPos.toLong(packet.getChunkX(), packet.getChunkZ());
        Long startedAt = firstSeenMissingAt.remove(key);
        long loadTimeMs = startedAt == null ? 0L : Math.max(0L, now - startedAt);
        loadedChunks.put(key, new LoadedChunk(packet.getChunkX(), packet.getChunkZ(), loadTimeMs, now));
    }

    public record LoadedChunk(int chunkX, int chunkZ, long loadTimeMs, long loadedAtMs) {
    }

    private static boolean isOutsideRenderDistance(long key, int centerX, int centerZ, int viewDistance) {
        int chunkX = (int) key;
        int chunkZ = (int) (key >> 32);
        return Math.abs(chunkX - centerX) > viewDistance || Math.abs(chunkZ - centerZ) > viewDistance;
    }
}
