package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public final class BlockEspTracker {
    private static final int MAX_RESULTS = 48;
    private static final int MAX_TRACERS = 16;

    private final HuzzConfigManager configManager;
    private final Mode mode;

    private volatile List<EspBlock> highlightedBlocks = List.of();
    private long lastScanAt;

    public BlockEspTracker(HuzzConfigManager configManager, Mode mode) {
        this.configManager = configManager;
        this.mode = mode;
    }

    public List<EspBlock> getHighlightedBlocks() {
        return highlightedBlocks;
    }

    public void clear() {
        highlightedBlocks = List.of();
        lastScanAt = 0L;
    }

    public void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            if (!highlightedBlocks.isEmpty()) {
                clear();
            }
            return;
        }

        HuzzConfig config = configManager.getConfig();
        if (!isEnabled(config)) {
            if (!highlightedBlocks.isEmpty()) {
                clear();
            }
            return;
        }

        long now = Util.getMeasuringTimeMs();
        if (now - lastScanAt < config.getRescanRateMs()) {
            return;
        }

        lastScanAt = now;
        highlightedBlocks = List.copyOf(scan(client, config));
    }

    private List<EspBlock> scan(MinecraftClient client, HuzzConfig config) {
        List<String> configuredTargets = mode == Mode.BLOCK ? config.getBlockEspTargets() : config.getStorageEspTargets();
        int range = mode == Mode.BLOCK ? config.getBlockEspRangeBlocks() : config.getStorageEspRangeBlocks();
        boolean tracers = mode == Mode.BLOCK ? config.isBlockEspTracers() : config.isStorageEspTracers();
        List<WorldBlockScanner.FoundBlock> foundBlocks = WorldBlockScanner.scanBlocks(client, configuredTargets, range, mode == Mode.STORAGE, MAX_RESULTS);
        if (foundBlocks.isEmpty()) {
            return List.of();
        }

        List<EspBlock> results = new ArrayList<>(foundBlocks.size());
        for (WorldBlockScanner.FoundBlock foundBlock : foundBlocks) {
            results.add(new EspBlock(
                foundBlock.blockPos(),
                foundBlock.blockId(),
                colorFor(foundBlock.blockId()),
                tracers,
                foundBlock.distanceSquared()
            ));
        }

        if (!tracers) {
            return results;
        }

        List<EspBlock> limitedTracerResults = new ArrayList<>(results.size());
        for (int i = 0; i < results.size(); i++) {
            EspBlock result = results.get(i);
            limitedTracerResults.add(i < MAX_TRACERS
                ? result
                : new EspBlock(result.blockPos(), result.blockId(), result.colorRgb(), false, result.distanceSquared()));
        }

        return limitedTracerResults;
    }

    private boolean isEnabled(HuzzConfig config) {
        return mode == Mode.BLOCK ? config.isBlockEspEnabled() : config.isStorageEspEnabled();
    }

    private static int colorFor(String blockId) {
        int hash = blockId.hashCode();
        float hue = ((hash >>> 1) & 1023) / 1023.0F;
        float saturation = 0.62F + (((hash >>> 11) & 0xFF) / 255.0F) * 0.25F;
        float brightness = 0.82F + (((hash >>> 19) & 0x7F) / 127.0F) * 0.16F;
        return Color.HSBtoRGB(hue, Math.min(0.92F, saturation), Math.min(0.98F, brightness));
    }

    public enum Mode {
        BLOCK,
        STORAGE
    }

    public record EspBlock(BlockPos blockPos, String blockId, int colorRgb, boolean tracers, double distanceSquared) {
    }
}
