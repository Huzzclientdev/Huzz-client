package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public final class CriticalsController {
    private final HuzzConfigManager configManager;

    public CriticalsController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void beforeAttack(MinecraftClient client, ClientPlayerEntity player, Entity target) {
        HuzzConfig config = configManager.getConfig();
        if (!config.isCriticalsEnabled()
            || client.currentScreen != null
            || player.networkHandler == null
            || !player.isOnGround()
            || player.isTouchingWater()
            || player.isInLava()
            || player.hasVehicle()
            || target == null) {
            return;
        }

        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        boolean horizontalCollision = player.horizontalCollision;
        player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.0625D, z, false, horizontalCollision));
        player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false, horizontalCollision));
    }
}
