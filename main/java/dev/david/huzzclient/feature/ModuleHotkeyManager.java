package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

import java.util.HashMap;
import java.util.Map;

public final class ModuleHotkeyManager {
    private final HuzzConfigManager configManager;
    private final Map<Integer, Boolean> keyStates = new HashMap<>();

    public ModuleHotkeyManager(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        HuzzConfig config = configManager.getConfig();
        boolean allowToggle = client.currentScreen == null;
        var window = client.getWindow();

        poll(window, allowToggle, config.getChunkFinderKeyCode(), () -> config.setChunkFinderEnabled(!config.isChunkFinderEnabled()));
        poll(window, allowToggle, config.getTimeDebugKeyCode(), () -> config.setTimeDebugEnabled(!config.isTimeDebugEnabled()));
        poll(window, allowToggle, config.getBlockEspKeyCode(), () -> config.setBlockEspEnabled(!config.isBlockEspEnabled()));
        poll(window, allowToggle, config.getStorageEspKeyCode(), () -> config.setStorageEspEnabled(!config.isStorageEspEnabled()));
        poll(window, allowToggle, config.getFreeCamKeyCode(), () -> config.setFreeCamEnabled(!config.isFreeCamEnabled()));
        poll(window, allowToggle, config.getFreeLookKeyCode(), () -> config.setFreeLookEnabled(!config.isFreeLookEnabled()));
        poll(window, allowToggle, config.getFullBrightKeyCode(), () -> config.setFullBrightEnabled(!config.isFullBrightEnabled()));
        poll(window, allowToggle, config.getItemNametagsKeyCode(), () -> config.setItemNametagsEnabled(!config.isItemNametagsEnabled()));
    }

    private void poll(net.minecraft.client.util.Window window, boolean allowToggle, int keyCode, Runnable toggleAction) {
        if (keyCode == HuzzConfig.NO_KEY) {
            return;
        }

        boolean pressed = InputUtil.isKeyPressed(window, keyCode);
        boolean wasPressed = keyStates.getOrDefault(keyCode, false);
        if (allowToggle && pressed && !wasPressed) {
            toggleAction.run();
            configManager.save();
        }

        keyStates.put(keyCode, pressed);
    }
}
