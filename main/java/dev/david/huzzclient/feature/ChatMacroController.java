package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ChatMacroController {
    private final HuzzConfigManager configManager;
    private final Map<Integer, Boolean> keyStates = new HashMap<>();

    public ChatMacroController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        HuzzConfig config = configManager.getConfig();
        if (!config.isChatMacroEnabled() || client.player == null || client.getNetworkHandler() == null) {
            keyStates.clear();
            return;
        }

        Set<Integer> activeKeys = new HashSet<>();
        boolean allowTrigger = client.currentScreen == null;
        var window = client.getWindow();
        for (HuzzConfig.ChatMacroEntry macro : config.getChatMacros()) {
            int keyCode = macro.getKeyCode();
            activeKeys.add(keyCode);

            boolean pressed = InputUtil.isKeyPressed(window, keyCode);
            boolean wasPressed = keyStates.getOrDefault(keyCode, false);
            if (allowTrigger && pressed && !wasPressed) {
                sendMacro(client, macro.getMessage());
            }
            keyStates.put(keyCode, pressed);
        }

        keyStates.keySet().removeIf(keyCode -> !activeKeys.contains(keyCode));
    }

    private static void sendMacro(MinecraftClient client, String message) {
        String normalized = message == null ? "" : message.trim();
        if (normalized.isEmpty()) {
            return;
        }

        if (normalized.startsWith("/")) {
            client.getNetworkHandler().sendChatCommand(normalized.substring(1));
        } else {
            client.getNetworkHandler().sendChatMessage(normalized);
        }
    }
}
