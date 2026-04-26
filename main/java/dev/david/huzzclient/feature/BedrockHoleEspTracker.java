package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BedrockHoleEspTracker {
    private static final int MAX_CHUNKS_PER_TICK = 4;

    private final HuzzConfigManager configManager;
    private final Map<Long, List<Hole>> holesByChunk = new HashMap<>();
    private final Deque<Long> pendingChunkScans = new ArrayDeque<>();

    private volatile List<Hole> highlightedHoles = List.of();
    private boolean indexedLoadedChunks;
    private long indexedWorldId = Long.MIN_VALUE;

    public BedrockHoleEspTracker(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public List<Hole> getHighlightedHoles() {
        return highlightedHoles;
    }

    public void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            clear();
            return;
        }

        HuzzConfig config = configManager.getConfig();
        if (!config.isBedrockHoleEspEnabled()) {
            clear();
            return;
        }

        if (indexedWorldId != client.world.getRegistryKey().getValue().hashCode()) {
            holesByChunk.clear();
            pendingChunkScans.clear();
            highlightedHoles = List.of();
            indexedLoadedChunks = false;
            indexedWorldId = client.world.getRegistryKey().getValue().hashCode();
        }

        pruneUnloadedChunks(client);
        queueLoadedChunksOnce(client);
        processPendingChunkScans(client);
        highlightedHoles = List.copyOf(collectVisibleHoles());
    }

    public void clear() {
        holesByChunk.clear();
        pendingChunkScans.clear();
        highlightedHoles = List.of();
        indexedLoadedChunks = false;
        indexedWorldId = Long.MIN_VALUE;
    }

    private void processPendingChunkScans(MinecraftClient client) {
        if (client.world == null) {
            return;
        }

        ClientChunkManager chunkManager = client.world.getChunkManager();
        int processed = 0;
        while (processed < MAX_CHUNKS_PER_TICK && !pendingChunkScans.isEmpty()) {
            long chunkKey = pendingChunkScans.removeFirst();
            int chunkX = ChunkPos.getPackedX(chunkKey);
            int chunkZ = ChunkPos.getPackedZ(chunkKey);
            WorldChunk chunk = chunkManager.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
            if (chunk == null) {
                holesByChunk.remove(chunkKey);
                continue;
            }
            holesByChunk.put(chunkKey, scanChunk(chunk, client));
            processed++;
        }
    }

    private List<Hole> collectVisibleHoles() {
        ArrayList<Hole> holes = new ArrayList<>();
        for (List<Hole> chunkHoles : holesByChunk.values()) {
            holes.addAll(chunkHoles);
        }
        return holes;
    }

    private static List<Hole> scanChunk(WorldChunk chunk, MinecraftClient client) {
        ArrayList<Hole> holes = new ArrayList<>();
        Set<Long> usedCells = new HashSet<>();
        BlockView world = client.world;
        int minY = client.world.getBottomY();
        int maxY = client.world.getTopYInclusive();
        int startX = chunk.getPos().getStartX();
        int endX = chunk.getPos().getEndX();
        int startZ = chunk.getPos().getStartZ();
        int endZ = chunk.getPos().getEndZ();

        for (int y = minY; y <= maxY; y++) {
            for (int x = startX; x <= endX; x++) {
                for (int z = startZ; z <= endZ; z++) {
                    BlockPos start = new BlockPos(x, y, z);
                    if (usedCells.contains(start.asLong())) {
                        continue;
                    }

                    Hole eastWest = detectHole(world, start, Direction.EAST);
                    if (eastWest != null) {
                        holes.add(eastWest);
                        addCells(usedCells, eastWest.cells());
                        continue;
                    }

                    Hole northSouth = detectHole(world, start, Direction.SOUTH);
                    if (northSouth != null) {
                        holes.add(northSouth);
                        addCells(usedCells, northSouth.cells());
                    }
                }
            }
        }

        return holes;
    }

    private static Hole detectHole(BlockView world, BlockPos start, Direction axis) {
        Direction opposite = axis.getOpposite();
        BlockPos second = start.offset(axis);
        if (!isBedrock(world, start.offset(opposite)) || !isBedrock(world, second.offset(axis))) {
            return null;
        }

        if (isBedrock(world, start) || isBedrock(world, second)) {
            return null;
        }

        Direction sideA = axis == Direction.EAST ? Direction.NORTH : Direction.WEST;
        Direction sideB = axis == Direction.EAST ? Direction.SOUTH : Direction.EAST;
        if (!isBedrock(world, start.offset(sideA))
            || !isBedrock(world, start.offset(sideB))
            || !isBedrock(world, second.offset(sideA))
            || !isBedrock(world, second.offset(sideB))
            || !isBedrock(world, start.down())
            || !isBedrock(world, second.down())
            || !isBedrock(world, start.up())
            || !isBedrock(world, second.up())) {
            return null;
        }

        double maxX = axis == Direction.EAST ? second.getX() + 1.0D : start.getX() + 1.0D;
        double maxZ = axis == Direction.SOUTH ? second.getZ() + 1.0D : start.getZ() + 1.0D;
        Box box = new Box(start.getX(), start.getY(), start.getZ(), maxX, start.getY() + 1.0D, maxZ);
        return new Hole(box, 2, 1, Set.of(start.toImmutable(), second.toImmutable()));
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
        long chunkKey = ChunkPos.toLong(chunkX, chunkZ);
        if (!pendingChunkScans.contains(chunkKey)) {
            pendingChunkScans.addLast(chunkKey);
        }
    }

    private void pruneUnloadedChunks(MinecraftClient client) {
        ClientChunkManager chunkManager = client.world.getChunkManager();
        holesByChunk.entrySet().removeIf(entry ->
            chunkManager.getChunk(ChunkPos.getPackedX(entry.getKey()), ChunkPos.getPackedZ(entry.getKey()), ChunkStatus.FULL, false) == null);
        pendingChunkScans.removeIf(chunkKey ->
            chunkManager.getChunk(ChunkPos.getPackedX(chunkKey), ChunkPos.getPackedZ(chunkKey), ChunkStatus.FULL, false) == null);
    }

    private static boolean isBedrock(BlockView world, BlockPos pos) {
        return world.getBlockState(pos).isOf(Blocks.BEDROCK);
    }

    private static void addCells(Set<Long> usedCells, Set<BlockPos> cells) {
        for (BlockPos cell : cells) {
            usedCells.add(cell.asLong());
        }
    }

    public record Hole(Box box, int length, int depth, Set<BlockPos> cells) {
    }
}
