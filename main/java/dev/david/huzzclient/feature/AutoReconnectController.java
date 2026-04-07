package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.network.CookieStorage;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.util.Util;

import java.util.Map;

public final class AutoReconnectController {
    private static final String SERVER_NAME = "DonutSMP";
    private static final String SERVER_ADDRESS = "donutsmp.net";

    private final HuzzConfigManager configManager;

    private long reconnectAt;
    private boolean skipNextReconnect;

    public AutoReconnectController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        HuzzConfig config = configManager.getConfig();
        if (!config.isAutoReconnectEnabled()) {
            clear();
            return;
        }

        if (!(client.currentScreen instanceof DisconnectedScreen)) {
            reconnectAt = 0L;
            return;
        }

        if (skipNextReconnect) {
            reconnectAt = 0L;
            return;
        }

        long now = Util.getMeasuringTimeMs();
        if (reconnectAt == 0L) {
            reconnectAt = now + config.getAutoReconnectDelayMs();
            return;
        }

        if (now < reconnectAt) {
            return;
        }

        reconnectAt = 0L;
        ServerInfo serverInfo = new ServerInfo(SERVER_NAME, SERVER_ADDRESS, ServerInfo.ServerType.OTHER);
        ConnectScreen.connect(
            client.currentScreen,
            client,
            ServerAddress.parse(SERVER_ADDRESS),
            serverInfo,
            false,
            new CookieStorage(Map.of(), Map.of(), false)
        );
    }

    public void suppressNextReconnect() {
        skipNextReconnect = true;
        reconnectAt = 0L;
    }

    public void clear() {
        reconnectAt = 0L;
        skipNextReconnect = false;
    }
}
