package dev.david.huzzclient.detector;

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
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;

import java.util.ArrayList;
import java.util.List;

public final class ObservedActivityTimeTracker {
    private static final long ACTIVE_GRACE_MS = 1500L;
    private static final long HEATMAP_VISIBLE_MS = 15000L;
    private static final long STALE_TRACKED_MS = 180000L;
    private static final int MIN_ACTIVITY_SCORE = 60;
    private static final int MAX_MARKED_DISTANCE_CHUNKS = 34;

    private final HuzzConfigManager configManager;
    private final Long2ObjectMap<TrackedObservedChunk> trackedChunks = new Long2ObjectOpenHashMap<>();
    private final List<TrackedObservedChunk> markedActivities = new ArrayList<>();

    private volatile List<ObservedChunk> markedChunks = List.of();
    private volatile List<HeatmapChunk> heatmapChunks = List.of();
    private long lastRescanAt;
    private boolean markedSnapshotDirty;

    public ObservedActivityTimeTracker(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public List<ObservedChunk> getMarkedChunks() {
        return markedChunks;
    }

    public List<HeatmapChunk> getHeatmapChunks() {
        return heatmapChunks;
    }

    public void clear() {
        trackedChunks.clear();
        markedActivities.clear();
        markedChunks = List.of();
        heatmapChunks = List.of();
        lastRescanAt = 0L;
        markedSnapshotDirty = false;
    }

    public void recordChunkDelta(ChunkDeltaUpdateS2CPacket packet) {
        if (!configManager.getConfig().isTimeDebugEnabled()) {
            return;
        }

        ChunkSectionPos sectionPos = ((ChunkDeltaUpdateS2CPacketAccessor) packet).huzzclient$getSectionPos();
        record(sectionPos.getSectionX(), sectionPos.getSectionZ(), PacketKind.DELTA);
    }

    public void recordChunkData(ChunkDataS2CPacket packet) {
        if (!configManager.getConfig().isTimeDebugEnabled()) {
            return;
        }

        record(packet.getChunkX(), packet.getChunkZ(), PacketKind.CHUNK_DATA);
    }

    public void recordLightUpdate(LightUpdateS2CPacket packet) {
        if (!configManager.getConfig().isTimeDebugEnabled()) {
            return;
        }

        record(packet.getChunkX(), packet.getChunkZ(), PacketKind.LIGHT);
    }

    public void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            if (!trackedChunks.isEmpty()) {
                clear();
            }
            return;
        }

        HuzzConfig config = configManager.getConfig();
        if (!config.isTimeDebugEnabled()) {
            if (!trackedChunks.isEmpty()) {
                clear();
            }
            return;
        }

        long now = Util.getMeasuringTimeMs();
        if (now - lastRescanAt < config.getRescanRateMs()) {
            return;
        }

