package dev.david.huzzclient.feature;

import dev.david.huzzclient.HuzzClient;
import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;

public final class AutoDisconnectController {
    private final HuzzConfigManager configManager;

    private boolean triggered;

    public AutoDisconnectController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            clear();
            return;
        }

        HuzzConfig config = configManager.getConfig();
        if (!config.isAutoDisconnectEnabled() || triggered) {
            return;
        }

        for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
            if (player == client.player || player.isSpectator()) {
                continue;
            }

            triggered = true;
            HuzzClient.getAutoReconnectController().suppressNextReconnect();
            client.disconnect(new TitleScreen(), false);
            client.setScreen(new TitleScreen());
            return;
        }
    }

    public void clear() {
        triggered = false;
    }
}
