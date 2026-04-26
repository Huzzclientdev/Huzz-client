package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;

public final class AutoRocketController {
    private final HuzzConfigManager configManager;

    private long nextUseAt;

    public AutoRocketController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.interactionManager == null) {
            clear();
            return;
        }

        HuzzConfig config = configManager.getConfig();
        if (!config.isAutoRocketEnabled() || client.currentScreen != null || !player.isGliding()) {
            clear();
            return;
        }

        Hand hand = rocketHand(player);
        if (hand == null) {
            return;
        }

        long now = Util.getMeasuringTimeMs();
        if (now < nextUseAt) {
            return;
        }

        ActionResult result = client.interactionManager.interactItem(player, hand);
        if (result.isAccepted()) {
            player.swingHand(hand);
            nextUseAt = now + config.getAutoRocketDelayMs();
        }
    }

    public void clear() {
        nextUseAt = 0L;
    }

    private static Hand rocketHand(ClientPlayerEntity player) {
        if (player.getMainHandStack().isOf(Items.FIREWORK_ROCKET)) {
            return Hand.MAIN_HAND;
        }
        if (player.getOffHandStack().isOf(Items.FIREWORK_ROCKET)) {
            return Hand.OFF_HAND;
        }
        return null;
    }
}
