package dev.david.huzzclient.detector;

import dev.david.huzzclient.HuzzClient;
import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import dev.david.huzzclient.mixin.ChunkDeltaUpdateS2CPacketAccessor;
import dev.david.huzzclient.render.UltimateChunkToast;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkSectionPos;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.ChunkPos;

public final class ChunkPacketTracker {
    private static final long MIN_ACTIVITY_WINDOW_MS = 5000L;
    private static final int MAX_MARKED_DISTANCE_CHUNKS = 34;

    private final HuzzConfigManager configManager;
    private final Long2ObjectMap<TrackedChunkActivity> trackedChunks = new Long2ObjectOpenHashMap<>();
    private final List<TrackedChunkActivity> markedActivities = new ArrayList<>();

    private volatile List<MarkedChunk> markedChunks = List.of();
    private long lastRescanAt;
    private boolean markedSnapshotDirty;

    public ChunkPacketTracker(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public List<MarkedChunk> getMarkedChunks() {
        return markedChunks;
    }

    public void clear() {
        trackedChunks.clear();
        markedActivities.clear();
        markedChunks = List.of();
        lastRescanAt = 0L;
        markedSnapshotDirty = false;
    }

    public void recordChunkDelta(ChunkDeltaUpdateS2CPacket packet) {
        if (!configManager.getConfig().isChunkFinderEnabled()) {
            return;
        }

        ChunkSectionPos sectionPos = ((ChunkDeltaUpdateS2CPacketAccessor) packet).huzzclient$getSectionPos();
        record(sectionPos.getSectionX(), sectionPos.getSectionZ(), TrackedChunkActivity::recordDelta);
    }

    public void recordChunkData(ChunkDataS2CPacket packet) {
        if (!configManager.getConfig().isChunkFinderEnabled()) {
            return;
        }

        record(packet.getChunkX(), packet.getChunkZ(), TrackedChunkActivity::recordChunkData);
    }

    public void recordLightUpdate(LightUpdateS2CPacket packet) {
        if (!configManager.getConfig().isChunkFinderEnabled()) {
            return;
        }

        record(packet.getChunkX(), packet.getChunkZ(), TrackedChunkActivity::recordLight);
    }

    public void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            if (!trackedChunks.isEmpty()) {
                clear();
            }
            return;
        }

        long now = Util.getMeasuringTimeMs();
        HuzzConfig config = configManager.getConfig();
        if (!config.isChunkFinderEnabled()) {
            if (!trackedChunks.isEmpty()) {
                clear();
            }
            return;
        }

        if (now - lastRescanAt < config.getRescanRateMs()) {
            return;
        }

        lastRescanAt = now;
        rescan(client, now, client.player.getChunkPos(), config);
    }

    private void rescan(MinecraftClient client, long now, ChunkPos playerChunkPos, HuzzConfig config) {
        long activityWindowMs = Math.max(MIN_ACTIVITY_WINDOW_MS, config.getRescanRateMs() * 4L);
        long cutoff = now - activityWindowMs;
        int threshold = config.getDeltaPacketThreshold();
        ObjectIterator<Long2ObjectMap.Entry<TrackedChunkActivity>> iterator = trackedChunks.long2ObjectEntrySet().iterator();

        while (iterator.hasNext()) {
            TrackedChunkActivity activity = iterator.next().getValue();
            activity.prune(cutoff);

            if (isOutOfRange(activity, playerChunkPos)) {
                removeMarkedActivity(activity);
                iterator.remove();
                continue;
            }

            if (!activity.isMarked()) {
                int suspicionScore = calculateSuspicionScore(activity, threshold);
                if (isSuspicious(activity, threshold)) {
                    activity.mark(now, suspicionScore);
                    markedActivities.add(0, activity);
                    markedSnapshotDirty = true;
                    client.getToastManager().add(UltimateChunkToast.chunkFinder(activity.toMarkedChunk()));
                }
            }

            if (activity.shouldDiscard()) {
                removeMarkedActivity(activity);
                iterator.remove();
            }
        }

        if (markedSnapshotDirty) {
            rebuildMarkedSnapshot();
        }
    }

    private boolean isOutOfRange(TrackedChunkActivity activity, ChunkPos playerChunkPos) {
        int dx = Math.abs(activity.getChunkX() - playerChunkPos.x);
        int dz = Math.abs(activity.getChunkZ() - playerChunkPos.z);
        return Math.max(dx, dz) > MAX_MARKED_DISTANCE_CHUNKS;
    }

    private boolean isSuspicious(TrackedChunkActivity activity, int threshold) {
        int deltaCount = activity.getDeltaCount();
        int lightCount = activity.getLightCount();
        int chunkDataCount = activity.getChunkDataCount();

        if (deltaCount >= threshold) {
            return true;
        }

        if (deltaCount >= Math.max(1, threshold - 1) && lightCount >= 2) {
            return true;
        }

        if (deltaCount >= Math.max(1, threshold - 2) && lightCount >= 1 && chunkDataCount >= 1) {
            return true;
        }

        return deltaCount >= Math.max(1, threshold / 2) && lightCount >= 5;
    }

    private int calculateSuspicionScore(TrackedChunkActivity activity, int threshold) {
        int score = activity.getDeltaCount() * 15;
        score += activity.getLightCount() * 9;
        score += activity.getChunkDataCount() * 18;

        if (activity.getDeltaCount() >= threshold) {
            score += 24;
        }

        return score;
    }

    private void record(int chunkX, int chunkZ, ActivityRecorder recorder) {
        long now = Util.getMeasuringTimeMs();
        long chunkKey = ChunkPos.toLong(chunkX, chunkZ);
        TrackedChunkActivity activity = trackedChunks.get(chunkKey);
        if (activity == null) {
            activity = new TrackedChunkActivity(chunkX, chunkZ);
            trackedChunks.put(chunkKey, activity);
        }

        recorder.record(activity, now);
        if (activity.isMarked()) {
            markedSnapshotDirty = true;
        }
    }

    private void removeMarkedActivity(TrackedChunkActivity activity) {
        if (!activity.isMarked()) {
            return;
        }

        if (markedActivities.remove(activity)) {
            markedSnapshotDirty = true;
        }
    }

    private void rebuildMarkedSnapshot() {
        if (markedActivities.isEmpty()) {
            markedChunks = List.of();
            markedSnapshotDirty = false;
            return;
        }

        List<MarkedChunk> updatedMarkedChunks = new ArrayList<>(markedActivities.size());
        for (TrackedChunkActivity activity : markedActivities) {
            updatedMarkedChunks.add(activity.toMarkedChunk());
        }

        markedChunks = List.copyOf(updatedMarkedChunks);
        markedSnapshotDirty = false;
    }

    @FunctionalInterface
    private interface ActivityRecorder {
        void record(TrackedChunkActivity activity, long now);
    }

    public record MarkedChunk(
        int chunkX,
        int chunkZ,
        int deltaCount,
        int lightCount,
        int chunkDataCount,
        int suspicionScore,
        long markedAt,
        long lastSeenAt
    ) {
    }
}
