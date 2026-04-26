package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import dev.david.huzzclient.mixin.MinecraftClientInvoker;
import net.minecraft.client.MinecraftClient;

public final class DoubleClickController {
    private static final int NO_BUTTON = -1;

    private final HuzzConfigManager configManager;

    private long scheduledAtMs;
    private int scheduledButton = NO_BUTTON;
    private boolean replaying;

    public DoubleClickController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void recordClick(MinecraftClient client, int button) {
        HuzzConfig config = configManager.getConfig();
        if (!config.isDoubleClickEnabled() || replaying || client.player == null || client.world == null || client.currentScreen != null) {
            return;
        }

        if (button != 0 && button != 1) {
            return;
        }

        scheduledButton = button;
        scheduledAtMs = System.currentTimeMillis() + config.getDoubleClickDelayMs();
    }

    public void tick(MinecraftClient client) {
        if (scheduledButton == NO_BUTTON) {
            return;
        }

        HuzzConfig config = configManager.getConfig();
        if (!config.isDoubleClickEnabled() || client.player == null || client.world == null || client.currentScreen != null) {
            clear();
            return;
        }

        if (System.currentTimeMillis() < scheduledAtMs) {
            return;
        }

        replaying = true;
        try {
            MinecraftClientInvoker invoker = (MinecraftClientInvoker) client;
            if (scheduledButton == 0) {
                invoker.huzzclient$invokeDoAttack();
            } else if (scheduledButton == 1) {
                invoker.huzzclient$invokeDoItemUse();
            }
        } finally {
            replaying = false;
            clear();
        }
    }

    public void clear() {
        scheduledButton = NO_BUTTON;
        scheduledAtMs = 0L;
    }
}
