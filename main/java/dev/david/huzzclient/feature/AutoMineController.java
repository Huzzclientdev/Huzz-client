package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;

public final class AutoMineController {
    private final HuzzConfigManager configManager;
    private boolean forcingAttack;

    public AutoMineController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        HuzzConfig config = configManager.getConfig();
        if (client.player == null || client.world == null || client.currentScreen != null || !config.isAutoMineEnabled()) {
            clear(client);
            return;
        }

        client.options.attackKey.setPressed(true);
        forcingAttack = true;
    }

    public void clear() {
        clear(MinecraftClient.getInstance());
    }

    public void clear(MinecraftClient client) {
        if (forcingAttack && client != null && !configManager.getConfig().isTunnelBaseFinderEnabled()) {
            client.options.attackKey.setPressed(false);
        }
        forcingAttack = false;
    }
}
