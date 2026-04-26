package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.Locale;

public final class SilentSetHomeController {
    private static final long SUPPRESSION_WINDOW_MS = 4000L;

    private final HuzzConfigManager configManager;
    private boolean wasEnabled;
    private long suppressUntilMs;

    public SilentSetHomeController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        HuzzConfig config = configManager.getConfig();
        if (!config.isSilentSetHomeEnabled()) {
            wasEnabled = false;
            return;
        }

        if (wasEnabled || client.getNetworkHandler() == null || client.player == null) {
            return;
        }

        client.getNetworkHandler().sendChatCommand("sethome 1");
        suppressUntilMs = System.currentTimeMillis() + SUPPRESSION_WINDOW_MS;
        config.setSilentSetHomeEnabled(false);
        configManager.save();
        wasEnabled = true;
    }

    public boolean shouldSuppress(Text message) {
        if (message == null || System.currentTimeMillis() > suppressUntilMs) {
            return false;
        }

        String normalized = message.getString().toLowerCase(Locale.ROOT);
        if (!normalized.contains("home")) {
            return false;
        }

        if (normalized.contains("set")
            || normalized.contains("saved")
            || normalized.contains("created")
            || normalized.contains("overwrite")
            || normalized.contains("limit")
            || normalized.contains("/sethome")) {
            suppressUntilMs = 0L;
            return true;
        }

        return false;
    }

    public void clear() {
        wasEnabled = false;
        suppressUntilMs = 0L;
    }
}
