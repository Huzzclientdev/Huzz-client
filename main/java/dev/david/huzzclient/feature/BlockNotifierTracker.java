package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import dev.david.huzzclient.render.UltimateChunkToast;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class BlockNotifierTracker {
    private static final int MAX_RESULTS = 64;

    private final HuzzConfigManager configManager;
    private final Set<Long> seenBlocks = new HashSet<>();
    private long lastScanAt;

    public BlockNotifierTracker(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            clear();
            return;
        }

        HuzzConfig config = configManager.getConfig();
        if (!config.isBlockNotifierEnabled()) {
            clear();
            return;
        }

        long now = Util.getMeasuringTimeMs();
        if (now - lastScanAt < config.getRescanRateMs()) {
            return;
        }

        lastScanAt = now;
        List<WorldBlockScanner.FoundBlock> foundBlocks = WorldBlockScanner.scanBlocks(
            client,
            config.getBlockNotifierTargets(),
            config.getBlockEspRangeBlocks(),
            false,
            MAX_RESULTS
        );

        Set<Long> currentSeen = new HashSet<>(foundBlocks.size());
        for (WorldBlockScanner.FoundBlock foundBlock : foundBlocks) {
            long key = foundBlock.blockPos().asLong();
            currentSeen.add(key);
            if (seenBlocks.add(key)) {
                client.getToastManager().add(UltimateChunkToast.generic(
                    Text.literal("Block notifier"),
                    Text.literal(foundBlock.blockId() + " @ " + foundBlock.blockPos().toShortString()),
                    0xFFC89263,
                    0xFFFFD6A1
                ));
            }
        }

        seenBlocks.retainAll(currentSeen);
    }

    public void clear() {
        seenBlocks.clear();
        lastScanAt = 0L;
    }
}
