package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import dev.david.huzzclient.render.UltimateChunkToast;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public final class StashFinderController {
    private static final long NOTIFY_COOLDOWN_MS = 15000L;

    private final HuzzConfigManager configManager;

    private long lastScanAt;
    private long lastNotifyAt;
    private boolean aboveThreshold;

    public StashFinderController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            reset();
            return;
        }

        HuzzConfig config = configManager.getConfig();
        if (!config.isStashFinderEnabled()) {
            reset();
            return;
        }

        long now = Util.getMeasuringTimeMs();
        if (now - lastScanAt < config.getRescanRateMs()) {
            return;
        }

        lastScanAt = now;
        int storageCount = WorldBlockScanner.countBlocks(client, config.getStorageEspTargets(), config.getStorageEspRangeBlocks(), true);
        boolean meetsThreshold = storageCount >= config.getStashFinderThreshold();
        if (meetsThreshold && (!aboveThreshold || now - lastNotifyAt >= NOTIFY_COOLDOWN_MS)) {
            client.getToastManager().add(UltimateChunkToast.generic(
                Text.literal("Stash finder"),
                Text.literal(storageCount + " storage blocks in range"),
                0xFF82C98D,
                0xFFAFF3B9
            ));
            lastNotifyAt = now;
        }

        aboveThreshold = meetsThreshold;
    }

    public void clear() {
        reset();
    }

    private void reset() {
        lastScanAt = 0L;
        lastNotifyAt = 0L;
        aboveThreshold = false;
    }
}
