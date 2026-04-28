package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import dev.david.huzzclient.render.UltimateChunkToast;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SusTracker {
    private static final int MAX_CHUNKS_PER_TICK = 3;
    private static final int MAX_VISIBLE_BLOCKS = 256;
    private static final int SUS_COLOR_RGB = 0xFF3333;

    private final HuzzConfigManager configManager;
    private final Map<Long, Map<Long, SusBlock>> blocksByChunk = new HashMap<>();
    private final Map<Long, ChunkScan> chunkScans = new HashMap<>();
    private final Deque<Long> pendingChunkScans = new ArrayDeque<>();
    private final Set<Long> toastedChunks = new HashSet<>();

    private List<BlockEspTracker.EspBlock> highlightedBlocks = List.of();
    private List<SusChunk> highlightedChunks = List.of();
    private boolean indexedLoadedChunks;
    private long indexedWorldId = Long.MIN_VALUE;

    public SusTracker(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public List<BlockEspTracker.EspBlock> getHighlightedBlocks() {
        return highlightedBlocks;
    }

    public List<SusChunk> getHighlightedChunks() {
        return highlightedChunks;
    }

    public void clear() {
        blocksByChunk.clear();
        chunkScans.clear();
        pendingChunkScans.clear();
        toastedChunks.clear();
        highlightedBlocks = List.of();
        highlightedChunks = List.of();
        indexedLoadedChunks = false;
        indexedWorldId = Long.MIN_VALUE;
    }

    public void tick(MinecraftClient client) {
        HuzzConfig config = configManager.getConfig();
        if (client.world == null || client.player == null || (!config.isSusChunkFinderEnabled() && !config.isSusEspEnabled())) {
            if (!highlightedBlocks.isEmpty() || !blocksByChunk.isEmpty() || indexedLoadedChunks) {
                clear();
            }
            return;
        }

        long worldId = client.world.getRegistryKey().getValue().hashCode();
        if (indexedWorldId != worldId) {
            clear();
            indexedWorldId = worldId;
        }

        pruneUnloadedChunks(client);
        queueLoadedChunksOnce(client);
        processPendingChunkScans(client, config);
        highlightedBlocks = config.isSusEspEnabled() ? List.copyOf(collectVisibleBlocks(client, config.isSusEspTracers())) : List.of();
        highlightedChunks = config.isSusChunkFinderEnabled() ? List.copyOf(collectHighlightedChunks(config)) : List.of();
    }

    public void recordChunkData(MinecraftClient client, ChunkDataS2CPacket packet) {
        HuzzConfig config = configManager.getConfig();
        if (client.world == null || (!config.isSusChunkFinderEnabled() && !config.isSusEspEnabled())) {
            return;
        }
        queueChunkScan(packet.getChunkX(), packet.getChunkZ());
        processPendingChunkScans(client, config);
    }

    public void recordChunkDelta(ChunkDeltaUpdateS2CPacket packet) {
        HuzzConfig config = configManager.getConfig();
        if (!config.isSusChunkFinderEnabled() && !config.isSusEspEnabled()) {
            return;
        }

        packet.visitUpdates((blockPos, blockState) -> recordBlockState(config, blockPos.toImmutable(), blockState));
    }

    public void recordBlockUpdate(BlockUpdateS2CPacket packet) {
        HuzzConfig config = configManager.getConfig();
        if (!config.isSusChunkFinderEnabled() && !config.isSusEspEnabled()) {
            return;
        }
        recordBlockState(config, packet.getPos().toImmutable(), packet.getState());
    }

    private void recordBlockState(HuzzConfig config, BlockPos blockPos, BlockState state) {
        long chunkKey = ChunkPos.toLong(blockPos.getX() >> 4, blockPos.getZ() >> 4);
        Map<Long, SusBlock> chunkBlocks = blocksByChunk.computeIfAbsent(chunkKey, ignored -> new HashMap<>());
        ChunkScan scan = chunkScans.computeIfAbsent(chunkKey, ignored -> new ChunkScan());
        SusKind previousKind = chunkBlocks.containsKey(blockPos.asLong()) ? chunkBlocks.get(blockPos.asLong()).kind() : null;
        if (previousKind != null) {
            scan.remove(previousKind);
        }

        SusKind kind = susKind(state);
        if (kind == null) {
            chunkBlocks.remove(blockPos.asLong());
            if (chunkBlocks.isEmpty()) {
                blocksByChunk.remove(chunkKey);
            }
        } else {
            chunkBlocks.put(blockPos.asLong(), new SusBlock(blockPos, kind));
            scan.add(kind);
            maybeToast(MinecraftClient.getInstance(), config, chunkKey, scan);
        }
    }

    private void processPendingChunkScans(MinecraftClient client, HuzzConfig config) {
        ClientChunkManager chunkManager = client.world.getChunkManager();
        int processed = 0;
        while (processed < MAX_CHUNKS_PER_TICK && !pendingChunkScans.isEmpty()) {
            long chunkKey = pendingChunkScans.removeFirst();
            int chunkX = (int) chunkKey;
            int chunkZ = (int) (chunkKey >> 32);
            WorldChunk chunk = chunkManager.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
            if (chunk == null) {
                blocksByChunk.remove(chunkKey);
                chunkScans.remove(chunkKey);
                continue;
            }

            ChunkScan scan = scanChunk(chunk);
            if (scan.blocks().isEmpty()) {
                blocksByChunk.remove(chunkKey);
                chunkScans.remove(chunkKey);
            } else {
                blocksByChunk.put(chunkKey, scan.blocks());
                chunkScans.put(chunkKey, scan);
                maybeToast(client, config, chunkKey, scan);
            }
            processed++;
        }
    }

    private ChunkScan scanChunk(WorldChunk chunk) {
        ChunkScan scan = new ChunkScan();
        ChunkSection[] sections = chunk.getSectionArray();
        int baseChunkX = chunk.getPos().x << 4;
        int baseChunkZ = chunk.getPos().z << 4;
        int bottomY = chunk.getBottomY();

        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            ChunkSection section = sections[sectionIndex];
            if (section == null || section.isEmpty()) {
                continue;
            }

            int sectionBaseY = bottomY + sectionIndex * 16;
            for (int localY = 0; localY < 16; localY++) {
                int worldY = sectionBaseY + localY;
                for (int localZ = 0; localZ < 16; localZ++) {
                    int worldZ = baseChunkZ + localZ;
                    for (int localX = 0; localX < 16; localX++) {
                        BlockState state = section.getBlockState(localX, localY, localZ);
                        SusKind kind = susKind(state);
                        if (kind == null) {
                            continue;
                        }
                        BlockPos blockPos = new BlockPos(baseChunkX + localX, worldY, worldZ);
                        scan.put(blockPos, kind);
                    }
                }
            }
        }
        return scan;
    }

    private List<BlockEspTracker.EspBlock> collectVisibleBlocks(MinecraftClient client, boolean tracers) {
        double playerX = client.player.getX();
        double playerY = client.player.getEyeY();
        double playerZ = client.player.getZ();
        List<BlockEspTracker.EspBlock> blocks = new ArrayList<>();
        for (Map<Long, SusBlock> chunkBlocks : blocksByChunk.values()) {
            for (SusBlock block : chunkBlocks.values()) {
                double distanceSquared = distanceSquared(block.pos(), playerX, playerY, playerZ);
                blocks.add(new BlockEspTracker.EspBlock(block.pos(), block.kind().label(), SUS_COLOR_RGB, tracers, distanceSquared));
            }
        }
        blocks.sort(Comparator.comparingDouble(BlockEspTracker.EspBlock::distanceSquared));
        return blocks.size() <= MAX_VISIBLE_BLOCKS ? blocks : List.copyOf(blocks.subList(0, MAX_VISIBLE_BLOCKS));
    }

    private List<SusChunk> collectHighlightedChunks(HuzzConfig config) {
        List<SusChunk> chunks = new ArrayList<>();
        for (Map.Entry<Long, ChunkScan> entry : chunkScans.entrySet()) {
            ChunkScan scan = entry.getValue();
            if (scan.score() < config.getSusChunkSensitivity()) {
                continue;
            }
            long key = entry.getKey();
            chunks.add(new SusChunk((int) (long) key, (int) (key >> 32), scan.score(), scan.summary()));
        }
        chunks.sort(Comparator.comparingInt(SusChunk::score).reversed());
        return chunks.size() <= MAX_VISIBLE_BLOCKS ? chunks : List.copyOf(chunks.subList(0, MAX_VISIBLE_BLOCKS));
    }

    private void maybeToast(MinecraftClient client, HuzzConfig config, long chunkKey, ChunkScan scan) {
        if (!config.isSusChunkFinderEnabled() || scan.score() < config.getSusChunkSensitivity() || !toastedChunks.add(chunkKey) || client.getToastManager() == null) {
            return;
        }

        client.getToastManager().add(UltimateChunkToast.generic(
            Text.literal("Suspicious chunk"),
            Text.literal(scan.summary()),
            0xFFFF67D8,
            0xFFFFB7E8
        ));
    }

    private void queueLoadedChunksOnce(MinecraftClient client) {
        if (indexedLoadedChunks) {
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
        long chunkKey = ChunkPos.toLong(chunkX, chunkZ);
        if (!pendingChunkScans.contains(chunkKey)) {
            pendingChunkScans.addLast(chunkKey);
        }
    }

    private void pruneUnloadedChunks(MinecraftClient client) {
        ClientChunkManager chunkManager = client.world.getChunkManager();
        blocksByChunk.keySet().removeIf(chunkKey -> chunkManager.getChunk((int) (long) chunkKey, (int) (chunkKey >> 32), ChunkStatus.FULL, false) == null);
        chunkScans.keySet().removeIf(chunkKey -> chunkManager.getChunk((int) (long) chunkKey, (int) (chunkKey >> 32), ChunkStatus.FULL, false) == null);
    }

    private static SusKind susKind(BlockState state) {
        if (state.isOf(Blocks.AMETHYST_CLUSTER)) {
            return SusKind.AMETHYST;
        }
        if ((state.isOf(Blocks.BEEHIVE) || state.isOf(Blocks.BEE_NEST)) && state.contains(BeehiveBlock.HONEY_LEVEL) && state.get(BeehiveBlock.HONEY_LEVEL) >= BeehiveBlock.FULL_HONEY_LEVEL) {
            return SusKind.BEEHIVE;
        }
        if (state.isOf(Blocks.KELP) && state.contains(Properties.AGE_25) && state.get(Properties.AGE_25) >= 25) {
            return SusKind.KELP;
        }
        return null;
    }

    private static double distanceSquared(BlockPos blockPos, double playerX, double playerY, double playerZ) {
        double dx = blockPos.getX() + 0.5D - playerX;
        double dy = blockPos.getY() + 0.5D - playerY;
        double dz = blockPos.getZ() + 0.5D - playerZ;
        return dx * dx + dy * dy + dz * dz;
    }

    private enum SusKind {
        KELP("kelp"),
        AMETHYST("amethyst"),
        BEEHIVE("beehive");

        private final String label;

        SusKind(String label) {
            this.label = label;
        }

        String label() {
            return label;
        }
    }

    private record SusBlock(BlockPos pos, SusKind kind) {
    }

    public record SusChunk(int chunkX, int chunkZ, int score, String summary) {
    }

    private static final class ChunkScan {
        private final Map<Long, SusBlock> blocks = new HashMap<>();
        private int kelp;
        private int amethyst;
        private int beehives;

        Map<Long, SusBlock> blocks() {
            return blocks;
        }

        void put(BlockPos pos, SusKind kind) {
            blocks.put(pos.asLong(), new SusBlock(pos, kind));
            add(kind);
        }

        void add(SusKind kind) {
            switch (kind) {
                case KELP -> kelp++;
                case AMETHYST -> amethyst++;
                case BEEHIVE -> beehives++;
            }
        }

        void remove(SusKind kind) {
            switch (kind) {
                case KELP -> kelp = Math.max(0, kelp - 1);
                case AMETHYST -> amethyst = Math.max(0, amethyst - 1);
                case BEEHIVE -> beehives = Math.max(0, beehives - 1);
            }
        }

        int score() {
            return kelp + amethyst * 2 + beehives * 3;
        }

        String summary() {
            return "kelp " + kelp + " | amethyst " + amethyst + " | bees " + beehives;
        }
    }
}
