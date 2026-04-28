package dev.david.huzzclient.feature;

import dev.david.huzzclient.HuzzClient;
import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public final class TunnelBaseFinderController {
    private final HuzzConfigManager configManager;

    private float lockedYaw;
    private boolean forcingKeys;
    private boolean triggeredDisconnect;
    private float lastHealthWithAbsorption = -1.0F;
    private long lastStorageScanAt;

    public TunnelBaseFinderController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null || client.interactionManager == null) {
            clear(client);
            return;
        }

        HuzzConfig config = configManager.getConfig();
        if (!config.isTunnelBaseFinderEnabled() || client.currentScreen != null || triggeredDisconnect) {
            clear(client);
            return;
        }

        if (!forcingKeys) {
            lockedYaw = snapToRightAngle(player.getYaw());
        }

        float pitch = config.getTunnelBaseFinderMode() == HuzzConfig.TunnelBaseFinderMode.MODE_1X2 ? 45.0F : 0.0F;
        forceMovement(client, player, lockedYaw, pitch);
        checkDamageDisconnect(client, player, config);
        checkStorageThresholdDisconnect(client, config);
    }

    public void clear() {
        clear(MinecraftClient.getInstance());
    }

    public void clear(MinecraftClient client) {
        if (forcingKeys && client != null) {
            client.options.forwardKey.setPressed(false);
            client.options.attackKey.setPressed(false);
        }

        forcingKeys = false;
        triggeredDisconnect = false;
        lastHealthWithAbsorption = -1.0F;
        lastStorageScanAt = 0L;
    }

    private void forceMovement(MinecraftClient client, ClientPlayerEntity player, float yaw, float pitch) {
        player.setYaw(yaw);
        player.setHeadYaw(yaw);
        player.setBodyYaw(yaw);
        player.setPitch(pitch);
        player.lastYaw = yaw;
        player.lastPitch = pitch;
        client.options.forwardKey.setPressed(true);
        client.options.attackKey.setPressed(true);
        forcingKeys = true;
    }

    private void checkDamageDisconnect(MinecraftClient client, ClientPlayerEntity player, HuzzConfig config) {
        float healthWithAbsorption = player.getHealth() + player.getAbsorptionAmount();
        if (lastHealthWithAbsorption >= 0.0F && healthWithAbsorption + 0.05F < lastHealthWithAbsorption) {
            disconnect(client, config, Text.literal("Tunnel finder: damage detected"));
            return;
        }

        lastHealthWithAbsorption = healthWithAbsorption;
    }

    private void checkStorageThresholdDisconnect(MinecraftClient client, HuzzConfig config) {
        long now = Util.getMeasuringTimeMs();
        if (now - lastStorageScanAt < config.getRescanRateMs()) {
            return;
        }

        lastStorageScanAt = now;
        int storageCount = WorldBlockScanner.countBlocks(client, config.getStorageEspTargets(), config.getStorageEspRangeBlocks(), true);
        if (storageCount >= config.getStashFinderThreshold()) {
            disconnect(client, config, Text.literal("Tunnel finder: " + storageCount + " storage blocks"));
        }
    }

    private void disconnect(MinecraftClient client, HuzzConfig config, Text reason) {
        if (triggeredDisconnect) {
            return;
        }

        triggeredDisconnect = true;
        config.setTunnelBaseFinderEnabled(false);
        configManager.save();
        HuzzClient.getAutoReconnectController().suppressNextReconnect();
        client.disconnect(new TitleScreen(), false);
        client.setScreen(new TitleScreen());
        if (client.player != null) {
            client.player.sendMessage(reason, false);
        }
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
