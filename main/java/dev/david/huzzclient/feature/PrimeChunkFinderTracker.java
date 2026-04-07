package dev.david.huzzclient.feature;

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

public final class PrimeChunkFinderTracker {
    private static final int PRIME_DELTA_THRESHOLD = 5;
    private static final long ACTIVE_GRACE_MS = 1500L;
    private static final long STALE_TRACKED_MS = 180000L;
    private static final int MIN_ACTIVITY_SCORE = 60;
    private static final int MAX_MARKED_DISTANCE_CHUNKS = 34;
    private static final int PRIME_MARK_RADIUS_CHUNKS = 3;

    private final HuzzConfigManager configManager;
    private final Long2ObjectMap<TrackedPrimeChunk> trackedChunks = new Long2ObjectOpenHashMap<>();
    private final List<TrackedPrimeChunk> markedActivities = new ArrayList<>();

    private volatile List<MarkedPrimeChunk> markedChunks = List.of();
    private long lastRescanAt;
    private boolean markedSnapshotDirty;

    public PrimeChunkFinderTracker(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public List<MarkedPrimeChunk> getMarkedChunks() {
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
        if (!configManager.getConfig().isPrimeChunkFinderEnabled()) {
            return;
        }

        ChunkSectionPos sectionPos = ((ChunkDeltaUpdateS2CPacketAccessor) packet).huzzclient$getSectionPos();
        record(sectionPos.getSectionX(), sectionPos.getSectionZ(), PacketKind.DELTA);
    }

    public void recordChunkData(ChunkDataS2CPacket packet) {
        if (!configManager.getConfig().isPrimeChunkFinderEnabled()) {
            return;
        }

        record(packet.getChunkX(), packet.getChunkZ(), PacketKind.CHUNK_DATA);
    }

    public void recordLightUpdate(LightUpdateS2CPacket packet) {
        if (!configManager.getConfig().isPrimeChunkFinderEnabled()) {
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
        if (!config.isPrimeChunkFinderEnabled()) {
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
        ObjectIterator<Long2ObjectMap.Entry<TrackedPrimeChunk>> iterator = trackedChunks.long2ObjectEntrySet().iterator();
        while (iterator.hasNext()) {
            TrackedPrimeChunk activity = iterator.next().getValue();
            activity.integrateUntil(now);

            if (isOutOfRange(activity, playerChunkPos)) {
                removeMarkedActivity(activity);
                iterator.remove();
                continue;
            }

            if (!activity.isMarked()
                && !hasNearbyMarkedChunk(activity)
                && shouldPrime(activity, config.getTimeDebugMarkDelayMs())) {
                activity.mark(now);
                markedActivities.add(0, activity);
                markedSnapshotDirty = true;
                client.getToastManager().add(UltimateChunkToast.primeChunk(activity.getChunkX(), activity.getChunkZ(), activity.getPrimeScore()));
            }

            if (activity.shouldDiscard(now)) {
                removeMarkedActivity(activity);
                iterator.remove();
            }
        }

        if (markedSnapshotDirty) {
            rebuildMarkedSnapshot();
        }
    }

    private boolean isOutOfRange(TrackedPrimeChunk activity, ChunkPos playerChunkPos) {
        int dx = Math.abs(activity.getChunkX() - playerChunkPos.x);
        int dz = Math.abs(activity.getChunkZ() - playerChunkPos.z);
        return Math.max(dx, dz) > MAX_MARKED_DISTANCE_CHUNKS;
    }

    private boolean shouldPrime(TrackedPrimeChunk activity, int delayMs) {
        return isSuspicious(activity)
            && activity.getObservedActiveMs() >= delayMs
            && activity.getActivityScore() >= MIN_ACTIVITY_SCORE;
    }

    private boolean hasNearbyMarkedChunk(TrackedPrimeChunk candidate) {
        for (TrackedPrimeChunk marked : markedActivities) {
            if (withinPrimeRadius(candidate.getChunkX(), candidate.getChunkZ(), marked.getChunkX(), marked.getChunkZ())) {
                return true;
            }
        }
        return false;
    }

    private static boolean withinPrimeRadius(int leftChunkX, int leftChunkZ, int rightChunkX, int rightChunkZ) {
        int dx = Math.abs(leftChunkX - rightChunkX);
        int dz = Math.abs(leftChunkZ - rightChunkZ);
        return Math.max(dx, dz) <= PRIME_MARK_RADIUS_CHUNKS;
    }

    private boolean isSuspicious(TrackedPrimeChunk activity) {
        int deltaCount = activity.getDeltaCount();
        int lightCount = activity.getLightCount();
        int chunkDataCount = activity.getChunkDataCount();

        if (deltaCount >= PRIME_DELTA_THRESHOLD) {
            return true;
        }
        if (deltaCount >= PRIME_DELTA_THRESHOLD - 1 && lightCount >= 2) {
            return true;
        }
        if (deltaCount >= PRIME_DELTA_THRESHOLD - 2 && lightCount >= 1 && chunkDataCount >= 1) {
            return true;
        }
        return deltaCount >= Math.max(1, PRIME_DELTA_THRESHOLD / 2) && lightCount >= 5;
    }

    private void record(int chunkX, int chunkZ, PacketKind kind) {
        long now = Util.getMeasuringTimeMs();
        long chunkKey = ChunkPos.toLong(chunkX, chunkZ);
        TrackedPrimeChunk activity = trackedChunks.get(chunkKey);
        if (activity == null) {
            activity = new TrackedPrimeChunk(chunkX, chunkZ);
            trackedChunks.put(chunkKey, activity);
        }

        activity.record(kind, now);
        if (activity.isMarked()) {
            markedSnapshotDirty = true;
        }
    }

    private void removeMarkedActivity(TrackedPrimeChunk activity) {
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

        List<MarkedPrimeChunk> updatedMarkedChunks = new ArrayList<>(markedActivities.size());
        for (TrackedPrimeChunk activity : markedActivities) {
            updatedMarkedChunks.add(activity.toMarkedChunk());
        }

        markedChunks = List.copyOf(updatedMarkedChunks);
        markedSnapshotDirty = false;
    }

    private enum PacketKind {
        DELTA,
        LIGHT,
        CHUNK_DATA
    }

    private static final class TrackedPrimeChunk {
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

        private TrackedPrimeChunk(int chunkX, int chunkZ) {
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

        int getDeltaCount() {
            return deltaCount;
        }

        int getLightCount() {
            return lightCount;
        }

        int getChunkDataCount() {
            return chunkDataCount;
        }

        int getActivityScore() {
            return deltaCount * 6 + lightCount * 4 + chunkDataCount * 8;
        }

        long getObservedActiveMs() {
            return observedActiveMs;
        }

        int getPrimeScore() {
            int score = deltaCount * 15 + lightCount * 9 + chunkDataCount * 18;
            if (deltaCount >= PRIME_DELTA_THRESHOLD) {
                score += 30;
            }
            if (observedActiveMs >= 6000L) {
                score += 18;
            }
            return score;
        }

        void record(PacketKind kind, long now) {
            integrateUntil(now);
            if (lastPacketTime == 0L || now - lastPacketTime > ACTIVE_GRACE_MS) {
                lastIntegratedTime = now;
            }

            lastPacketTime = now;
            switch (kind) {
                case DELTA -> deltaCount++;
                case LIGHT -> lightCount++;
                case CHUNK_DATA -> chunkDataCount++;
            }
        }

        void integrateUntil(long now) {
            if (lastPacketTime == 0L) {
                return;
            }

            long activeUntil = Math.min(now, lastPacketTime + ACTIVE_GRACE_MS);
            if (activeUntil <= lastIntegratedTime) {
                return;
            }

            observedActiveMs += activeUntil - lastIntegratedTime;
            lastIntegratedTime = activeUntil;
        }

        void mark(long now) {
            marked = true;
            markedAt = now;
        }

        boolean shouldDiscard(long now) {
            return now - lastPacketTime > STALE_TRACKED_MS;
        }

        MarkedPrimeChunk toMarkedChunk() {
            return new MarkedPrimeChunk(chunkX, chunkZ, deltaCount, lightCount, chunkDataCount, observedActiveMs, getPrimeScore(), markedAt, lastPacketTime);
        }
    }

    public record MarkedPrimeChunk(
        int chunkX,
        int chunkZ,
        int deltaCount,
        int lightCount,
        int chunkDataCount,
        long observedActiveMs,
        int primeScore,
        long markedAt,
        long lastSeenAt
    ) {
    }
}
