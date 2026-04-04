package dev.david.huzzclient.detector;

final class TrackedChunkActivity {
    private final int chunkX;
    private final int chunkZ;
    private final PacketTimeWindow deltaPacketTimes = new PacketTimeWindow();
    private final PacketTimeWindow lightPacketTimes = new PacketTimeWindow();
    private final PacketTimeWindow chunkDataPacketTimes = new PacketTimeWindow();

    private long lastPacketTime;
    private long markedAt;
    private boolean marked;
    private int markedDeltaCount;
    private int markedLightCount;
    private int markedChunkDataCount;
    private int markedSuspicionScore;

    TrackedChunkActivity(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    int getChunkX() {
        return chunkX;
    }

    int getChunkZ() {
        return chunkZ;
    }

    void recordDelta(long now) {
        deltaPacketTimes.add(now);
        lastPacketTime = now;
    }

    void recordLight(long now) {
        lightPacketTimes.add(now);
        lastPacketTime = now;
    }

    void recordChunkData(long now) {
        chunkDataPacketTimes.add(now);
        lastPacketTime = now;
    }

    void prune(long cutoff) {
        deltaPacketTimes.prune(cutoff);
        lightPacketTimes.prune(cutoff);
        chunkDataPacketTimes.prune(cutoff);
    }

    int getDeltaCount() {
        return deltaPacketTimes.size();
    }

    int getLightCount() {
        return lightPacketTimes.size();
    }

    int getChunkDataCount() {
        return chunkDataPacketTimes.size();
    }

    long getLastPacketTime() {
        return lastPacketTime;
    }

    boolean isMarked() {
        return marked;
    }

    void mark(long now, int suspicionScore) {
        marked = true;
        markedAt = now;
        markedDeltaCount = getDeltaCount();
        markedLightCount = getLightCount();
        markedChunkDataCount = getChunkDataCount();
        markedSuspicionScore = suspicionScore;
    }

    ChunkPacketTracker.MarkedChunk toMarkedChunk() {
        return new ChunkPacketTracker.MarkedChunk(
            chunkX,
            chunkZ,
            markedDeltaCount,
            markedLightCount,
            markedChunkDataCount,
            markedSuspicionScore,
            markedAt,
            lastPacketTime
        );
    }

    boolean shouldDiscard() {
        return !marked && deltaPacketTimes.isEmpty() && lightPacketTimes.isEmpty() && chunkDataPacketTimes.isEmpty();
    }

    private static final class PacketTimeWindow {
        private long[] timestamps = new long[8];
        private int start;
        private int size;

        void add(long timestamp) {
            ensureCapacity(size + 1);
            timestamps[(start + size) % timestamps.length] = timestamp;
            size++;
        }

        void prune(long cutoff) {
            while (size > 0 && timestamps[start] < cutoff) {
                start = (start + 1) % timestamps.length;
                size--;
            }
        }

        int size() {
            return size;
        }

        boolean isEmpty() {
            return size == 0;
        }

        private void ensureCapacity(int requiredSize) {
            if (requiredSize <= timestamps.length) {
                return;
            }

            long[] expanded = new long[timestamps.length * 2];
            for (int index = 0; index < size; index++) {
                expanded[index] = timestamps[(start + index) % timestamps.length];
            }

            timestamps = expanded;
            start = 0;
        }
    }
}