        lastRescanAt = now;
        rescan(client, now, client.player.getChunkPos(), config);
    }

    private void rescan(MinecraftClient client, long now, ChunkPos playerChunkPos, HuzzConfig config) {
        boolean heatmapMode = config.getTimeDebugMode() == HuzzConfig.TimeDebugMode.HEATMAP;
        List<HeatmapChunk> updatedHeatmapChunks = heatmapMode ? new ArrayList<>() : null;
        ObjectIterator<Long2ObjectMap.Entry<TrackedObservedChunk>> iterator = trackedChunks.long2ObjectEntrySet().iterator();

        while (iterator.hasNext()) {
            TrackedObservedChunk activity = iterator.next().getValue();
            boolean integrated = activity.integrateUntil(now);

            if (isOutOfRange(activity, playerChunkPos)) {
                removeMarkedActivity(activity);
                iterator.remove();
                continue;
            }

            if (heatmapMode) {
                if (activity.shouldRenderHeatmap(now)) {
                    updatedHeatmapChunks.add(activity.toHeatmapChunk());
                }
            } else {
                if (!activity.isMarked() && shouldMark(activity, config.getTimeDebugMarkDelayMs())) {
                    activity.mark(now);
                    markedActivities.add(0, activity);
                    markedSnapshotDirty = true;
                    client.getToastManager().add(UltimateChunkToast.timeDebug(activity.toObservedChunk()));
                } else if (activity.isMarked() && integrated) {
                    markedSnapshotDirty = true;
                }
            }

            if (activity.shouldDiscard(now)) {
                removeMarkedActivity(activity);
                iterator.remove();
            }
        }

        heatmapChunks = heatmapMode ? List.copyOf(updatedHeatmapChunks) : List.of();

        if (markedSnapshotDirty) {
            rebuildMarkedSnapshot();
        }
    }

    private boolean isOutOfRange(TrackedObservedChunk activity, ChunkPos playerChunkPos) {
        int dx = Math.abs(activity.getChunkX() - playerChunkPos.x);
        int dz = Math.abs(activity.getChunkZ() - playerChunkPos.z);
        return Math.max(dx, dz) > MAX_MARKED_DISTANCE_CHUNKS;
    }

    private boolean shouldMark(TrackedObservedChunk activity, int delayMs) {
        return activity.getObservedActiveMs() >= delayMs
            && activity.getActivityScore() >= MIN_ACTIVITY_SCORE;
    }

    private void record(int chunkX, int chunkZ, PacketKind kind) {
        long now = Util.getMeasuringTimeMs();
        long chunkKey = ChunkPos.toLong(chunkX, chunkZ);
        TrackedObservedChunk activity = trackedChunks.get(chunkKey);
        if (activity == null) {
            activity = new TrackedObservedChunk(chunkX, chunkZ);
            trackedChunks.put(chunkKey, activity);
        }

        activity.record(kind, now);
        if (activity.isMarked()) {
            markedSnapshotDirty = true;
        }
    }

    private void removeMarkedActivity(TrackedObservedChunk activity) {
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

        List<ObservedChunk> updatedMarkedChunks = new ArrayList<>(markedActivities.size());
        for (TrackedObservedChunk activity : markedActivities) {
            updatedMarkedChunks.add(activity.toObservedChunk());
        }

        markedChunks = List.copyOf(updatedMarkedChunks);
        markedSnapshotDirty = false;
    }

    private enum PacketKind {
        DELTA,
        LIGHT,
        CHUNK_DATA
    }

    private static final class TrackedObservedChunk {
        private final int chunkX;
        private final int chunkZ;

        private long lastPacketTime;
        private long lastIntegratedTime;
        private long observedActiveMs;
        private long markedAt;
        private boolean marked;
        private int deltaCount;
        private int lightCount;
        private int chunkDataCount;
        private long responseIntervalTotalMs;
        private int responseSamples;

        private TrackedObservedChunk(int chunkX, int chunkZ) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }

        int getChunkX() {
            return chunkX;
        }

        int getChunkZ() {
            return chunkZ;
        }

        boolean isMarked() {
            return marked;
        }

        long getObservedActiveMs() {
            return observedActiveMs;
        }

        int getActivityScore() {
            return deltaCount * 6 + lightCount * 4 + chunkDataCount * 8;
        }

        long getAverageResponseMs() {
            return responseSamples == 0 ? ACTIVE_GRACE_MS : responseIntervalTotalMs / responseSamples;
        }

        void record(PacketKind kind, long now) {
            integrateUntil(now);
            if (lastPacketTime == 0L || now - lastPacketTime > ACTIVE_GRACE_MS) {
                lastIntegratedTime = now;
            } else {
                responseIntervalTotalMs += now - lastPacketTime;
                responseSamples++;
            }

            lastPacketTime = now;

            switch (kind) {
                case DELTA -> deltaCount++;
                case LIGHT -> lightCount++;
                case CHUNK_DATA -> chunkDataCount++;
            }
        }

        boolean integrateUntil(long now) {
            if (lastPacketTime == 0L) {
                return false;
            }

            long activeUntil = Math.min(now, lastPacketTime + ACTIVE_GRACE_MS);
            if (activeUntil <= lastIntegratedTime) {
                return false;
            }

            observedActiveMs += activeUntil - lastIntegratedTime;
            lastIntegratedTime = activeUntil;
            return true;
        }

        void mark(long now) {
            marked = true;
            markedAt = now;
        }

        boolean shouldDiscard(long now) {
            return !marked && now - lastPacketTime > STALE_TRACKED_MS;
        }

        boolean shouldRenderHeatmap(long now) {
            return now - lastPacketTime <= HEATMAP_VISIBLE_MS && getActivityScore() >= 8;
        }

        ObservedChunk toObservedChunk() {
            return new ObservedChunk(
                chunkX,
                chunkZ,
                observedActiveMs,
                deltaCount,
                lightCount,
                chunkDataCount,
                getActivityScore(),
                markedAt,
                lastPacketTime
            );
        }

        HeatmapChunk toHeatmapChunk() {
            return new HeatmapChunk(
                chunkX,
                chunkZ,
                observedActiveMs,
                getAverageResponseMs(),
                getActivityScore(),
                lastPacketTime
            );
        }
    }

    public record ObservedChunk(
        int chunkX,
        int chunkZ,
        long observedActiveMs,
        int deltaCount,
        int lightCount,
        int chunkDataCount,
        int activityScore,
        long markedAt,
        long lastSeenAt
    ) {
    }

    public record HeatmapChunk(
        int chunkX,
        int chunkZ,
        long observedActiveMs,
        long averageResponseMs,
        int activityScore,
        long lastSeenAt
    ) {
    }
}
