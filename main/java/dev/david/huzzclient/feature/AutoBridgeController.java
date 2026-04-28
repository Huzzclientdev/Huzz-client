package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public final class AutoBridgeController {
    private static final float TARGET_PITCH = 80.0F;

    private final HuzzConfigManager configManager;

    private float lockedYaw;
    private boolean forcingKeys;

    public AutoBridgeController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        HuzzConfig config = configManager.getConfig();
        if (player == null || !config.isAutoBridgeEnabled() || client.currentScreen != null) {
            clear(client);
            return;
        }

        if (!forcingKeys) {
            lockedYaw = snapToRightAngle(player.getYaw());
        }

        player.setYaw(lockedYaw);
        player.setHeadYaw(lockedYaw);
        player.setBodyYaw(lockedYaw);
        player.setPitch(TARGET_PITCH);
        player.lastYaw = lockedYaw;
        player.lastPitch = TARGET_PITCH;
        client.options.backKey.setPressed(true);
        client.options.sneakKey.setPressed(true);
        client.options.useKey.setPressed(true);
        forcingKeys = true;
    }

    public void clear() {
        clear(MinecraftClient.getInstance());
    }

    public void clear(MinecraftClient client) {
        if (!forcingKeys || client == null) {
            forcingKeys = false;
            return;
        }

        client.options.backKey.setPressed(false);
        client.options.sneakKey.setPressed(false);
        client.options.useKey.setPressed(false);
        forcingKeys = false;
    }

    private static float snapToRightAngle(float yaw) {
        float normalized = ((yaw % 360.0F) + 360.0F) % 360.0F;
        int quadrant = Math.round(normalized / 90.0F) % 4;
        return switch (quadrant) {
            case 0 -> 0.0F;
            case 1 -> 90.0F;
            case 2 -> 180.0F;
            default -> -90.0F;
        };
    }
}
