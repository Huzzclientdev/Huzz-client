package dev.david.huzzclient;

import dev.david.huzzclient.config.HuzzConfigManager;
import dev.david.huzzclient.feature.BlockEspTracker;
import dev.david.huzzclient.feature.BlockNotifierTracker;
import dev.david.huzzclient.feature.ChatCommandController;
import dev.david.huzzclient.feature.AutoTotemController;
import dev.david.huzzclient.feature.AutoDisconnectController;
import dev.david.huzzclient.feature.AutoBridgeController;
import dev.david.huzzclient.feature.AimAssistController;
import dev.david.huzzclient.feature.AutoReconnectController;
import dev.david.huzzclient.feature.AutoReplenishController;
import dev.david.huzzclient.feature.AutoRocketController;
import dev.david.huzzclient.feature.AutoCrystalController;
import dev.david.huzzclient.feature.AutoToolController;
import dev.david.huzzclient.feature.BedrockHoleEspTracker;
import dev.david.huzzclient.feature.CrafterMacroController;
import dev.david.huzzclient.feature.ChatMacroController;
import dev.david.huzzclient.feature.DoubleClickController;
import dev.david.huzzclient.feature.DualArmAnimationController;
import dev.david.huzzclient.feature.FakeStatsController;
import dev.david.huzzclient.feature.FreeCamController;
import dev.david.huzzclient.feature.FreeLookController;
import dev.david.huzzclient.feature.FullBrightController;
import dev.david.huzzclient.feature.HoleEspTracker;
import dev.david.huzzclient.feature.ModuleHotkeyManager;
import dev.david.huzzclient.feature.PlayerNameController;
import dev.david.huzzclient.feature.PlayerNotifierController;
import dev.david.huzzclient.feature.SkinProtectController;
import dev.david.huzzclient.feature.SilentSetHomeController;
import dev.david.huzzclient.feature.StashFinderController;
import dev.david.huzzclient.feature.TimeChangerController;
import dev.david.huzzclient.feature.VisualAliasController;
import dev.david.huzzclient.render.ChunkHighlightRenderer;
import dev.david.huzzclient.render.FpsHudOverlay;
import dev.david.huzzclient.ui.HuzzConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HuzzClient implements ClientModInitializer {
    public static final String MOD_ID = "huzzclient";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final HuzzConfigManager CONFIG_MANAGER = new HuzzConfigManager();
    private static final BlockEspTracker BLOCK_ESP_TRACKER = new BlockEspTracker(CONFIG_MANAGER, BlockEspTracker.Mode.BLOCK);
    private static final BlockEspTracker STORAGE_ESP_TRACKER = new BlockEspTracker(CONFIG_MANAGER, BlockEspTracker.Mode.STORAGE);
    private static final StashFinderController STASH_FINDER_CONTROLLER = new StashFinderController(CONFIG_MANAGER);
    private static final BlockNotifierTracker BLOCK_NOTIFIER_TRACKER = new BlockNotifierTracker(CONFIG_MANAGER);
    private static final PlayerNotifierController PLAYER_NOTIFIER_CONTROLLER = new PlayerNotifierController(CONFIG_MANAGER);
    private static final SkinProtectController SKIN_PROTECT_CONTROLLER = new SkinProtectController(CONFIG_MANAGER);
    private static final FreeCamController FREE_CAM_CONTROLLER = new FreeCamController(CONFIG_MANAGER);
    private static final FreeLookController FREE_LOOK_CONTROLLER = new FreeLookController(CONFIG_MANAGER);
    private static final FullBrightController FULL_BRIGHT_CONTROLLER = new FullBrightController(CONFIG_MANAGER);
    private static final AutoTotemController AUTO_TOTEM_CONTROLLER = new AutoTotemController(CONFIG_MANAGER);
    private static final AutoRocketController AUTO_ROCKET_CONTROLLER = new AutoRocketController(CONFIG_MANAGER);
    private static final HoleEspTracker HOLE_ESP_TRACKER = new HoleEspTracker(CONFIG_MANAGER);
    private static final BedrockHoleEspTracker BEDROCK_HOLE_ESP_TRACKER = new BedrockHoleEspTracker(CONFIG_MANAGER);
    private static final AutoReconnectController AUTO_RECONNECT_CONTROLLER = new AutoReconnectController(CONFIG_MANAGER);
    private static final AutoDisconnectController AUTO_DISCONNECT_CONTROLLER = new AutoDisconnectController(CONFIG_MANAGER);
    private static final AutoReplenishController AUTO_REPLENISH_CONTROLLER = new AutoReplenishController(CONFIG_MANAGER);
    private static final AutoBridgeController AUTO_BRIDGE_CONTROLLER = new AutoBridgeController(CONFIG_MANAGER);
    private static final AimAssistController AIM_ASSIST_CONTROLLER = new AimAssistController(CONFIG_MANAGER);
    private static final AutoToolController AUTO_TOOL_CONTROLLER = new AutoToolController(CONFIG_MANAGER);
    private static final AutoCrystalController AUTO_CRYSTAL_CONTROLLER = new AutoCrystalController(CONFIG_MANAGER);
    private static final CrafterMacroController CRAFTER_MACRO_CONTROLLER = new CrafterMacroController(CONFIG_MANAGER);
    private static final ChatMacroController CHAT_MACRO_CONTROLLER = new ChatMacroController(CONFIG_MANAGER);
    private static final FakeStatsController FAKE_STATS_CONTROLLER = new FakeStatsController(CONFIG_MANAGER);
    private static final PlayerNameController PLAYER_NAME_CONTROLLER = new PlayerNameController(CONFIG_MANAGER);
    private static final ModuleHotkeyManager MODULE_HOTKEY_MANAGER = new ModuleHotkeyManager(CONFIG_MANAGER);
    private static final ChatCommandController CHAT_COMMAND_CONTROLLER = new ChatCommandController(CONFIG_MANAGER);
    private static final VisualAliasController VISUAL_ALIAS_CONTROLLER = new VisualAliasController(CONFIG_MANAGER);
    private static final TimeChangerController TIME_CHANGER_CONTROLLER = new TimeChangerController(CONFIG_MANAGER);
    private static final DoubleClickController DOUBLE_CLICK_CONTROLLER = new DoubleClickController(CONFIG_MANAGER);
    private static final SilentSetHomeController SILENT_SET_HOME_CONTROLLER = new SilentSetHomeController(CONFIG_MANAGER);
    private static final DualArmAnimationController DUAL_ARM_ANIMATION_CONTROLLER = new DualArmAnimationController(CONFIG_MANAGER);

    private static KeyBinding openConfigKey;

    @Override
    public void onInitializeClient() {
        CONFIG_MANAGER.load();

        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.huzzclient.open_config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_CONTROL,
            KeyBinding.Category.create(Identifier.of(MOD_ID, "controls"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            MODULE_HOTKEY_MANAGER.tick(client);
            FULL_BRIGHT_CONTROLLER.tick(client);
            FREE_LOOK_CONTROLLER.tick(client);
            FREE_CAM_CONTROLLER.tick(client);
            BLOCK_ESP_TRACKER.tick(client);
            STORAGE_ESP_TRACKER.tick(client);
            STASH_FINDER_CONTROLLER.tick(client);
            BLOCK_NOTIFIER_TRACKER.tick(client);
            PLAYER_NOTIFIER_CONTROLLER.tick(client);
            SKIN_PROTECT_CONTROLLER.tick(client);
            AUTO_TOTEM_CONTROLLER.tick(client);
            AUTO_ROCKET_CONTROLLER.tick(client);
            HOLE_ESP_TRACKER.tick(client);
            BEDROCK_HOLE_ESP_TRACKER.tick(client);
            AUTO_RECONNECT_CONTROLLER.tick(client);
            AUTO_DISCONNECT_CONTROLLER.tick(client);
            AUTO_REPLENISH_CONTROLLER.tick(client);
            AUTO_BRIDGE_CONTROLLER.tick(client);
            AUTO_TOOL_CONTROLLER.tick(client);
            AUTO_CRYSTAL_CONTROLLER.tick(client);
            CRAFTER_MACRO_CONTROLLER.tick(client);
            CHAT_MACRO_CONTROLLER.tick(client);
            FAKE_STATS_CONTROLLER.tick(client);
            VISUAL_ALIAS_CONTROLLER.tick(client);
            DOUBLE_CLICK_CONTROLLER.tick(client);
            SILENT_SET_HOME_CONTROLLER.tick(client);
            while (openConfigKey.wasPressed()) {
                client.setScreen(new HuzzConfigScreen(client.currentScreen));
            }
        });

        WorldRenderEvents.END_MAIN.register(ChunkHighlightRenderer::render);
        HudRenderCallback.EVENT.register(FpsHudOverlay::render);
    }

    public static HuzzConfigManager getConfigManager() {
        return CONFIG_MANAGER;
    }

    public static BlockEspTracker getBlockEspTracker() {
        return BLOCK_ESP_TRACKER;
    }

    public static BlockEspTracker getStorageEspTracker() {
        return STORAGE_ESP_TRACKER;
    }

    public static StashFinderController getStashFinderController() {
        return STASH_FINDER_CONTROLLER;
    }

    public static BlockNotifierTracker getBlockNotifierTracker() {
        return BLOCK_NOTIFIER_TRACKER;
    }

    public static PlayerNotifierController getPlayerNotifierController() {
        return PLAYER_NOTIFIER_CONTROLLER;
    }

    public static SkinProtectController getSkinProtectController() {
        return SKIN_PROTECT_CONTROLLER;
    }

    public static FreeCamController getFreeCamController() {
        return FREE_CAM_CONTROLLER;
    }

    public static FreeLookController getFreeLookController() {
        return FREE_LOOK_CONTROLLER;
    }

    public static FullBrightController getFullBrightController() {
        return FULL_BRIGHT_CONTROLLER;
    }

    public static AutoTotemController getAutoTotemController() {
        return AUTO_TOTEM_CONTROLLER;
    }

    public static AutoRocketController getAutoRocketController() {
        return AUTO_ROCKET_CONTROLLER;
    }

    public static HoleEspTracker getHoleEspTracker() {
        return HOLE_ESP_TRACKER;
    }

    public static BedrockHoleEspTracker getBedrockHoleEspTracker() {
        return BEDROCK_HOLE_ESP_TRACKER;
    }

    public static AutoReconnectController getAutoReconnectController() {
        return AUTO_RECONNECT_CONTROLLER;
    }

    public static AutoDisconnectController getAutoDisconnectController() {
        return AUTO_DISCONNECT_CONTROLLER;
    }

    public static AutoReplenishController getAutoReplenishController() {
        return AUTO_REPLENISH_CONTROLLER;
    }

    public static AutoBridgeController getAutoBridgeController() {
        return AUTO_BRIDGE_CONTROLLER;
    }

    public static AimAssistController getAimAssistController() {
        return AIM_ASSIST_CONTROLLER;
    }

    public static AutoToolController getAutoToolController() {
        return AUTO_TOOL_CONTROLLER;
    }

    public static AutoCrystalController getAutoCrystalController() {
        return AUTO_CRYSTAL_CONTROLLER;
    }

    public static CrafterMacroController getCrafterMacroController() {
        return CRAFTER_MACRO_CONTROLLER;
    }

    public static ChatMacroController getChatMacroController() {
        return CHAT_MACRO_CONTROLLER;
    }

    public static FakeStatsController getFakeStatsController() {
        return FAKE_STATS_CONTROLLER;
    }

    public static PlayerNameController getPlayerNameController() {
        return PLAYER_NAME_CONTROLLER;
    }

    public static ChatCommandController getChatCommandController() {
        return CHAT_COMMAND_CONTROLLER;
    }

    public static VisualAliasController getVisualAliasController() {
        return VISUAL_ALIAS_CONTROLLER;
    }

    public static DoubleClickController getDoubleClickController() {
        return DOUBLE_CLICK_CONTROLLER;
    }

    public static TimeChangerController getTimeChangerController() {
        return TIME_CHANGER_CONTROLLER;
    }

    public static SilentSetHomeController getSilentSetHomeController() {
        return SILENT_SET_HOME_CONTROLLER;
    }

    public static DualArmAnimationController getDualArmAnimationController() {
        return DUAL_ARM_ANIMATION_CONTROLLER;
    }

    public static void clearRuntimeState(MinecraftClient client) {
        BLOCK_ESP_TRACKER.clear();
        STORAGE_ESP_TRACKER.clear();
        STASH_FINDER_CONTROLLER.clear();
        BLOCK_NOTIFIER_TRACKER.clear();
        PLAYER_NOTIFIER_CONTROLLER.clear();
        SKIN_PROTECT_CONTROLLER.clear();
        FREE_CAM_CONTROLLER.clear();
        FREE_LOOK_CONTROLLER.clear();
        AUTO_TOTEM_CONTROLLER.clear(client);
        AUTO_ROCKET_CONTROLLER.clear();
        HOLE_ESP_TRACKER.clear();
        BEDROCK_HOLE_ESP_TRACKER.clear();
        AUTO_RECONNECT_CONTROLLER.clear();
        AUTO_DISCONNECT_CONTROLLER.clear();
        AUTO_REPLENISH_CONTROLLER.clear();
        AUTO_BRIDGE_CONTROLLER.clear(client);
        AUTO_CRYSTAL_CONTROLLER.clear();
        CRAFTER_MACRO_CONTROLLER.clear();
        FAKE_STATS_CONTROLLER.clear();
        DOUBLE_CLICK_CONTROLLER.clear();
        SILENT_SET_HOME_CONTROLLER.clear();
    }

    public static void handleGameJoin(GameJoinS2CPacket packet) {
        clearRuntimeState(client());
    }

    public static void handleChunkDeltaUpdate(ChunkDeltaUpdateS2CPacket packet) {
        BLOCK_ESP_TRACKER.recordChunkDelta(packet);
        STORAGE_ESP_TRACKER.recordChunkDelta(packet);
    }

    public static void handleChunkData(ChunkDataS2CPacket packet) {
        MinecraftClient client = client();
        BLOCK_ESP_TRACKER.recordChunkData(client, packet);
        STORAGE_ESP_TRACKER.recordChunkData(client, packet);
    }

    public static void handleBlockUpdate(BlockUpdateS2CPacket packet) {
        BLOCK_ESP_TRACKER.recordBlockUpdate(packet);
        STORAGE_ESP_TRACKER.recordBlockUpdate(packet);
    }

    public static MinecraftClient client() {
        return MinecraftClient.getInstance();
    }
}
