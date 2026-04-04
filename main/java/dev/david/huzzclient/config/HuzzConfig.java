package dev.david.huzzclient.config;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public final class HuzzConfig {
    public static final int NO_KEY = -1;

    public static final int MIN_RESCAN_RATE_MS = 250;
    public static final int MAX_RESCAN_RATE_MS = 5000;
    public static final int DEFAULT_RESCAN_RATE_MS = 750;

    public static final int MIN_DELTA_THRESHOLD = 1;
    public static final int MAX_DELTA_THRESHOLD = 30;
    public static final int DEFAULT_DELTA_THRESHOLD = 6;

    public static final int MIN_RENDER_HEIGHT = -64;
    public static final int MAX_RENDER_HEIGHT = 320;
    public static final int DEFAULT_RENDER_HEIGHT = 66;

    public static final int MIN_ESP_RANGE_BLOCKS = 16;
    public static final int MAX_ESP_RANGE_BLOCKS = 160;
    public static final int DEFAULT_BLOCK_ESP_RANGE_BLOCKS = 48;
    public static final int DEFAULT_STORAGE_ESP_RANGE_BLOCKS = 64;

    public static final int MIN_TIME_DEBUG_MARK_DELAY_MS = 2000;
    public static final int MAX_TIME_DEBUG_MARK_DELAY_MS = 60000;
    public static final int DEFAULT_TIME_DEBUG_MARK_DELAY_MS = 12000;

    public static final int MIN_FREE_CAM_SPEED = 1;
    public static final int MAX_FREE_CAM_SPEED = 40;
    public static final int DEFAULT_FREE_CAM_SPEED = 10;
    public static final int MIN_AUTO_TOTEM_DELAY_MS = 50;
    public static final int MAX_AUTO_TOTEM_DELAY_MS = 2000;
    public static final int DEFAULT_AUTO_TOTEM_DELAY_MS = 300;
    public static final int MIN_FAST_USE_COOLDOWN_TICKS = 0;
    public static final int MAX_FAST_USE_COOLDOWN_TICKS = 4;
    public static final int DEFAULT_FAST_USE_COOLDOWN_TICKS = 1;
    public static final int MIN_STASH_FINDER_THRESHOLD = 2;
    public static final int MAX_STASH_FINDER_THRESHOLD = 128;
    public static final int DEFAULT_STASH_FINDER_THRESHOLD = 20;
    public static final double MIN_FULL_BRIGHT_DEFAULT_GAMMA = 0.0D;
    public static final double MAX_FULL_BRIGHT_DEFAULT_GAMMA = 1.0D;
    public static final double DEFAULT_FULL_BRIGHT_DEFAULT_GAMMA = 0.5D;

    private boolean chunkFinderEnabled = true;
    private boolean timeDebugEnabled = true;
    private boolean primeChunkFinderEnabled;
    private boolean blockEspEnabled;
    private boolean blockEspTracers = true;
    private boolean storageEspEnabled;
    private boolean storageEspTracers = true;
    private boolean stashFinderEnabled;
    private boolean blockNotifierEnabled;
    private boolean playerNotifierEnabled;
    private boolean playerEspEnabled;
    private boolean freeCamEnabled;
    private boolean freeLookEnabled;
    private boolean crafterMacroEnabled;
    private boolean fastUseEnabled;
    private boolean fullBrightEnabled;
    private boolean fullBrightFade = true;
    private boolean fpsHudEnabled = true;
    private boolean itemNametagsEnabled;
    private boolean fakeMediaEnabled;
    private boolean fakePayEnabled;
    private boolean fakePayUppercaseSuffix = true;
    private boolean fakeStatsEnabled;
    private boolean autoTotemEnabled;
    private boolean nameProtectEnabled;
    private int crafterMacroMask = 0x1FF;
    private int autoTotemDelayMs = DEFAULT_AUTO_TOTEM_DELAY_MS;
    private FakeStatsMode fakeStatsMode = FakeStatsMode.EDIT_ALL;
    private String fakeStatsTitle = "Donut SMP";
    private String fakeStatsMoney = "872.94K";
    private String fakeStatsShards = "485";
    private String fakeStatsKills = "21";
    private String fakeStatsDeaths = "50";
    private String fakeStatsKeyall = "20m 5s";
    private String fakeStatsPlaytime = "18d 11h";
    private String fakeStatsTeam = "Effendylepstien";
    private String fakeStatsRegion = "NA East (91ms)";
    private FullBrightMethod fullBrightMethod = FullBrightMethod.GAMMA;
    private TimeDebugMode timeDebugMode = TimeDebugMode.CHUNK_FINDER;
    private int rescanRateMs = DEFAULT_RESCAN_RATE_MS;
    private int deltaPacketThreshold = DEFAULT_DELTA_THRESHOLD;
    private int blockEspRangeBlocks = DEFAULT_BLOCK_ESP_RANGE_BLOCKS;
    private int storageEspRangeBlocks = DEFAULT_STORAGE_ESP_RANGE_BLOCKS;
    private int stashFinderThreshold = DEFAULT_STASH_FINDER_THRESHOLD;
    private int timeDebugMarkDelayMs = DEFAULT_TIME_DEBUG_MARK_DELAY_MS;
    private int freeCamSpeed = DEFAULT_FREE_CAM_SPEED;
    private int fastUseCooldownTicks = DEFAULT_FAST_USE_COOLDOWN_TICKS;
    private int renderHeight = DEFAULT_RENDER_HEIGHT;
    private double fullBrightDefaultGamma = DEFAULT_FULL_BRIGHT_DEFAULT_GAMMA;
    private int chunkFinderKeyCode = NO_KEY;
    private int timeDebugKeyCode = NO_KEY;
    private int blockEspKeyCode = NO_KEY;
    private int storageEspKeyCode = NO_KEY;
    private int freeCamKeyCode = NO_KEY;
    private int freeLookKeyCode = NO_KEY;
    private int fullBrightKeyCode = NO_KEY;
    private int itemNametagsKeyCode = NO_KEY;
    private String nameProtectName = "Protected";
    private List<String> blockEspTargets = new ArrayList<>(List.of(
        "diamond_ore",
        "deepslate_diamond_ore",
        "ancient_debris"
    ));
    private List<String> storageEspTargets = new ArrayList<>(List.of(
        "chest",
        "trapped_chest",
        "barrel",
        "ender_chest",
        "hopper",
        "dispenser",
        "dropper",
        "furnace",
        "blast_furnace",
        "smoker",
        "shulker_box"
    ));
    private List<String> blockNotifierTargets = new ArrayList<>();

    public boolean isChunkFinderEnabled() {
        return chunkFinderEnabled;
    }

    public void setChunkFinderEnabled(boolean chunkFinderEnabled) {
        this.chunkFinderEnabled = chunkFinderEnabled;
    }

    public boolean isTimeDebugEnabled() {
        return timeDebugEnabled;
    }

    public void setTimeDebugEnabled(boolean timeDebugEnabled) {
        this.timeDebugEnabled = timeDebugEnabled;
    }

    public boolean isPrimeChunkFinderEnabled() {
        return primeChunkFinderEnabled;
    }

    public void setPrimeChunkFinderEnabled(boolean primeChunkFinderEnabled) {
        this.primeChunkFinderEnabled = primeChunkFinderEnabled;
    }

    public boolean isBlockEspEnabled() {
        return blockEspEnabled;
    }

    public void setBlockEspEnabled(boolean blockEspEnabled) {
        this.blockEspEnabled = blockEspEnabled;
    }

    public boolean isBlockEspTracers() {
        return blockEspTracers;
    }

    public void setBlockEspTracers(boolean blockEspTracers) {
        this.blockEspTracers = blockEspTracers;
    }

    public boolean isStorageEspEnabled() {
        return storageEspEnabled;
    }

    public void setStorageEspEnabled(boolean storageEspEnabled) {
        this.storageEspEnabled = storageEspEnabled;
    }

    public boolean isStorageEspTracers() {
        return storageEspTracers;
    }

    public void setStorageEspTracers(boolean storageEspTracers) {
        this.storageEspTracers = storageEspTracers;
    }

    public boolean isStashFinderEnabled() {
        return stashFinderEnabled;
    }

    public void setStashFinderEnabled(boolean stashFinderEnabled) {
        this.stashFinderEnabled = stashFinderEnabled;
    }

    public boolean isBlockNotifierEnabled() {
        return blockNotifierEnabled;
    }

    public void setBlockNotifierEnabled(boolean blockNotifierEnabled) {
        this.blockNotifierEnabled = blockNotifierEnabled;
    }

    public boolean isPlayerNotifierEnabled() {
        return playerNotifierEnabled;
    }

    public void setPlayerNotifierEnabled(boolean playerNotifierEnabled) {
        this.playerNotifierEnabled = playerNotifierEnabled;
    }

    public boolean isPlayerEspEnabled() {
        return playerEspEnabled;
    }

    public void setPlayerEspEnabled(boolean playerEspEnabled) {
        this.playerEspEnabled = playerEspEnabled;
    }

    public boolean isFreeCamEnabled() {
        return freeCamEnabled;
    }

    public void setFreeCamEnabled(boolean freeCamEnabled) {
        this.freeCamEnabled = freeCamEnabled;
    }

    public boolean isCrafterMacroEnabled() {
        return crafterMacroEnabled;
    }

    public void setCrafterMacroEnabled(boolean crafterMacroEnabled) {
        this.crafterMacroEnabled = crafterMacroEnabled;
    }

    public boolean isFastUseEnabled() {
        return fastUseEnabled;
    }

    public void setFastUseEnabled(boolean fastUseEnabled) {
        this.fastUseEnabled = fastUseEnabled;
    }

    public boolean isFullBrightEnabled() {
        return fullBrightEnabled;
    }

    public boolean isFreeLookEnabled() {
        return freeLookEnabled;
    }

    public void setFreeLookEnabled(boolean freeLookEnabled) {
        this.freeLookEnabled = freeLookEnabled;
    }

    public void setFullBrightEnabled(boolean fullBrightEnabled) {
        this.fullBrightEnabled = fullBrightEnabled;
    }

    public boolean isFullBrightFade() {
        return fullBrightFade;
    }

    public void setFullBrightFade(boolean fullBrightFade) {
        this.fullBrightFade = fullBrightFade;
    }

    public FullBrightMethod getFullBrightMethod() {
        return fullBrightMethod;
    }

    public void setFullBrightMethod(FullBrightMethod fullBrightMethod) {
        this.fullBrightMethod = fullBrightMethod == null ? FullBrightMethod.GAMMA : fullBrightMethod;
    }

    public boolean isItemNametagsEnabled() {
        return itemNametagsEnabled;
    }

    public void setItemNametagsEnabled(boolean itemNametagsEnabled) {
        this.itemNametagsEnabled = itemNametagsEnabled;
    }

    public boolean isFpsHudEnabled() {
        return fpsHudEnabled;
    }

    public void setFpsHudEnabled(boolean fpsHudEnabled) {
        this.fpsHudEnabled = fpsHudEnabled;
    }

    public boolean isFakeMediaEnabled() {
        return fakeMediaEnabled;
    }

    public void setFakeMediaEnabled(boolean fakeMediaEnabled) {
        this.fakeMediaEnabled = fakeMediaEnabled;
    }

    public boolean isFakePayEnabled() {
        return fakePayEnabled;
    }

    public void setFakePayEnabled(boolean fakePayEnabled) {
        this.fakePayEnabled = fakePayEnabled;
    }

    public boolean isFakePayUppercaseSuffix() {
        return fakePayUppercaseSuffix;
    }

    public void setFakePayUppercaseSuffix(boolean fakePayUppercaseSuffix) {
        this.fakePayUppercaseSuffix = fakePayUppercaseSuffix;
    }

    public boolean isFakeStatsEnabled() {
        return fakeStatsEnabled;
    }

    public void setFakeStatsEnabled(boolean fakeStatsEnabled) {
        this.fakeStatsEnabled = fakeStatsEnabled;
    }

    public FakeStatsMode getFakeStatsMode() {
        return fakeStatsMode;
    }

    public void setFakeStatsMode(FakeStatsMode fakeStatsMode) {
        this.fakeStatsMode = fakeStatsMode == null ? FakeStatsMode.EDIT_ALL : fakeStatsMode;
    }

    public boolean isAutoTotemEnabled() {
        return autoTotemEnabled;
    }

    public void setAutoTotemEnabled(boolean autoTotemEnabled) {
        this.autoTotemEnabled = autoTotemEnabled;
    }

    public int getAutoTotemDelayMs() {
        return autoTotemDelayMs;
    }

    public void setAutoTotemDelayMs(int autoTotemDelayMs) {
        this.autoTotemDelayMs = clamp(autoTotemDelayMs, MIN_AUTO_TOTEM_DELAY_MS, MAX_AUTO_TOTEM_DELAY_MS);
    }

    public boolean isNameProtectEnabled() {
        return nameProtectEnabled;
    }

    public void setNameProtectEnabled(boolean nameProtectEnabled) {
        this.nameProtectEnabled = nameProtectEnabled;
    }

    public TimeDebugMode getTimeDebugMode() {
        return timeDebugMode;
    }

    public void setTimeDebugMode(TimeDebugMode timeDebugMode) {
        this.timeDebugMode = timeDebugMode == null ? TimeDebugMode.CHUNK_FINDER : timeDebugMode;
    }

    public int getRescanRateMs() {
        return rescanRateMs;
    }

    public void setRescanRateMs(int rescanRateMs) {
        this.rescanRateMs = clamp(rescanRateMs, MIN_RESCAN_RATE_MS, MAX_RESCAN_RATE_MS);
    }

    public int getDeltaPacketThreshold() {
        return deltaPacketThreshold;
    }

    public void setDeltaPacketThreshold(int deltaPacketThreshold) {
        this.deltaPacketThreshold = clamp(deltaPacketThreshold, MIN_DELTA_THRESHOLD, MAX_DELTA_THRESHOLD);
    }

    public int getBlockEspRangeBlocks() {
        return blockEspRangeBlocks;
    }

    public void setBlockEspRangeBlocks(int blockEspRangeBlocks) {
        this.blockEspRangeBlocks = clamp(blockEspRangeBlocks, MIN_ESP_RANGE_BLOCKS, MAX_ESP_RANGE_BLOCKS);
    }

    public int getStorageEspRangeBlocks() {
        return storageEspRangeBlocks;
    }

    public void setStorageEspRangeBlocks(int storageEspRangeBlocks) {
        this.storageEspRangeBlocks = clamp(storageEspRangeBlocks, MIN_ESP_RANGE_BLOCKS, MAX_ESP_RANGE_BLOCKS);
    }

    public int getStashFinderThreshold() {
        return stashFinderThreshold;
    }

    public void setStashFinderThreshold(int stashFinderThreshold) {
        this.stashFinderThreshold = clamp(stashFinderThreshold, MIN_STASH_FINDER_THRESHOLD, MAX_STASH_FINDER_THRESHOLD);
    }

    public int getTimeDebugMarkDelayMs() {
        return timeDebugMarkDelayMs;
    }

    public void setTimeDebugMarkDelayMs(int timeDebugMarkDelayMs) {
        this.timeDebugMarkDelayMs = clamp(timeDebugMarkDelayMs, MIN_TIME_DEBUG_MARK_DELAY_MS, MAX_TIME_DEBUG_MARK_DELAY_MS);
    }

    public int getFreeCamSpeed() {
        return freeCamSpeed;
    }

    public void setFreeCamSpeed(int freeCamSpeed) {
        this.freeCamSpeed = clamp(freeCamSpeed, MIN_FREE_CAM_SPEED, MAX_FREE_CAM_SPEED);
    }

    public int getFastUseCooldownTicks() {
        return fastUseCooldownTicks;
    }

    public void setFastUseCooldownTicks(int fastUseCooldownTicks) {
        this.fastUseCooldownTicks = clamp(fastUseCooldownTicks, MIN_FAST_USE_COOLDOWN_TICKS, MAX_FAST_USE_COOLDOWN_TICKS);
    }

    public int getCrafterMacroMask() {
        return crafterMacroMask;
    }

    public void setCrafterMacroMask(int crafterMacroMask) {
        this.crafterMacroMask = crafterMacroMask & 0x1FF;
    }

    public boolean isCrafterSlotSelected(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= 9) {
            return false;
        }
        return (crafterMacroMask & (1 << slotIndex)) != 0;
    }

    public void setCrafterSlotSelected(int slotIndex, boolean selected) {
        if (slotIndex < 0 || slotIndex >= 9) {
            return;
        }
        if (selected) {
            crafterMacroMask |= (1 << slotIndex);
        } else {
            crafterMacroMask &= ~(1 << slotIndex);
        }
    }

    public int getRenderHeight() {
        return renderHeight;
    }

    public void setRenderHeight(int renderHeight) {
        this.renderHeight = clamp(renderHeight, MIN_RENDER_HEIGHT, MAX_RENDER_HEIGHT);
    }

    public double getFullBrightDefaultGamma() {
        return fullBrightDefaultGamma;
    }

    public void setFullBrightDefaultGamma(double fullBrightDefaultGamma) {
        this.fullBrightDefaultGamma = clamp(fullBrightDefaultGamma, MIN_FULL_BRIGHT_DEFAULT_GAMMA, MAX_FULL_BRIGHT_DEFAULT_GAMMA);
    }

    public String getNameProtectName() {
        return nameProtectName == null || nameProtectName.isBlank() ? "Protected" : nameProtectName;
    }

    public void setNameProtectName(String nameProtectName) {
        if (nameProtectName == null) {
            this.nameProtectName = "Protected";
            return;
        }

        String normalized = nameProtectName.trim();
        this.nameProtectName = normalized.isEmpty() ? "Protected" : normalized.substring(0, Math.min(24, normalized.length()));
    }

    public String getFakeStatsTitle() {
        return sanitizeStatText(fakeStatsTitle, "Donut SMP", 32);
    }

    public void setFakeStatsTitle(String fakeStatsTitle) {
        this.fakeStatsTitle = sanitizeStatText(fakeStatsTitle, "Donut SMP", 32);
    }

    public String getFakeStatsMoney() {
        return sanitizeStatText(fakeStatsMoney, "872.94K", 24);
    }

    public void setFakeStatsMoney(String fakeStatsMoney) {
        this.fakeStatsMoney = sanitizeStatText(fakeStatsMoney, "872.94K", 24);
    }

    public String getFakeStatsShards() {
        return sanitizeStatText(fakeStatsShards, "485", 24);
    }

    public void setFakeStatsShards(String fakeStatsShards) {
        this.fakeStatsShards = sanitizeStatText(fakeStatsShards, "485", 24);
    }

    public String getFakeStatsKills() {
        return sanitizeStatText(fakeStatsKills, "21", 24);
    }

    public void setFakeStatsKills(String fakeStatsKills) {
        this.fakeStatsKills = sanitizeStatText(fakeStatsKills, "21", 24);
    }

    public String getFakeStatsDeaths() {
        return sanitizeStatText(fakeStatsDeaths, "50", 24);
    }

    public void setFakeStatsDeaths(String fakeStatsDeaths) {
        this.fakeStatsDeaths = sanitizeStatText(fakeStatsDeaths, "50", 24);
    }

    public String getFakeStatsKeyall() {
        return sanitizeStatText(fakeStatsKeyall, "20m 5s", 24);
    }

    public void setFakeStatsKeyall(String fakeStatsKeyall) {
        this.fakeStatsKeyall = sanitizeStatText(fakeStatsKeyall, "20m 5s", 24);
    }

    public String getFakeStatsPlaytime() {
        return sanitizeStatText(fakeStatsPlaytime, "18d 11h", 24);
    }

    public void setFakeStatsPlaytime(String fakeStatsPlaytime) {
        this.fakeStatsPlaytime = sanitizeStatText(fakeStatsPlaytime, "18d 11h", 24);
    }

    public String getFakeStatsTeam() {
        return sanitizeStatText(fakeStatsTeam, "Effendylepstien", 32);
    }

    public void setFakeStatsTeam(String fakeStatsTeam) {
        this.fakeStatsTeam = sanitizeStatText(fakeStatsTeam, "Effendylepstien", 32);
    }

    public String getFakeStatsRegion() {
        return sanitizeStatText(fakeStatsRegion, "NA East (91ms)", 32);
    }

    public void setFakeStatsRegion(String fakeStatsRegion) {
        this.fakeStatsRegion = sanitizeStatText(fakeStatsRegion, "NA East (91ms)", 32);
    }

    public int getChunkFinderKeyCode() {
        return chunkFinderKeyCode;
    }

    public void setChunkFinderKeyCode(int chunkFinderKeyCode) {
        this.chunkFinderKeyCode = normalizeKeyCode(chunkFinderKeyCode);
    }

    public int getTimeDebugKeyCode() {
        return timeDebugKeyCode;
    }

    public void setTimeDebugKeyCode(int timeDebugKeyCode) {
        this.timeDebugKeyCode = normalizeKeyCode(timeDebugKeyCode);
    }

    public int getBlockEspKeyCode() {
        return blockEspKeyCode;
    }

    public void setBlockEspKeyCode(int blockEspKeyCode) {
        this.blockEspKeyCode = normalizeKeyCode(blockEspKeyCode);
    }

    public int getStorageEspKeyCode() {
        return storageEspKeyCode;
    }

    public void setStorageEspKeyCode(int storageEspKeyCode) {
        this.storageEspKeyCode = normalizeKeyCode(storageEspKeyCode);
    }

    public int getFreeCamKeyCode() {
        return freeCamKeyCode;
    }

    public void setFreeCamKeyCode(int freeCamKeyCode) {
        this.freeCamKeyCode = normalizeKeyCode(freeCamKeyCode);
    }

    public int getFullBrightKeyCode() {
        return fullBrightKeyCode;
    }

    public int getFreeLookKeyCode() {
        return freeLookKeyCode;
    }

    public void setFullBrightKeyCode(int fullBrightKeyCode) {
        this.fullBrightKeyCode = normalizeKeyCode(fullBrightKeyCode);
    }

    public void setFreeLookKeyCode(int freeLookKeyCode) {
        this.freeLookKeyCode = normalizeKeyCode(freeLookKeyCode);
    }

    public int getItemNametagsKeyCode() {
        return itemNametagsKeyCode;
    }

    public void setItemNametagsKeyCode(int itemNametagsKeyCode) {
        this.itemNametagsKeyCode = normalizeKeyCode(itemNametagsKeyCode);
    }

    public List<String> getBlockEspTargets() {
        return List.copyOf(blockEspTargets);
    }

    public void setBlockEspTargets(List<String> blockEspTargets) {
        this.blockEspTargets = normalizeTargets(blockEspTargets);
    }

    public boolean addBlockEspTarget(String blockId) {
        String normalized = normalizeTarget(blockId);
        if (normalized == null || blockEspTargets.contains(normalized)) {
            return false;
        }

        blockEspTargets = new ArrayList<>(blockEspTargets);
        blockEspTargets.add(normalized);
        return true;
    }

    public boolean removeBlockEspTarget(String blockId) {
        String normalized = normalizeTarget(blockId);
        if (normalized == null || !blockEspTargets.contains(normalized)) {
            return false;
        }

        blockEspTargets = new ArrayList<>(blockEspTargets);
        return blockEspTargets.remove(normalized);
    }

    public List<String> getStorageEspTargets() {
        return List.copyOf(storageEspTargets);
    }

    public void setStorageEspTargets(List<String> storageEspTargets) {
        this.storageEspTargets = normalizeTargets(storageEspTargets);
    }

    public boolean addStorageEspTarget(String blockId) {
        String normalized = normalizeTarget(blockId);
        if (normalized == null || storageEspTargets.contains(normalized)) {
            return false;
        }

        storageEspTargets = new ArrayList<>(storageEspTargets);
        storageEspTargets.add(normalized);
        return true;
    }

    public boolean removeStorageEspTarget(String blockId) {
        String normalized = normalizeTarget(blockId);
        if (normalized == null || !storageEspTargets.contains(normalized)) {
            return false;
        }

        storageEspTargets = new ArrayList<>(storageEspTargets);
        return storageEspTargets.remove(normalized);
    }

    public List<String> getBlockNotifierTargets() {
        return List.copyOf(blockNotifierTargets);
    }

    public void setBlockNotifierTargets(List<String> blockNotifierTargets) {
        this.blockNotifierTargets = normalizeTargets(blockNotifierTargets);
    }

    public boolean addBlockNotifierTarget(String blockId) {
        String normalized = normalizeTarget(blockId);
        if (normalized == null || blockNotifierTargets.contains(normalized)) {
            return false;
        }

        blockNotifierTargets = new ArrayList<>(blockNotifierTargets);
        blockNotifierTargets.add(normalized);
        return true;
    }

    public boolean removeBlockNotifierTarget(String blockId) {
        String normalized = normalizeTarget(blockId);
        if (normalized == null || !blockNotifierTargets.contains(normalized)) {
            return false;
        }

        blockNotifierTargets = new ArrayList<>(blockNotifierTargets);
        return blockNotifierTargets.remove(normalized);
    }

    public HuzzConfig copy() {
        HuzzConfig copy = new HuzzConfig();
        copy.setChunkFinderEnabled(chunkFinderEnabled);
        copy.setTimeDebugEnabled(timeDebugEnabled);
        copy.setPrimeChunkFinderEnabled(primeChunkFinderEnabled);
        copy.setBlockEspEnabled(blockEspEnabled);
        copy.setBlockEspTracers(blockEspTracers);
        copy.setStorageEspEnabled(storageEspEnabled);
        copy.setStorageEspTracers(storageEspTracers);
        copy.setStashFinderEnabled(stashFinderEnabled);
        copy.setBlockNotifierEnabled(blockNotifierEnabled);
        copy.setPlayerNotifierEnabled(playerNotifierEnabled);
        copy.setPlayerEspEnabled(playerEspEnabled);
        copy.setFreeCamEnabled(freeCamEnabled);
        copy.setFreeLookEnabled(freeLookEnabled);
        copy.setCrafterMacroEnabled(crafterMacroEnabled);
        copy.setCrafterMacroMask(crafterMacroMask);
        copy.setFastUseEnabled(fastUseEnabled);
        copy.setFullBrightEnabled(fullBrightEnabled);
        copy.setFullBrightFade(fullBrightFade);
        copy.setFpsHudEnabled(fpsHudEnabled);
        copy.setItemNametagsEnabled(itemNametagsEnabled);
        copy.setFakeMediaEnabled(fakeMediaEnabled);
        copy.setFakePayEnabled(fakePayEnabled);
        copy.setFakePayUppercaseSuffix(fakePayUppercaseSuffix);
        copy.setFakeStatsEnabled(fakeStatsEnabled);
        copy.setFakeStatsMode(fakeStatsMode);
        copy.setFakeStatsTitle(fakeStatsTitle);
        copy.setFakeStatsMoney(fakeStatsMoney);
        copy.setFakeStatsShards(fakeStatsShards);
        copy.setFakeStatsKills(fakeStatsKills);
        copy.setFakeStatsDeaths(fakeStatsDeaths);
        copy.setFakeStatsKeyall(fakeStatsKeyall);
        copy.setFakeStatsPlaytime(fakeStatsPlaytime);
        copy.setFakeStatsTeam(fakeStatsTeam);
        copy.setFakeStatsRegion(fakeStatsRegion);
        copy.setAutoTotemEnabled(autoTotemEnabled);
        copy.setAutoTotemDelayMs(autoTotemDelayMs);
        copy.setNameProtectEnabled(nameProtectEnabled);
        copy.setFullBrightMethod(fullBrightMethod);
        copy.setTimeDebugMode(timeDebugMode);
        copy.setRescanRateMs(rescanRateMs);
        copy.setDeltaPacketThreshold(deltaPacketThreshold);
        copy.setBlockEspRangeBlocks(blockEspRangeBlocks);
        copy.setStorageEspRangeBlocks(storageEspRangeBlocks);
        copy.setStashFinderThreshold(stashFinderThreshold);
        copy.setTimeDebugMarkDelayMs(timeDebugMarkDelayMs);
        copy.setFreeCamSpeed(freeCamSpeed);
        copy.setFastUseCooldownTicks(fastUseCooldownTicks);
        copy.setRenderHeight(renderHeight);
        copy.setFullBrightDefaultGamma(fullBrightDefaultGamma);
        copy.setNameProtectName(nameProtectName);
        copy.setChunkFinderKeyCode(chunkFinderKeyCode);
        copy.setTimeDebugKeyCode(timeDebugKeyCode);
        copy.setBlockEspKeyCode(blockEspKeyCode);
        copy.setStorageEspKeyCode(storageEspKeyCode);
        copy.setFreeCamKeyCode(freeCamKeyCode);
        copy.setFreeLookKeyCode(freeLookKeyCode);
        copy.setFullBrightKeyCode(fullBrightKeyCode);
        copy.setItemNametagsKeyCode(itemNametagsKeyCode);
        copy.setBlockEspTargets(blockEspTargets);
        copy.setStorageEspTargets(storageEspTargets);
        copy.setBlockNotifierTargets(blockNotifierTargets);
        return copy;
    }

    public void copyFrom(HuzzConfig other) {
        setChunkFinderEnabled(other.isChunkFinderEnabled());
        setTimeDebugEnabled(other.isTimeDebugEnabled());
        setPrimeChunkFinderEnabled(other.isPrimeChunkFinderEnabled());
        setBlockEspEnabled(other.isBlockEspEnabled());
        setBlockEspTracers(other.isBlockEspTracers());
        setStorageEspEnabled(other.isStorageEspEnabled());
        setStorageEspTracers(other.isStorageEspTracers());
        setStashFinderEnabled(other.isStashFinderEnabled());
        setBlockNotifierEnabled(other.isBlockNotifierEnabled());
        setPlayerNotifierEnabled(other.isPlayerNotifierEnabled());
        setPlayerEspEnabled(other.isPlayerEspEnabled());
        setFreeCamEnabled(other.isFreeCamEnabled());
        setFreeLookEnabled(other.isFreeLookEnabled());
        setCrafterMacroEnabled(other.isCrafterMacroEnabled());
        setCrafterMacroMask(other.getCrafterMacroMask());
        setFastUseEnabled(other.isFastUseEnabled());
        setFullBrightEnabled(other.isFullBrightEnabled());
        setFullBrightFade(other.isFullBrightFade());
        setFpsHudEnabled(other.isFpsHudEnabled());
        setItemNametagsEnabled(other.isItemNametagsEnabled());
        setFakeMediaEnabled(other.isFakeMediaEnabled());
        setFakePayEnabled(other.isFakePayEnabled());
        setFakePayUppercaseSuffix(other.isFakePayUppercaseSuffix());
        setFakeStatsEnabled(other.isFakeStatsEnabled());
        setFakeStatsMode(other.getFakeStatsMode());
        setFakeStatsTitle(other.getFakeStatsTitle());
        setFakeStatsMoney(other.getFakeStatsMoney());
        setFakeStatsShards(other.getFakeStatsShards());
        setFakeStatsKills(other.getFakeStatsKills());
        setFakeStatsDeaths(other.getFakeStatsDeaths());
        setFakeStatsKeyall(other.getFakeStatsKeyall());
        setFakeStatsPlaytime(other.getFakeStatsPlaytime());
        setFakeStatsTeam(other.getFakeStatsTeam());
        setFakeStatsRegion(other.getFakeStatsRegion());
        setAutoTotemEnabled(other.isAutoTotemEnabled());
        setAutoTotemDelayMs(other.getAutoTotemDelayMs());
        setNameProtectEnabled(other.isNameProtectEnabled());
        setFullBrightMethod(other.getFullBrightMethod());
        setTimeDebugMode(other.getTimeDebugMode());
        setRescanRateMs(other.getRescanRateMs());
        setDeltaPacketThreshold(other.getDeltaPacketThreshold());
        setBlockEspRangeBlocks(other.getBlockEspRangeBlocks());
        setStorageEspRangeBlocks(other.getStorageEspRangeBlocks());
        setStashFinderThreshold(other.getStashFinderThreshold());
        setTimeDebugMarkDelayMs(other.getTimeDebugMarkDelayMs());
        setFreeCamSpeed(other.getFreeCamSpeed());
        setFastUseCooldownTicks(other.getFastUseCooldownTicks());
        setRenderHeight(other.getRenderHeight());
        setFullBrightDefaultGamma(other.getFullBrightDefaultGamma());
        setNameProtectName(other.getNameProtectName());
        setChunkFinderKeyCode(other.getChunkFinderKeyCode());
        setTimeDebugKeyCode(other.getTimeDebugKeyCode());
        setBlockEspKeyCode(other.getBlockEspKeyCode());
        setStorageEspKeyCode(other.getStorageEspKeyCode());
        setFreeCamKeyCode(other.getFreeCamKeyCode());
        setFreeLookKeyCode(other.getFreeLookKeyCode());
        setFullBrightKeyCode(other.getFullBrightKeyCode());
        setItemNametagsKeyCode(other.getItemNametagsKeyCode());
        setBlockEspTargets(other.getBlockEspTargets());
        setStorageEspTargets(other.getStorageEspTargets());
        setBlockNotifierTargets(other.getBlockNotifierTargets());
    }

    public void disableAllModules() {
        setChunkFinderEnabled(false);
        setTimeDebugEnabled(false);
        setPrimeChunkFinderEnabled(false);
        setBlockEspEnabled(false);
        setStorageEspEnabled(false);
        setStashFinderEnabled(false);
        setBlockNotifierEnabled(false);
        setPlayerNotifierEnabled(false);
        setPlayerEspEnabled(false);
        setFreeCamEnabled(false);
        setFreeLookEnabled(false);
        setCrafterMacroEnabled(false);
        setFastUseEnabled(false);
        setFullBrightEnabled(false);
        setFpsHudEnabled(false);
        setItemNametagsEnabled(false);
        setFakeMediaEnabled(false);
        setFakePayEnabled(false);
        setFakeStatsEnabled(false);
        setAutoTotemEnabled(false);
        setNameProtectEnabled(false);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int normalizeKeyCode(int keyCode) {
        return keyCode < NO_KEY ? NO_KEY : keyCode;
    }

    private static List<String> normalizeTargets(List<String> targets) {
        if (targets == null || targets.isEmpty()) {
            return new ArrayList<>();
        }

        LinkedHashSet<String> normalizedTargets = new LinkedHashSet<>();
        for (String target : targets) {
            String normalized = normalizeTarget(target);
            if (normalized != null) {
                normalizedTargets.add(normalized);
            }
        }

        return new ArrayList<>(normalizedTargets);
    }

    private static String normalizeTarget(String blockId) {
        if (blockId == null) {
            return null;
        }

        String normalized = blockId.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private static String sanitizeStatText(String value, String fallback, int maxLength) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return fallback;
        }
        return trimmed.substring(0, Math.min(maxLength, trimmed.length()));
    }

    public enum TimeDebugMode {
        CHUNK_FINDER,
        HEATMAP
    }

    public enum FullBrightMethod {
        GAMMA,
        NIGHT_VISION
    }

    public enum FakeStatsMode {
        EDIT_ALL,
        KEEP_REAL_STATS_AND_UPDATE_MONEY
    }
}
