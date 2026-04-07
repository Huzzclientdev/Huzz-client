package dev.david.huzzclient;

import dev.david.huzzclient.config.HuzzConfigManager;
import dev.david.huzzclient.detector.ChunkPacketTracker;
import dev.david.huzzclient.detector.ObservedActivityTimeTracker;
import dev.david.huzzclient.feature.BlockEspTracker;
import dev.david.huzzclient.feature.BlockNotifierTracker;
import dev.david.huzzclient.feature.ChatCommandController;
import dev.david.huzzclient.feature.AutoTotemController;
import dev.david.huzzclient.feature.AutoDisconnectController;
import dev.david.huzzclient.feature.AutoReconnectController;
import dev.david.huzzclient.feature.AutoRocketController;
import dev.david.huzzclient.feature.CrafterMacroController;
import dev.david.huzzclient.feature.ChatMacroController;
import dev.david.huzzclient.feature.FakeStatsController;
import dev.david.huzzclient.feature.FreeCamController;
import dev.david.huzzclient.feature.FreeLookController;
import dev.david.huzzclient.feature.FullBrightController;
import dev.david.huzzclient.feature.HoleEspTracker;
import dev.david.huzzclient.feature.ModuleHotkeyManager;
import dev.david.huzzclient.feature.PlayerNameController;
import dev.david.huzzclient.feature.PlayerNotifierController;
import dev.david.huzzclient.feature.PrimeChunkFinderTracker;
import dev.david.huzzclient.feature.SkinProtectController;
import dev.david.huzzclient.feature.StashFinderController;
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
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HuzzClient implements ClientModInitializer {
    public static final String MOD_ID = "huzzclient";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final HuzzConfigManager CONFIG_MANAGER = new HuzzConfigManager();
    private static final ChunkPacketTracker CHUNK_FINDER_TRACKER = new ChunkPacketTracker(CONFIG_MANAGER);
    private static final ObservedActivityTimeTracker TIME_DEBUG_TRACKER = new ObservedActivityTimeTracker(CONFIG_MANAGER);
    private static final PrimeChunkFinderTracker PRIME_CHUNK_FINDER_TRACKER = new PrimeChunkFinderTracker(CONFIG_MANAGER);
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
    private static final AutoReconnectController AUTO_RECONNECT_CONTROLLER = new AutoReconnectController(CONFIG_MANAGER);
    private static final AutoDisconnectController AUTO_DISCONNECT_CONTROLLER = new AutoDisconnectController(CONFIG_MANAGER);
    private static final CrafterMacroController CRAFTER_MACRO_CONTROLLER = new CrafterMacroController(CONFIG_MANAGER);
    private static final ChatMacroController CHAT_MACRO_CONTROLLER = new ChatMacroController(CONFIG_MANAGER);
    private static final FakeStatsController FAKE_STATS_CONTROLLER = new FakeStatsController(CONFIG_MANAGER);
    private static final PlayerNameController PLAYER_NAME_CONTROLLER = new PlayerNameController(CONFIG_MANAGER);
    private static final ModuleHotkeyManager MODULE_HOTKEY_MANAGER = new ModuleHotkeyManager(CONFIG_MANAGER);
    private static final ChatCommandController CHAT_COMMAND_CONTROLLER = new ChatCommandController(CONFIG_MANAGER);

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
            CHUNK_FINDER_TRACKER.tick(client);
            TIME_DEBUG_TRACKER.tick(client);
            PRIME_CHUNK_FINDER_TRACKER.tick(client);
            BLOCK_ESP_TRACKER.tick(client);
            STORAGE_ESP_TRACKER.tick(client);
            STASH_FINDER_CONTROLLER.tick(client);
            BLOCK_NOTIFIER_TRACKER.tick(client);
            PLAYER_NOTIFIER_CONTROLLER.tick(client);
            SKIN_PROTECT_CONTROLLER.tick(client);
            AUTO_TOTEM_CONTROLLER.tick(client);
            AUTO_ROCKET_CONTROLLER.tick(client);
            HOLE_ESP_TRACKER.tick(client);
            AUTO_RECONNECT_CONTROLLER.tick(client);
            AUTO_DISCONNECT_CONTROLLER.tick(client);
            CRAFTER_MACRO_CONTROLLER.tick(client);
            CHAT_MACRO_CONTROLLER.tick(client);
            FAKE_STATS_CONTROLLER.tick(client);

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

    public static ChunkPacketTracker getTracker() {
        return CHUNK_FINDER_TRACKER;
    }

    public static ObservedActivityTimeTracker getTimeDebugTracker() {
        return TIME_DEBUG_TRACKER;
    }

    public static PrimeChunkFinderTracker getPrimeChunkFinderTracker() {
        return PRIME_CHUNK_FINDER_TRACKER;
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

    public static AutoReconnectController getAutoReconnectController() {
        return AUTO_RECONNECT_CONTROLLER;
    }

    public static AutoDisconnectController getAutoDisconnectController() {
        return AUTO_DISCONNECT_CONTROLLER;
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

    public static void clearRuntimeState(MinecraftClient client) {
        CHUNK_FINDER_TRACKER.clear();
        TIME_DEBUG_TRACKER.clear();
        PRIME_CHUNK_FINDER_TRACKER.clear();
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
        AUTO_RECONNECT_CONTROLLER.clear();
        AUTO_DISCONNECT_CONTROLLER.clear();
        CRAFTER_MACRO_CONTROLLER.clear();
        FAKE_STATS_CONTROLLER.clear();
    }

    public static void handleGameJoin(GameJoinS2CPacket packet) {
        clearRuntimeState(client());
    }

    public static void handleChunkDeltaUpdate(ChunkDeltaUpdateS2CPacket packet) {
        CHUNK_FINDER_TRACKER.recordChunkDelta(packet);
        TIME_DEBUG_TRACKER.recordChunkDelta(packet);
        PRIME_CHUNK_FINDER_TRACKER.recordChunkDelta(packet);
    }

    public static void handleChunkData(ChunkDataS2CPacket packet) {
        CHUNK_FINDER_TRACKER.recordChunkData(packet);
        TIME_DEBUG_TRACKER.recordChunkData(packet);
        PRIME_CHUNK_FINDER_TRACKER.recordChunkData(packet);
    }

    public static void handleLightUpdate(LightUpdateS2CPacket packet) {
        CHUNK_FINDER_TRACKER.recordLightUpdate(packet);
        TIME_DEBUG_TRACKER.recordLightUpdate(packet);
        PRIME_CHUNK_FINDER_TRACKER.recordLightUpdate(packet);
    }

    public static MinecraftClient client() {
        return MinecraftClient.getInstance();
    }
}
