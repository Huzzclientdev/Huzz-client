package dev.david.huzzclient.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
            HuzzConfig loaded = GSON.fromJson(reader, HuzzConfig.class);
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
                config.setSkinProtectEnabled(config.isSkinProtectEnabled());
                config.setSwingSpeedEnabled(config.isSwingSpeedEnabled());
                config.setFreeCamEnabled(config.isFreeCamEnabled());
                config.setFreeLookEnabled(config.isFreeLookEnabled());
                config.setCrafterMacroEnabled(config.isCrafterMacroEnabled());
                config.setCrafterMacroMask(config.getCrafterMacroMask());
                config.setChatMacroEnabled(config.isChatMacroEnabled());
                config.setAutoRocketEnabled(config.isAutoRocketEnabled());
                config.setHoleEspEnabled(config.isHoleEspEnabled());
                config.setAutoReconnectEnabled(config.isAutoReconnectEnabled());
                config.setAutoDisconnectEnabled(config.isAutoDisconnectEnabled());
                config.setAutoReplenishEnabled(config.isAutoReplenishEnabled());
                config.setAutoBridgeEnabled(config.isAutoBridgeEnabled());
                config.setFastUseEnabled(config.isFastUseEnabled());
                config.setFullBrightEnabled(config.isFullBrightEnabled());
                config.setFullBrightFade(config.isFullBrightFade());
                config.setFpsHudEnabled(config.isFpsHudEnabled());
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
                config.setFullBrightMethod(config.getFullBrightMethod());
                config.setTimeDebugMode(config.getTimeDebugMode());
                config.setRescanRateMs(config.getRescanRateMs());
                config.setDeltaPacketThreshold(config.getDeltaPacketThreshold());
                config.setBlockEspRangeBlocks(config.getBlockEspRangeBlocks());
                config.setStorageEspRangeBlocks(config.getStorageEspRangeBlocks());
                config.setStashFinderThreshold(config.getStashFinderThreshold());
                config.setHoleEspRangeBlocks(config.getHoleEspRangeBlocks());
                config.setAutoReconnectDelayMs(config.getAutoReconnectDelayMs());
        config.setTimeDebugMarkDelayMs(config.getTimeDebugMarkDelayMs());
                config.setFreeCamSpeed(config.getFreeCamSpeed());
                config.setFastUseCooldownTicks(config.getFastUseCooldownTicks());
                config.setRenderHeight(config.getRenderHeight());
                config.setFullBrightDefaultGamma(config.getFullBrightDefaultGamma());
                config.setSwingSpeedMultiplier(config.getSwingSpeedMultiplier());
                config.setAutoReconnectAddress(config.getAutoReconnectAddress());
                config.setNameProtectName(config.getNameProtectName());
                config.setSkinProtectName(config.getSkinProtectName());
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
        } catch (IOException exception) {
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
}
