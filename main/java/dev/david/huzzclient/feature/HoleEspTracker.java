package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class HoleEspTracker {
    private static final int MIN_DEPTH = 4;

    private final HuzzConfigManager configManager;

    private volatile List<Hole> highlightedHoles = List.of();
    private long lastScanAt;

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

        long now = Util.getMeasuringTimeMs();
        if (now - lastScanAt < config.getRescanRateMs()) {
            return;
        }

        lastScanAt = now;
        highlightedHoles = List.copyOf(scan(client, config.getHoleEspRangeBlocks()));
    }

    public void clear() {
        highlightedHoles = List.of();
        lastScanAt = 0L;
    }

    private static List<Hole> scan(MinecraftClient client, int range) {
        ArrayList<Hole> results = new ArrayList<>();
        Set<BlockPos> usedCells = new HashSet<>();
        int centerX = client.player.getBlockX();
        int centerY = client.player.getBlockY();
        int centerZ = client.player.getBlockZ();
        int minY = centerY - 24;
        int maxY = centerY + 8;
        double rangeSquared = range * range;

        for (int y = maxY; y >= minY; y--) {
            for (int x = centerX - range; x <= centerX + range; x++) {
                for (int z = centerZ - range; z <= centerZ + range; z++) {
                    BlockPos top = new BlockPos(x, y, z);
                    if (usedCells.contains(top) || distanceSquared(centerX, centerY, centerZ, top) > rangeSquared || !isAir(client.world, top)) {
                        continue;
                    }

                    Hole lineHole = detectLineHole(client.world, top);
                    if (lineHole != null) {
                        results.add(lineHole);
                        usedCells.addAll(lineHole.cells());
                        continue;
                    }

                    Hole singleHole = detectSingleHole(client.world, top);
                    if (singleHole != null) {
                        results.add(singleHole);
                        usedCells.addAll(singleHole.cells());
                    }
                }
            }
        }

        return results;
    }

    private static Hole detectSingleHole(BlockView world, BlockPos top) {
        if (isSingleHoleCell(world, top.up())) {
            return null;
        }

        int depth = depthOfSingleHole(world, top);
        if (depth < MIN_DEPTH) {
            return null;
        }

        Set<BlockPos> cells = new HashSet<>(depth);
        for (int offset = 0; offset < depth; offset++) {
            cells.add(top.down(offset).toImmutable());
        }

        double minY = top.getY() - depth + 1.0D;
        Box box = new Box(top.getX(), minY, top.getZ(), top.getX() + 1.0D, top.getY() + 1.0D, top.getZ() + 1.0D);
        return new Hole(box, 1, depth, Set.copyOf(cells));
    }

    private static Hole detectLineHole(BlockView world, BlockPos top) {
        Hole xHole = detectLineHole(world, top, Direction.EAST);
        if (xHole != null) {
            return xHole;
        }
        return detectLineHole(world, top, Direction.SOUTH);
    }

    private static Hole detectLineHole(BlockView world, BlockPos top, Direction axis) {
        Direction opposite = axis.getOpposite();
        if (isLineHoleCell(world, top.offset(opposite), axis)) {
            return null;
        }

        BlockPos third = top.offset(axis, 2);
        if (!isLineHoleCell(world, top, axis) || isLineHoleCell(world, third.offset(axis), axis)) {
            return null;
        }

        int depth = depthOfLineHole(world, top, axis);
        if (depth < MIN_DEPTH) {
            return null;
        }

        Set<BlockPos> cells = new HashSet<>(depth * 3);
        for (int vertical = 0; vertical < depth; vertical++) {
            BlockPos rowStart = top.down(vertical);
            cells.add(rowStart.toImmutable());
            cells.add(rowStart.offset(axis).toImmutable());
            cells.add(rowStart.offset(axis, 2).toImmutable());
        }

        double minY = top.getY() - depth + 1.0D;
        double maxX = axis == Direction.EAST ? third.getX() + 1.0D : top.getX() + 1.0D;
        double maxZ = axis == Direction.SOUTH ? third.getZ() + 1.0D : top.getZ() + 1.0D;
        Box box = new Box(top.getX(), minY, top.getZ(), maxX, top.getY() + 1.0D, maxZ);
        return new Hole(box, 3, depth, Set.copyOf(cells));
    }

    private static int depthOfSingleHole(BlockView world, BlockPos top) {
        int depth = 0;
        while (isSingleHoleCell(world, top.down(depth))) {
            depth++;
        }
        return isSolid(world, top.down(depth)) ? depth : 0;
    }

    private static int depthOfLineHole(BlockView world, BlockPos top, Direction axis) {
        int depth = 0;
        while (isLineHoleCell(world, top.down(depth), axis)) {
            depth++;
        }

        BlockPos floor = top.down(depth);
        return hasSolidLineFloor(world, floor, axis) ? depth : 0;
    }

    private static boolean isSingleHoleCell(BlockView world, BlockPos pos) {
        return isAir(world, pos)
            && isSolid(world, pos.north())
            && isSolid(world, pos.south())
            && isSolid(world, pos.east())
            && isSolid(world, pos.west());
    }

    private static boolean isLineHoleCell(BlockView world, BlockPos start, Direction axis) {
        BlockPos second = start.offset(axis);
        BlockPos third = second.offset(axis);
        Direction sideA = axis == Direction.EAST ? Direction.NORTH : Direction.WEST;
        Direction sideB = axis == Direction.EAST ? Direction.SOUTH : Direction.EAST;

        return isAir(world, start)
            && isAir(world, second)
            && isAir(world, third)
            && isSolid(world, start.offset(axis.getOpposite()))
            && isSolid(world, third.offset(axis))
            && isSolid(world, start.offset(sideA))
            && isSolid(world, start.offset(sideB))
            && isSolid(world, second.offset(sideA))
            && isSolid(world, second.offset(sideB))
            && isSolid(world, third.offset(sideA))
            && isSolid(world, third.offset(sideB));
    }

    private static boolean hasSolidLineFloor(BlockView world, BlockPos floorStart, Direction axis) {
        return isSolid(world, floorStart)
            && isSolid(world, floorStart.offset(axis))
            && isSolid(world, floorStart.offset(axis, 2));
    }

    private static boolean isAir(BlockView world, BlockPos pos) {
        return world.getBlockState(pos).isAir();
    }

    private static boolean isSolid(BlockView world, BlockPos pos) {
        return world.getBlockState(pos).isSolidBlock(world, pos);
    }

    private static double distanceSquared(int centerX, int centerY, int centerZ, BlockPos pos) {
        double dx = pos.getX() - centerX;
        double dy = pos.getY() - centerY;
        double dz = pos.getZ() - centerZ;
        return dx * dx + dy * dy + dz * dz;
    }

    public record Hole(Box box, int length, int depth, Set<BlockPos> cells) {
    }
}
