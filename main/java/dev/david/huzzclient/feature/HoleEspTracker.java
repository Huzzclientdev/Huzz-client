package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
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

public final class HoleEspTracker {
    private static final int MAX_CHUNKS_PER_TICK = 4;
    private static final int MIN_DEPTH = 5;

    private final HuzzConfigManager configManager;
    private final Map<Long, List<Hole>> holesByChunk = new HashMap<>();
    private final Deque<Long> pendingChunkScans = new ArrayDeque<>();

    private volatile List<Hole> highlightedHoles = List.of();
    private boolean indexedLoadedChunks;
    private long indexedWorldId = Long.MIN_VALUE;

    public HoleEspTracker(HuzzConfigManager configManager) {
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
        if (!config.isHoleEspEnabled()) {
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

                    Hole single = detectSingleHole(world, start);
                    if (single != null) {
                        holes.add(single);
                        addCells(usedCells, single.cells());
                        continue;
                    }

                    Hole eastWest = detectLineHole(world, start, Direction.EAST);
                    if (eastWest != null) {
                        holes.add(eastWest);
                        addCells(usedCells, eastWest.cells());
                        continue;
                    }

                    Hole northSouth = detectLineHole(world, start, Direction.SOUTH);
                    if (northSouth != null) {
                        holes.add(northSouth);
                        addCells(usedCells, northSouth.cells());
                    }
                }
            }
        }

        return holes;
    }

    private static Hole detectSingleHole(BlockView world, BlockPos start) {
        if (!isSingleCrossSection(world, start)) {
            return null;
        }
        if (isSingleCrossSection(world, start.up())) {
            return null;
        }

        int depth = singleDepth(world, start);
        if (depth < MIN_DEPTH) {
            return null;
        }

        Box box = new Box(start.getX(), start.getY() - depth + 1.0D, start.getZ(), start.getX() + 1.0D, start.getY() + 1.0D, start.getZ() + 1.0D);
        return new Hole(box, 1, depth, Set.of(start.toImmutable()));
    }

    private static Hole detectLineHole(BlockView world, BlockPos start, Direction axis) {
        if (!isLineCrossSection(world, start, axis)) {
            return null;
        }
        if (isLineCrossSection(world, start.up(), axis) || isLineCrossSection(world, start.offset(axis.getOpposite()), axis)) {
            return null;
        }

        BlockPos second = start.offset(axis);
        BlockPos third = second.offset(axis);
        List<BlockPos> cells = List.of(start, second, third);
        int depth = lineDepth(world, start, axis);
        if (depth < MIN_DEPTH) {
            return null;
        }

        double maxX = axis == Direction.EAST ? third.getX() + 1.0D : start.getX() + 1.0D;
        double maxZ = axis == Direction.SOUTH ? third.getZ() + 1.0D : start.getZ() + 1.0D;
        Box box = new Box(start.getX(), start.getY() - depth + 1.0D, start.getZ(), maxX, start.getY() + 1.0D, maxZ);
        return new Hole(box, 3, depth, Set.of(start.toImmutable(), second.toImmutable(), third.toImmutable()));
    }

    private static int singleDepth(BlockView world, BlockPos start) {
        int depth = 0;
        while (isSingleCrossSection(world, start.down(depth))) {
            depth++;
        }
        return isSolid(world, start.down(depth)) ? depth : 0;
    }

    private static int lineDepth(BlockView world, BlockPos start, Direction axis) {
        int depth = 0;
        while (isLineCrossSection(world, start.down(depth), axis)) {
            depth++;
        }

        BlockPos floorStart = start.down(depth);
        return isSolid(world, floorStart)
            && isSolid(world, floorStart.offset(axis))
            && isSolid(world, floorStart.offset(axis, 2))
            ? depth
            : 0;
    }

    private static boolean isSingleCrossSection(BlockView world, BlockPos pos) {
        return isOpen(world, pos) && hasSolidWalls(world, pos);
    }

    private static boolean isLineCrossSection(BlockView world, BlockPos start, Direction axis) {
        BlockPos second = start.offset(axis);
        BlockPos third = second.offset(axis);
        if (!isOpen(world, start) || !isOpen(world, second) || !isOpen(world, third)) {
            return false;
        }
        if (!isSolid(world, start.offset(axis.getOpposite())) || !isSolid(world, third.offset(axis))) {
            return false;
        }

        Direction sideA = axis == Direction.EAST ? Direction.NORTH : Direction.WEST;
        Direction sideB = axis == Direction.EAST ? Direction.SOUTH : Direction.EAST;
        return isSolid(world, start.offset(sideA))
            && isSolid(world, start.offset(sideB))
            && isSolid(world, second.offset(sideA))
            && isSolid(world, second.offset(sideB))
            && isSolid(world, third.offset(sideA))
            && isSolid(world, third.offset(sideB));
    }

    private static boolean hasSolidWalls(BlockView world, BlockPos pos) {
        return isSolid(world, pos.north())
            && isSolid(world, pos.south())
            && isSolid(world, pos.east())
            && isSolid(world, pos.west());
    }

    private static boolean isOpen(BlockView world, BlockPos pos) {
        return !world.getBlockState(pos).isSolidBlock(world, pos);
    }

    private static boolean isSolid(BlockView world, BlockPos pos) {
        return world.getBlockState(pos).isSolidBlock(world, pos);
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

    private static void addCells(Set<Long> usedCells, Set<BlockPos> cells) {
        for (BlockPos cell : cells) {
            usedCells.add(cell.asLong());
        }
    }

    public record Hole(Box box, int length, int depth, Set<BlockPos> cells) {
    }
}
