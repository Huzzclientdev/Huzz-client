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

        for (HuzzConfig.ModuleKey moduleKey : HuzzConfig.ModuleKey.values()) {
            poll(window, allowToggle, config.getModuleKeyCode(moduleKey), () -> toggleModule(config, moduleKey));
        }
    }

    private void toggleModule(HuzzConfig config, HuzzConfig.ModuleKey moduleKey) {
        switch (moduleKey) {
            case CHUNK_FINDER -> config.setChunkFinderEnabled(!config.isChunkFinderEnabled());
            case TIME_DEBUG -> config.setTimeDebugEnabled(!config.isTimeDebugEnabled());
            case PRIME_CHUNK_FINDER -> config.setPrimeChunkFinderEnabled(!config.isPrimeChunkFinderEnabled());
            case BLOCK_ESP -> config.setBlockEspEnabled(!config.isBlockEspEnabled());
            case STORAGE_ESP -> config.setStorageEspEnabled(!config.isStorageEspEnabled());
            case HOLE_ESP -> config.setHoleEspEnabled(!config.isHoleEspEnabled());
            case BREAK_PROGRESS -> config.setBreakProgressEnabled(!config.isBreakProgressEnabled());
            case STASH_FINDER -> config.setStashFinderEnabled(!config.isStashFinderEnabled());
            case BLOCK_NOTIFIER -> config.setBlockNotifierEnabled(!config.isBlockNotifierEnabled());
            case CHAT_MACRO -> config.setChatMacroEnabled(!config.isChatMacroEnabled());
            case PLAYER_NOTIFIER -> config.setPlayerNotifierEnabled(!config.isPlayerNotifierEnabled());
            case AUTO_DISCONNECT -> config.setAutoDisconnectEnabled(!config.isAutoDisconnectEnabled());
            case PLAYER_ESP -> config.setPlayerEspEnabled(!config.isPlayerEspEnabled());
            case SWING_SPEED -> config.setSwingSpeedEnabled(!config.isSwingSpeedEnabled());
            case FREE_CAM -> config.setFreeCamEnabled(!config.isFreeCamEnabled());
            case FREE_LOOK -> config.setFreeLookEnabled(!config.isFreeLookEnabled());
            case AUTO_REPLENISH -> config.setAutoReplenishEnabled(!config.isAutoReplenishEnabled());
            case AUTO_BRIDGE -> config.setAutoBridgeEnabled(!config.isAutoBridgeEnabled());
            case FAST_USE -> config.setFastUseEnabled(!config.isFastUseEnabled());
            case FULL_BRIGHT -> config.setFullBrightEnabled(!config.isFullBrightEnabled());
            case MOTION_BLUR -> config.setMotionBlurEnabled(!config.isMotionBlurEnabled());
            case BLOCK_SELECT -> config.setBlockSelectionMode(
                config.getBlockSelectionMode() == HuzzConfig.BlockSelectionMode.OFF
                    ? HuzzConfig.BlockSelectionMode.FILLED
                    : HuzzConfig.BlockSelectionMode.OFF);
            case ITEM_NAMETAGS -> config.setItemNametagsEnabled(!config.isItemNametagsEnabled());
            case FAKE_MEDIA -> config.setFakeMediaEnabled(!config.isFakeMediaEnabled());
            case FAKE_PAY -> config.setFakePayEnabled(!config.isFakePayEnabled());
            case FAKE_STATS -> config.setFakeStatsEnabled(!config.isFakeStatsEnabled());
            case AUTO_TOTEM -> config.setAutoTotemEnabled(!config.isAutoTotemEnabled());
            case AUTO_ROCKET -> config.setAutoRocketEnabled(!config.isAutoRocketEnabled());
            case NAME_PROTECT -> config.setNameProtectEnabled(!config.isNameProtectEnabled());
            case SKIN_PROTECT -> config.setSkinProtectEnabled(!config.isSkinProtectEnabled());
            case AUTO_RECONNECT -> config.setAutoReconnectEnabled(!config.isAutoReconnectEnabled());
            case HUD -> config.setFpsHudEnabled(!config.isFpsHudEnabled());
            case CRAFTER_MACRO -> config.setCrafterMacroEnabled(!config.isCrafterMacroEnabled());
        }
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
