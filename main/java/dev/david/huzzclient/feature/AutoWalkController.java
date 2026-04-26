package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

public final class AutoWalkController {
    private final HuzzConfigManager configManager;

    private KeyBinding forcedKey;

    public AutoWalkController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        HuzzConfig config = configManager.getConfig();
        if (client.player == null || client.world == null || client.currentScreen != null || !config.isAutoWalkEnabled()) {
            clear(client);
            return;
        }

        KeyBinding nextKey = keyFor(client, config.getAutoWalkDirection());
        if (nextKey == null) {
            clear(client);
            return;
        }

        if (forcedKey != null && forcedKey != nextKey) {
            forcedKey.setPressed(false);
        }
        nextKey.setPressed(true);
        forcedKey = nextKey;
    }

    public void clear() {
        clear(MinecraftClient.getInstance());
    }

    public void clear(MinecraftClient client) {
        if (forcedKey != null && client != null) {
            forcedKey.setPressed(false);
        }
        forcedKey = null;
    }

    private static KeyBinding keyFor(MinecraftClient client, HuzzConfig.AutoWalkDirection direction) {
        return switch (direction) {
            case FORWARD -> client.options.forwardKey;
            case LEFT -> client.options.leftKey;
            case BACK -> client.options.backKey;
            case RIGHT -> client.options.rightKey;
        };
    }
}
