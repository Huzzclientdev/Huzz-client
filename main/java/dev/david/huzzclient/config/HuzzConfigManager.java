package dev.david.huzzclient.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.david.huzzclient.HuzzClient;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HuzzConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path configPath = FabricLoader.getInstance()
        .getConfigDir()
        .resolve("ultimate-chunk-finder.json");

    private HuzzConfig config = new HuzzConfig();

    public HuzzConfig getConfig() {
        return config;
    }

    public void load() {
        if (Files.notExists(configPath)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            JsonElement root = JsonParser.parseReader(reader);
            sanitizeConfigJson(root);
            HuzzConfig loaded = GSON.fromJson(root, HuzzConfig.class);
            if (loaded != null) {
                config = loaded;
                config.setChunkFinderEnabled(config.isChunkFinderEnabled());
                config.setTimeDebugEnabled(config.isTimeDebugEnabled());
                config.setPrimeChunkFinderEnabled(config.isPrimeChunkFinderEnabled());
                config.setBlockEspEnabled(config.isBlockEspEnabled());
                config.setBlockEspTracers(config.isBlockEspTracers());
                config.setStorageEspEnabled(config.isStorageEspEnabled());
                config.setStorageEspTracers(config.isStorageEspTracers());
                config.setStashFinderEnabled(config.isStashFinderEnabled());
                config.setBlockNotifierEnabled(config.isBlockNotifierEnabled());
                config.setPlayerNotifierEnabled(config.isPlayerNotifierEnabled());
                config.setPlayerEspEnabled(config.isPlayerEspEnabled());
                config.setMobEspEnabled(config.isMobEspEnabled());
                config.setMobEspTracers(config.isMobEspTracers());
                config.setSkinProtectEnabled(config.isSkinProtectEnabled());
                config.setSwingSpeedEnabled(config.isSwingSpeedEnabled());
                config.setFreeCamEnabled(config.isFreeCamEnabled());
                config.setFreeLookEnabled(config.isFreeLookEnabled());
                config.setCrafterMacroEnabled(config.isCrafterMacroEnabled());
                config.setCrafterMacroMask(config.getCrafterMacroMask());
                config.setChatMacroEnabled(config.isChatMacroEnabled());
                config.setAutoRocketEnabled(config.isAutoRocketEnabled());
                config.setHoleEspEnabled(config.isHoleEspEnabled());
                config.setBedrockHoleEspEnabled(config.isBedrockHoleEspEnabled());
                config.setAutoReconnectEnabled(config.isAutoReconnectEnabled());
                config.setAutoDisconnectEnabled(config.isAutoDisconnectEnabled());
                config.setAutoReplenishEnabled(config.isAutoReplenishEnabled());
                config.setAutoBridgeEnabled(config.isAutoBridgeEnabled());
                config.setFastUseEnabled(config.isFastUseEnabled());
                config.setFullBrightEnabled(config.isFullBrightEnabled());
                config.setFullBrightFade(config.isFullBrightFade());
                config.setHudEnabled(config.isHudEnabled());
                config.setHudFpsEnabled(config.isHudFpsEnabled());
                config.setHudMinimapEnabled(config.isHudMinimapEnabled());
                config.setHudClockEnabled(config.isHudClockEnabled());
                config.setHudArmorEnabled(config.isHudArmorEnabled());
                config.setHudPotionEffectsEnabled(config.isHudPotionEffectsEnabled());
                config.setHudMusicEnabled(config.isHudMusicEnabled());
                config.setHudMusicLargeUi(config.isHudMusicLargeUi());
                config.setHudMusicThemeEnabled(config.isHudMusicThemeEnabled());
                config.setCustomFovEnabled(config.isCustomFovEnabled());
                config.setNoRenderEnabled(config.isNoRenderEnabled());
                config.setTimeChangerEnabled(config.isTimeChangerEnabled());
                config.setNoRenderWeather(config.isNoRenderWeather());
                config.setNoRenderParticles(config.isNoRenderParticles());
                config.setNoRenderOverworldFog(config.isNoRenderOverworldFog());
                config.setNoRenderChunkCulling(config.isNoRenderChunkCulling());
                config.setBreakProgressEnabled(config.isBreakProgressEnabled());
        config.setItemNametagsEnabled(config.isItemNametagsEnabled());
                config.setFakeMediaEnabled(config.isFakeMediaEnabled());
                config.setFakePayEnabled(config.isFakePayEnabled());
                config.setFakePayUppercaseSuffix(config.isFakePayUppercaseSuffix());
                config.setFakeStatsEnabled(config.isFakeStatsEnabled());
                config.setFakeStatsMode(config.getFakeStatsMode());
                config.setFakeStatsTitle(config.getFakeStatsTitle());
                config.setFakeStatsMoney(config.getFakeStatsMoney());
                config.setFakeStatsShards(config.getFakeStatsShards());
                config.setFakeStatsKills(config.getFakeStatsKills());
                config.setFakeStatsDeaths(config.getFakeStatsDeaths());
                config.setFakeStatsKeyall(config.getFakeStatsKeyall());
                config.setFakeStatsPlaytime(config.getFakeStatsPlaytime());
                config.setFakeStatsTeam(config.getFakeStatsTeam());
                config.setFakeStatsRegion(config.getFakeStatsRegion());
                config.setAutoTotemEnabled(config.isAutoTotemEnabled());
                config.setAutoTotemDelayMs(config.getAutoTotemDelayMs());
                config.setAutoRocketDelayMs(config.getAutoRocketDelayMs());
                config.setNameProtectEnabled(config.isNameProtectEnabled());
                config.setFakeSpawnerEnabled(config.isFakeSpawnerEnabled());
                config.setFakeElytraEnabled(config.isFakeElytraEnabled());
                config.setSilentSetHomeEnabled(config.isSilentSetHomeEnabled());
                config.setDoubleClickEnabled(config.isDoubleClickEnabled());
                config.setDualArmAnimationEnabled(config.isDualArmAnimationEnabled());
                config.setGoonEnabled(config.isGoonEnabled());
                config.setFullBrightMethod(config.getFullBrightMethod());
                config.setMusicHudSource(config.getMusicHudSource());
                config.setTimeDebugMode(config.getTimeDebugMode());
                config.setRescanRateMs(config.getRescanRateMs());
                config.setDeltaPacketThreshold(config.getDeltaPacketThreshold());
                config.setBlockEspRangeBlocks(config.getBlockEspRangeBlocks());
                config.setStorageEspRangeBlocks(config.getStorageEspRangeBlocks());
                config.setStashFinderThreshold(config.getStashFinderThreshold());
                config.setHoleEspRangeBlocks(config.getHoleEspRangeBlocks());
                config.setAutoReconnectDelayMs(config.getAutoReconnectDelayMs());
                config.setDoubleClickDelayMs(config.getDoubleClickDelayMs());
        config.setTimeDebugMarkDelayMs(config.getTimeDebugMarkDelayMs());
                config.setFreeCamSpeed(config.getFreeCamSpeed());
                config.setFastUseCooldownTicks(config.getFastUseCooldownTicks());
                config.setRenderHeight(config.getRenderHeight());
                config.setCustomFovDegrees(config.getCustomFovDegrees());
                config.setTimeChangerValue(config.getTimeChangerValue());
                config.setFullBrightDefaultGamma(config.getFullBrightDefaultGamma());
                config.setSwingSpeedMultiplier(config.getSwingSpeedMultiplier());
                config.setAutoReconnectAddress(config.getAutoReconnectAddress());
                config.setNameProtectName(config.getNameProtectName());
                config.setSkinProtectName(config.getSkinProtectName());
                config.setFakeSpawnerBlockId(config.getFakeSpawnerBlockId());
                config.setFakeElytraItemId(config.getFakeElytraItemId());
                config.setChunkFinderKeyCode(config.getChunkFinderKeyCode());
                config.setTimeDebugKeyCode(config.getTimeDebugKeyCode());
                config.setBlockEspKeyCode(config.getBlockEspKeyCode());
                config.setStorageEspKeyCode(config.getStorageEspKeyCode());
                config.setFreeCamKeyCode(config.getFreeCamKeyCode());
                config.setFreeLookKeyCode(config.getFreeLookKeyCode());
                config.setFullBrightKeyCode(config.getFullBrightKeyCode());
                config.setItemNametagsKeyCode(config.getItemNametagsKeyCode());
                for (HuzzConfig.ModuleKey moduleKey : HuzzConfig.ModuleKey.values()) {
                    config.setModuleKeyCode(moduleKey, config.getModuleKeyCode(moduleKey));
                }
                config.setBlockEspTargets(config.getBlockEspTargets());
                config.setStorageEspTargets(config.getStorageEspTargets());
                config.setBlockNotifierTargets(config.getBlockNotifierTargets());
                config.setChatMacros(config.getChatMacros());
            }
        } catch (IOException | RuntimeException exception) {
            HuzzClient.LOGGER.error("Failed to load config from {}", configPath, exception);
        }
    }

    public void save() {
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException exception) {
            HuzzClient.LOGGER.error("Failed to save config to {}", configPath, exception);
        }
    }

    private static void sanitizeConfigJson(JsonElement root) {
        if (!(root instanceof JsonObject rootObject)) {
            return;
        }

        JsonElement moduleKeyCodesElement = rootObject.get("moduleKeyCodes");
        if (!(moduleKeyCodesElement instanceof JsonObject moduleKeyCodesObject)) {
            return;
        }

        moduleKeyCodesObject.entrySet().removeIf(entry -> !isKnownModuleKey(entry.getKey()));
    }

    private static boolean isKnownModuleKey(String key) {
        if (key == null) {
            return false;
        }

        try {
            HuzzConfig.ModuleKey.valueOf(key);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
