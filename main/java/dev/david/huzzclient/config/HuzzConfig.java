package dev.david.huzzclient.config;

import java.util.ArrayList;
import java.util.EnumMap;
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
    public static final int MIN_PLAYER_CHUNK_FAST_THRESHOLD_MS = 10;
    public static final int MAX_PLAYER_CHUNK_FAST_THRESHOLD_MS = 500;
    public static final int DEFAULT_PLAYER_CHUNK_FAST_THRESHOLD_MS = 75;
    public static final int MIN_PLAYER_CHUNK_BASELINE_SAMPLES = 3;
    public static final int MAX_PLAYER_CHUNK_BASELINE_SAMPLES = 30;
    public static final int DEFAULT_PLAYER_CHUNK_BASELINE_SAMPLES = 8;

    public static final int MIN_FREE_CAM_SPEED = 1;
    public static final int MAX_FREE_CAM_SPEED = 40;
    public static final int DEFAULT_FREE_CAM_SPEED = 10;
    public static final int MIN_AUTO_TOTEM_DELAY_MS = 50;
    public static final int MAX_AUTO_TOTEM_DELAY_MS = 2000;
    public static final int DEFAULT_AUTO_TOTEM_DELAY_MS = 300;
    public static final int MIN_AUTO_ROCKET_DELAY_MS = 1000;
    public static final int MAX_AUTO_ROCKET_DELAY_MS = 10000;
    public static final int DEFAULT_AUTO_ROCKET_DELAY_MS = 1000;
    public static final int MIN_AUTO_CRYSTAL_DELAY_MS = 0;
    public static final int MAX_AUTO_CRYSTAL_DELAY_MS = 1000;
    public static final int DEFAULT_AUTO_CRYSTAL_DELAY_MS = 0;
    public static final int MIN_FAST_USE_COOLDOWN_TICKS = 0;
    public static final int MAX_FAST_USE_COOLDOWN_TICKS = 4;
    public static final int DEFAULT_FAST_USE_COOLDOWN_TICKS = 1;
    public static final int MIN_STASH_FINDER_THRESHOLD = 2;
    public static final int MAX_STASH_FINDER_THRESHOLD = 128;
    public static final int DEFAULT_STASH_FINDER_THRESHOLD = 20;
    public static final int MIN_HOLE_ESP_RANGE_BLOCKS = 16;
    public static final int MAX_HOLE_ESP_RANGE_BLOCKS = 128;
    public static final int DEFAULT_HOLE_ESP_RANGE_BLOCKS = 48;
    public static final long MIN_TIME_CHANGER_VALUE = 0L;
    public static final long MAX_TIME_CHANGER_VALUE = 23999L;
    public static final long DEFAULT_TIME_CHANGER_VALUE = 6000L;
    public static final int MIN_AUTO_RECONNECT_DELAY_MS = 1000;
    public static final int MAX_AUTO_RECONNECT_DELAY_MS = 15000;
    public static final int DEFAULT_AUTO_RECONNECT_DELAY_MS = 3000;
    public static final double MIN_FULL_BRIGHT_DEFAULT_GAMMA = 0.0D;
    public static final double MAX_FULL_BRIGHT_DEFAULT_GAMMA = 1.0D;
    public static final double DEFAULT_FULL_BRIGHT_DEFAULT_GAMMA = 0.5D;
    public static final double MIN_SWING_SPEED_MULTIPLIER = 0.25D;
    public static final double MAX_SWING_SPEED_MULTIPLIER = 4.0D;
    public static final double DEFAULT_SWING_SPEED_MULTIPLIER = 1.0D;
    public static final int MIN_MOTION_BLUR_FRAMES = 2;
    public static final int MAX_MOTION_BLUR_FRAMES = 64;
    public static final int DEFAULT_MOTION_BLUR_FRAMES = 4;
    public static final int MIN_CUSTOM_FOV_DEGREES = 60;
    public static final int MAX_CUSTOM_FOV_DEGREES = 170;
    public static final int DEFAULT_CUSTOM_FOV_DEGREES = 90;
    public static final int MIN_AIM_FOV_RADIUS = 20;
    public static final int MAX_AIM_FOV_RADIUS = 400;
    public static final int DEFAULT_AIM_FOV_RADIUS = 120;
    public static final double MIN_AIM_ASSIST_STRENGTH = 0.05D;
    public static final double MAX_AIM_ASSIST_STRENGTH = 1.0D;
    public static final double DEFAULT_AIM_ASSIST_STRENGTH = 0.35D;
    public static final int MIN_DOUBLE_CLICK_DELAY_MS = 50;
    public static final int MAX_DOUBLE_CLICK_DELAY_MS = 2000;
    public static final int DEFAULT_DOUBLE_CLICK_DELAY_MS = 250;

    private boolean chunkFinderEnabled = true;
    private boolean timeDebugEnabled = true;
    private boolean primeChunkFinderEnabled;
    private boolean playerChunkFinderEnabled;
    private boolean blockEspEnabled;
    private boolean blockEspTracers = true;
    private boolean storageEspEnabled;
    private boolean storageEspTracers = true;
    private boolean stashFinderEnabled;
    private boolean blockNotifierEnabled;
    private boolean playerNotifierEnabled;
    private boolean playerEspEnabled;
    private boolean mobEspEnabled;
    private boolean mobEspTracers = true;
    private boolean skinProtectEnabled;
    private boolean swingSpeedEnabled;
    private boolean freeCamEnabled;
    private boolean freeLookEnabled;
    private boolean crafterMacroEnabled;
    private boolean chatMacroEnabled;
    private boolean autoRocketEnabled;
    private boolean holeEspEnabled;
    private boolean bedrockHoleEspEnabled;
    private boolean autoReconnectEnabled;
    private boolean autoDisconnectEnabled;
    private boolean autoReplenishEnabled;
    private boolean autoBridgeEnabled;
    private boolean autoToolEnabled;
    private boolean autoCrystalEnabled;
    private boolean fastUseEnabled;
    private boolean fullBrightEnabled;
    private boolean fullBrightFade = true;
    private boolean hudEnabled = true;
    private boolean hudFpsEnabled = true;
    private boolean hudMinimapEnabled = true;
    private boolean hudClockEnabled = true;
    private boolean hudArmorEnabled = true;
    private boolean hudPotionEffectsEnabled = true;
    private boolean hudMusicEnabled;
    private boolean hudMusicLargeUi;
    private boolean hudMusicThemeEnabled = true;
    private boolean motionBlurEnabled;
    private boolean customFovEnabled;
    private boolean noRenderEnabled;
    private boolean timeChangerEnabled;
    private boolean noRenderWeather = true;
    private boolean noRenderParticles = true;
    private boolean noRenderOverworldFog = true;
    private boolean noRenderChunkCulling = true;
    private boolean breakProgressEnabled = true;
    private boolean itemNametagsEnabled;
    private boolean fakeMediaEnabled;
    private boolean fakePayEnabled;
    private boolean fakePayUppercaseSuffix = true;
    private boolean fakeStatsEnabled;
    private boolean autoTotemEnabled;
    private boolean nameProtectEnabled;
    private boolean fakeSpawnerEnabled;
    private boolean fakeElytraEnabled;
    private boolean bedrockProtectEnabled;
    private boolean silentSetHomeEnabled;
    private boolean doubleClickEnabled;
    private boolean dualArmAnimationEnabled;
    private boolean goonEnabled;
    private boolean tunnelBaseFinderEnabled;
    private boolean autoEatEnabled;
    private boolean autoMineEnabled;
    private boolean autoWalkEnabled;
    private boolean lsdEnabled;
    private boolean blockNotifierDisconnectEnabled;
    private boolean aimAssistEnabled;
    private boolean aimFovEnabled;
    private boolean aimAssistVisibleOnly;
    private transient boolean autoBridgeForcedAutoReplenish;
    private int crafterMacroMask = 0x1FF;
    private int autoTotemDelayMs = DEFAULT_AUTO_TOTEM_DELAY_MS;
    private int autoRocketDelayMs = DEFAULT_AUTO_ROCKET_DELAY_MS;
    private int autoCrystalDelayMs = DEFAULT_AUTO_CRYSTAL_DELAY_MS;
    private int holeEspRangeBlocks = DEFAULT_HOLE_ESP_RANGE_BLOCKS;
    private int autoReconnectDelayMs = DEFAULT_AUTO_RECONNECT_DELAY_MS;
    private int doubleClickDelayMs = DEFAULT_DOUBLE_CLICK_DELAY_MS;
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
    private String autoReconnectAddress = "donutsmp.net";
    private FullBrightMethod fullBrightMethod = FullBrightMethod.GAMMA;
    private TunnelBaseFinderMode tunnelBaseFinderMode = TunnelBaseFinderMode.MODE_3X3;
    private AutoWalkDirection autoWalkDirection = AutoWalkDirection.FORWARD;
    private MusicHudSource musicHudSource = MusicHudSource.MAC_SPOTIFY_APPLESCRIPT;
    private TimeDebugMode timeDebugMode = TimeDebugMode.CHUNK_FINDER;
    private int rescanRateMs = DEFAULT_RESCAN_RATE_MS;
    private int deltaPacketThreshold = DEFAULT_DELTA_THRESHOLD;
    private int blockEspRangeBlocks = DEFAULT_BLOCK_ESP_RANGE_BLOCKS;
    private int storageEspRangeBlocks = DEFAULT_STORAGE_ESP_RANGE_BLOCKS;
    private int stashFinderThreshold = DEFAULT_STASH_FINDER_THRESHOLD;
    private int timeDebugMarkDelayMs = DEFAULT_TIME_DEBUG_MARK_DELAY_MS;
    private int playerChunkFastThresholdMs = DEFAULT_PLAYER_CHUNK_FAST_THRESHOLD_MS;
    private int playerChunkBaselineSamples = DEFAULT_PLAYER_CHUNK_BASELINE_SAMPLES;
    private int freeCamSpeed = DEFAULT_FREE_CAM_SPEED;
    private int fastUseCooldownTicks = DEFAULT_FAST_USE_COOLDOWN_TICKS;
    private int renderHeight = DEFAULT_RENDER_HEIGHT;
    private int motionBlurFrames = DEFAULT_MOTION_BLUR_FRAMES;
    private int customFovDegrees = DEFAULT_CUSTOM_FOV_DEGREES;
    private long timeChangerValue = DEFAULT_TIME_CHANGER_VALUE;
    private int aimAssistSlot = 0;
    private int aimFovRadius = DEFAULT_AIM_FOV_RADIUS;
    private double fullBrightDefaultGamma = DEFAULT_FULL_BRIGHT_DEFAULT_GAMMA;
    private double aimAssistStrength = DEFAULT_AIM_ASSIST_STRENGTH;
    private double swingSpeedMultiplier = DEFAULT_SWING_SPEED_MULTIPLIER;
    private AimAssistTarget aimAssistTarget = AimAssistTarget.HEAD;
    private BlockSelectionMode blockSelectionMode = BlockSelectionMode.FILLED;
    private HighlightColor blockSelectionColor = HighlightColor.CYAN;
    private int chunkFinderKeyCode = NO_KEY;
    private int timeDebugKeyCode = NO_KEY;
    private int blockEspKeyCode = NO_KEY;
    private int storageEspKeyCode = NO_KEY;
    private int freeCamKeyCode = NO_KEY;
    private int freeLookKeyCode = NO_KEY;
    private int fullBrightKeyCode = NO_KEY;
    private int itemNametagsKeyCode = NO_KEY;
    private EnumMap<ModuleKey, Integer> moduleKeyCodes = createDefaultModuleKeyCodes();
    private String nameProtectName = "Protected";
    private String skinProtectName = "Notch";
    private String fakeSpawnerBlockId = "";
    private String fakeElytraItemId = "";
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
    private List<ChatMacroEntry> chatMacros = new ArrayList<>();

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

    public boolean isPlayerChunkFinderEnabled() {
        return playerChunkFinderEnabled;
    }

    public void setPlayerChunkFinderEnabled(boolean playerChunkFinderEnabled) {
        this.playerChunkFinderEnabled = playerChunkFinderEnabled;
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

    public boolean isBlockNotifierDisconnectEnabled() {
        return blockNotifierDisconnectEnabled;
    }

    public void setBlockNotifierDisconnectEnabled(boolean blockNotifierDisconnectEnabled) {
        this.blockNotifierDisconnectEnabled = blockNotifierDisconnectEnabled;
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

    public boolean isMobEspEnabled() {
        return mobEspEnabled;
    }

    public void setMobEspEnabled(boolean mobEspEnabled) {
        this.mobEspEnabled = mobEspEnabled;
    }

    public boolean isMobEspTracers() {
        return mobEspTracers;
    }

    public void setMobEspTracers(boolean mobEspTracers) {
        this.mobEspTracers = mobEspTracers;
    }

    public boolean isSkinProtectEnabled() {
        return skinProtectEnabled;
    }

    public void setSkinProtectEnabled(boolean skinProtectEnabled) {
        this.skinProtectEnabled = skinProtectEnabled;
    }

    public boolean isSwingSpeedEnabled() {
        return swingSpeedEnabled;
    }

    public void setSwingSpeedEnabled(boolean swingSpeedEnabled) {
        this.swingSpeedEnabled = swingSpeedEnabled;
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

    public boolean isChatMacroEnabled() {
        return chatMacroEnabled;
    }

    public void setChatMacroEnabled(boolean chatMacroEnabled) {
        this.chatMacroEnabled = chatMacroEnabled;
    }

    public boolean isAutoRocketEnabled() {
        return autoRocketEnabled;
    }

    public void setAutoRocketEnabled(boolean autoRocketEnabled) {
        this.autoRocketEnabled = autoRocketEnabled;
    }

    public boolean isHoleEspEnabled() {
        return holeEspEnabled;
    }

    public void setHoleEspEnabled(boolean holeEspEnabled) {
        this.holeEspEnabled = holeEspEnabled;
    }

    public boolean isBedrockHoleEspEnabled() {
        return bedrockHoleEspEnabled;
    }

    public void setBedrockHoleEspEnabled(boolean bedrockHoleEspEnabled) {
        this.bedrockHoleEspEnabled = bedrockHoleEspEnabled;
    }

    public boolean isAutoReconnectEnabled() {
        return autoReconnectEnabled;
    }

    public void setAutoReconnectEnabled(boolean autoReconnectEnabled) {
        this.autoReconnectEnabled = autoReconnectEnabled;
    }

    public boolean isAutoDisconnectEnabled() {
        return autoDisconnectEnabled;
    }

    public void setAutoDisconnectEnabled(boolean autoDisconnectEnabled) {
        this.autoDisconnectEnabled = autoDisconnectEnabled;
    }

    public boolean isAutoReplenishEnabled() {
        return autoReplenishEnabled;
    }

    public void setAutoReplenishEnabled(boolean autoReplenishEnabled) {
        this.autoReplenishEnabled = autoReplenishEnabled;
    }

    public boolean isAutoBridgeEnabled() {
        return autoBridgeEnabled;
    }

    public void setAutoBridgeEnabled(boolean autoBridgeEnabled) {
        if (autoBridgeEnabled) {
            if (!this.autoBridgeEnabled && !this.autoReplenishEnabled) {
                autoBridgeForcedAutoReplenish = true;
                this.autoReplenishEnabled = true;
            }
        } else {
            if (this.autoBridgeEnabled && autoBridgeForcedAutoReplenish) {
                this.autoReplenishEnabled = false;
            }
            autoBridgeForcedAutoReplenish = false;
        }
        this.autoBridgeEnabled = autoBridgeEnabled;
    }

    public boolean isFastUseEnabled() {
        return fastUseEnabled;
    }

    public void setFastUseEnabled(boolean fastUseEnabled) {
        this.fastUseEnabled = fastUseEnabled;
    }

    public boolean isAutoToolEnabled() {
        return autoToolEnabled;
    }

    public void setAutoToolEnabled(boolean autoToolEnabled) {
        this.autoToolEnabled = autoToolEnabled;
    }

    public boolean isAutoCrystalEnabled() {
        return autoCrystalEnabled;
    }

    public void setAutoCrystalEnabled(boolean autoCrystalEnabled) {
        this.autoCrystalEnabled = autoCrystalEnabled;
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

    public TunnelBaseFinderMode getTunnelBaseFinderMode() {
        return tunnelBaseFinderMode == null ? TunnelBaseFinderMode.MODE_3X3 : tunnelBaseFinderMode;
    }

    public void setTunnelBaseFinderMode(TunnelBaseFinderMode tunnelBaseFinderMode) {
        this.tunnelBaseFinderMode = tunnelBaseFinderMode == null ? TunnelBaseFinderMode.MODE_3X3 : tunnelBaseFinderMode;
    }

    public AutoWalkDirection getAutoWalkDirection() {
        return autoWalkDirection == null ? AutoWalkDirection.FORWARD : autoWalkDirection;
    }

    public void setAutoWalkDirection(AutoWalkDirection autoWalkDirection) {
        this.autoWalkDirection = autoWalkDirection == null ? AutoWalkDirection.FORWARD : autoWalkDirection;
    }

    public MusicHudSource getMusicHudSource() {
        return musicHudSource == null ? MusicHudSource.MAC_SPOTIFY_APPLESCRIPT : musicHudSource;
    }

    public void setMusicHudSource(MusicHudSource musicHudSource) {
        this.musicHudSource = musicHudSource == null ? MusicHudSource.MAC_SPOTIFY_APPLESCRIPT : musicHudSource;
    }

    public boolean isItemNametagsEnabled() {
        return itemNametagsEnabled;
    }

    public void setItemNametagsEnabled(boolean itemNametagsEnabled) {
        this.itemNametagsEnabled = itemNametagsEnabled;
    }

    public boolean isHudEnabled() {
        return hudEnabled;
    }

    public void setHudEnabled(boolean hudEnabled) {
        this.hudEnabled = hudEnabled;
    }

    public boolean isHudFpsEnabled() {
        return hudFpsEnabled;
    }

    public void setHudFpsEnabled(boolean hudFpsEnabled) {
        this.hudFpsEnabled = hudFpsEnabled;
    }

    public boolean isHudMinimapEnabled() {
        return hudMinimapEnabled;
    }

    public void setHudMinimapEnabled(boolean hudMinimapEnabled) {
        this.hudMinimapEnabled = hudMinimapEnabled;
    }

    public boolean isHudClockEnabled() {
        return hudClockEnabled;
    }

    public void setHudClockEnabled(boolean hudClockEnabled) {
        this.hudClockEnabled = hudClockEnabled;
    }

    public boolean isHudArmorEnabled() {
        return hudArmorEnabled;
    }

    public void setHudArmorEnabled(boolean hudArmorEnabled) {
        this.hudArmorEnabled = hudArmorEnabled;
    }

    public boolean isHudPotionEffectsEnabled() {
        return hudPotionEffectsEnabled;
    }

    public void setHudPotionEffectsEnabled(boolean hudPotionEffectsEnabled) {
        this.hudPotionEffectsEnabled = hudPotionEffectsEnabled;
    }

    public boolean isHudMusicEnabled() {
        return hudMusicEnabled;
    }

    public void setHudMusicEnabled(boolean hudMusicEnabled) {
        this.hudMusicEnabled = hudMusicEnabled;
    }

    public boolean isHudMusicLargeUi() {
        return hudMusicLargeUi;
    }

    public void setHudMusicLargeUi(boolean hudMusicLargeUi) {
        this.hudMusicLargeUi = hudMusicLargeUi;
    }

    public boolean isHudMusicThemeEnabled() {
        return hudMusicThemeEnabled;
    }

    public void setHudMusicThemeEnabled(boolean hudMusicThemeEnabled) {
        this.hudMusicThemeEnabled = hudMusicThemeEnabled;
    }

    public boolean isMotionBlurEnabled() {
        return motionBlurEnabled;
    }

    public void setMotionBlurEnabled(boolean motionBlurEnabled) {
        this.motionBlurEnabled = motionBlurEnabled;
    }

    public boolean isCustomFovEnabled() {
        return customFovEnabled;
    }

    public void setCustomFovEnabled(boolean customFovEnabled) {
        this.customFovEnabled = customFovEnabled;
    }

    public boolean isNoRenderEnabled() {
        return noRenderEnabled;
    }

    public void setNoRenderEnabled(boolean noRenderEnabled) {
        this.noRenderEnabled = noRenderEnabled;
    }

    public boolean isTimeChangerEnabled() {
        return timeChangerEnabled;
    }

    public void setTimeChangerEnabled(boolean timeChangerEnabled) {
        this.timeChangerEnabled = timeChangerEnabled;
    }

    public boolean isNoRenderWeather() {
        return noRenderWeather;
    }

    public void setNoRenderWeather(boolean noRenderWeather) {
        this.noRenderWeather = noRenderWeather;
    }

    public boolean isNoRenderParticles() {
        return noRenderParticles;
    }

    public void setNoRenderParticles(boolean noRenderParticles) {
        this.noRenderParticles = noRenderParticles;
    }

    public boolean isNoRenderOverworldFog() {
        return noRenderOverworldFog;
    }

    public void setNoRenderOverworldFog(boolean noRenderOverworldFog) {
        this.noRenderOverworldFog = noRenderOverworldFog;
    }

    public boolean isNoRenderChunkCulling() {
        return noRenderChunkCulling;
    }

    public void setNoRenderChunkCulling(boolean noRenderChunkCulling) {
        this.noRenderChunkCulling = noRenderChunkCulling;
    }

    public boolean isBreakProgressEnabled() {
        return breakProgressEnabled;
    }

    public void setBreakProgressEnabled(boolean breakProgressEnabled) {
        this.breakProgressEnabled = breakProgressEnabled;
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

    public int getAutoRocketDelayMs() {
        return autoRocketDelayMs;
    }

    public void setAutoRocketDelayMs(int autoRocketDelayMs) {
        this.autoRocketDelayMs = clamp(autoRocketDelayMs, MIN_AUTO_ROCKET_DELAY_MS, MAX_AUTO_ROCKET_DELAY_MS);
    }

    public int getAutoCrystalDelayMs() {
        return autoCrystalDelayMs;
    }

    public void setAutoCrystalDelayMs(int autoCrystalDelayMs) {
        this.autoCrystalDelayMs = clamp(autoCrystalDelayMs, MIN_AUTO_CRYSTAL_DELAY_MS, MAX_AUTO_CRYSTAL_DELAY_MS);
    }

    public boolean isNameProtectEnabled() {
        return nameProtectEnabled;
    }

    public void setNameProtectEnabled(boolean nameProtectEnabled) {
        this.nameProtectEnabled = nameProtectEnabled;
    }

    public boolean isFakeSpawnerEnabled() {
        return fakeSpawnerEnabled;
    }

    public void setFakeSpawnerEnabled(boolean fakeSpawnerEnabled) {
        this.fakeSpawnerEnabled = fakeSpawnerEnabled;
    }

    public boolean isFakeElytraEnabled() {
        return fakeElytraEnabled;
    }

    public void setFakeElytraEnabled(boolean fakeElytraEnabled) {
        this.fakeElytraEnabled = fakeElytraEnabled;
    }

    public boolean isBedrockProtectEnabled() {
        return bedrockProtectEnabled;
    }

    public void setBedrockProtectEnabled(boolean bedrockProtectEnabled) {
        this.bedrockProtectEnabled = bedrockProtectEnabled;
    }

    public boolean isSilentSetHomeEnabled() {
        return silentSetHomeEnabled;
    }

    public void setSilentSetHomeEnabled(boolean silentSetHomeEnabled) {
        this.silentSetHomeEnabled = silentSetHomeEnabled;
    }

    public boolean isDoubleClickEnabled() {
        return doubleClickEnabled;
    }

    public void setDoubleClickEnabled(boolean doubleClickEnabled) {
        this.doubleClickEnabled = doubleClickEnabled;
    }

    public boolean isDualArmAnimationEnabled() {
        return dualArmAnimationEnabled;
    }

    public void setDualArmAnimationEnabled(boolean dualArmAnimationEnabled) {
        this.dualArmAnimationEnabled = dualArmAnimationEnabled;
    }

    public boolean isGoonEnabled() {
        return goonEnabled;
    }

    public void setGoonEnabled(boolean goonEnabled) {
        this.goonEnabled = goonEnabled;
    }

    public boolean isTunnelBaseFinderEnabled() {
        return tunnelBaseFinderEnabled;
    }

    public void setTunnelBaseFinderEnabled(boolean tunnelBaseFinderEnabled) {
        this.tunnelBaseFinderEnabled = tunnelBaseFinderEnabled;
    }

    public boolean isAutoEatEnabled() {
        return autoEatEnabled;
    }

    public void setAutoEatEnabled(boolean autoEatEnabled) {
        this.autoEatEnabled = autoEatEnabled;
    }

    public boolean isAutoMineEnabled() {
        return autoMineEnabled;
    }

    public void setAutoMineEnabled(boolean autoMineEnabled) {
        this.autoMineEnabled = autoMineEnabled;
    }

    public boolean isAutoWalkEnabled() {
        return autoWalkEnabled;
    }

    public void setAutoWalkEnabled(boolean autoWalkEnabled) {
        this.autoWalkEnabled = autoWalkEnabled;
    }

    public boolean isLsdEnabled() {
        return lsdEnabled;
    }

    public void setLsdEnabled(boolean lsdEnabled) {
        this.lsdEnabled = lsdEnabled;
    }

    public boolean isAimAssistEnabled() {
        return aimAssistEnabled;
    }

    public void setAimAssistEnabled(boolean aimAssistEnabled) {
        this.aimAssistEnabled = aimAssistEnabled;
    }

    public boolean isAimFovEnabled() {
        return aimFovEnabled;
    }

    public void setAimFovEnabled(boolean aimFovEnabled) {
        this.aimFovEnabled = aimFovEnabled;
    }

    public boolean isAimAssistVisibleOnly() {
        return aimAssistVisibleOnly;
    }

    public void setAimAssistVisibleOnly(boolean aimAssistVisibleOnly) {
        this.aimAssistVisibleOnly = aimAssistVisibleOnly;
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

    public int getHoleEspRangeBlocks() {
        return holeEspRangeBlocks;
    }

    public void setHoleEspRangeBlocks(int holeEspRangeBlocks) {
        this.holeEspRangeBlocks = clamp(holeEspRangeBlocks, MIN_HOLE_ESP_RANGE_BLOCKS, MAX_HOLE_ESP_RANGE_BLOCKS);
    }

    public int getAutoReconnectDelayMs() {
        return autoReconnectDelayMs;
    }

    public void setAutoReconnectDelayMs(int autoReconnectDelayMs) {
        this.autoReconnectDelayMs = clamp(autoReconnectDelayMs, MIN_AUTO_RECONNECT_DELAY_MS, MAX_AUTO_RECONNECT_DELAY_MS);
    }

    public int getDoubleClickDelayMs() {
        return doubleClickDelayMs;
    }

    public void setDoubleClickDelayMs(int doubleClickDelayMs) {
        this.doubleClickDelayMs = clamp(doubleClickDelayMs, MIN_DOUBLE_CLICK_DELAY_MS, MAX_DOUBLE_CLICK_DELAY_MS);
    }

    public int getCustomFovDegrees() {
        return customFovDegrees;
    }

    public void setCustomFovDegrees(int customFovDegrees) {
        this.customFovDegrees = clamp(customFovDegrees, MIN_CUSTOM_FOV_DEGREES, MAX_CUSTOM_FOV_DEGREES);
    }

    public long getTimeChangerValue() {
        return Math.max(MIN_TIME_CHANGER_VALUE, Math.min(MAX_TIME_CHANGER_VALUE, timeChangerValue));
    }

    public void setTimeChangerValue(long timeChangerValue) {
        this.timeChangerValue = Math.max(MIN_TIME_CHANGER_VALUE, Math.min(MAX_TIME_CHANGER_VALUE, timeChangerValue));
    }

    public int getAimAssistSlot() {
        return clamp(aimAssistSlot, 0, 9);
    }

    public void setAimAssistSlot(int aimAssistSlot) {
        this.aimAssistSlot = clamp(aimAssistSlot, 0, 9);
    }

    public AimAssistTarget getAimAssistTarget() {
        return aimAssistTarget == null ? AimAssistTarget.HEAD : aimAssistTarget;
    }

    public void setAimAssistTarget(AimAssistTarget aimAssistTarget) {
        this.aimAssistTarget = aimAssistTarget == null ? AimAssistTarget.HEAD : aimAssistTarget;
    }

    public int getAimFovRadius() {
        return aimFovRadius;
    }

    public void setAimFovRadius(int aimFovRadius) {
        this.aimFovRadius = clamp(aimFovRadius, MIN_AIM_FOV_RADIUS, MAX_AIM_FOV_RADIUS);
    }

    public double getAimAssistStrength() {
        return aimAssistStrength;
    }

    public void setAimAssistStrength(double aimAssistStrength) {
        this.aimAssistStrength = clamp(aimAssistStrength, MIN_AIM_ASSIST_STRENGTH, MAX_AIM_ASSIST_STRENGTH);
    }

    public int getTimeDebugMarkDelayMs() {
        return timeDebugMarkDelayMs;
    }

    public void setTimeDebugMarkDelayMs(int timeDebugMarkDelayMs) {
        this.timeDebugMarkDelayMs = clamp(timeDebugMarkDelayMs, MIN_TIME_DEBUG_MARK_DELAY_MS, MAX_TIME_DEBUG_MARK_DELAY_MS);
    }

    public int getPlayerChunkFastThresholdMs() {
        return playerChunkFastThresholdMs;
    }

    public void setPlayerChunkFastThresholdMs(int playerChunkFastThresholdMs) {
        this.playerChunkFastThresholdMs = clamp(
            playerChunkFastThresholdMs,
            MIN_PLAYER_CHUNK_FAST_THRESHOLD_MS,
            MAX_PLAYER_CHUNK_FAST_THRESHOLD_MS
        );
    }

    public int getPlayerChunkBaselineSamples() {
        return playerChunkBaselineSamples;
    }

    public void setPlayerChunkBaselineSamples(int playerChunkBaselineSamples) {
        this.playerChunkBaselineSamples = clamp(
            playerChunkBaselineSamples,
            MIN_PLAYER_CHUNK_BASELINE_SAMPLES,
            MAX_PLAYER_CHUNK_BASELINE_SAMPLES
        );
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

    public int getMotionBlurFrames() {
        return motionBlurFrames;
    }

    public void setMotionBlurFrames(int motionBlurFrames) {
        this.motionBlurFrames = clamp(motionBlurFrames, MIN_MOTION_BLUR_FRAMES, MAX_MOTION_BLUR_FRAMES);
    }

    public double getFullBrightDefaultGamma() {
        return fullBrightDefaultGamma;
    }

    public void setFullBrightDefaultGamma(double fullBrightDefaultGamma) {
        this.fullBrightDefaultGamma = clamp(fullBrightDefaultGamma, MIN_FULL_BRIGHT_DEFAULT_GAMMA, MAX_FULL_BRIGHT_DEFAULT_GAMMA);
    }

    public double getSwingSpeedMultiplier() {
        return swingSpeedMultiplier;
    }

    public void setSwingSpeedMultiplier(double swingSpeedMultiplier) {
        this.swingSpeedMultiplier = clamp(swingSpeedMultiplier, MIN_SWING_SPEED_MULTIPLIER, MAX_SWING_SPEED_MULTIPLIER);
    }

    public BlockSelectionMode getBlockSelectionMode() {
        return blockSelectionMode;
    }

    public void setBlockSelectionMode(BlockSelectionMode blockSelectionMode) {
        this.blockSelectionMode = blockSelectionMode == null ? BlockSelectionMode.OFF : blockSelectionMode;
    }

    public HighlightColor getBlockSelectionColor() {
        return blockSelectionColor;
    }

    public void setBlockSelectionColor(HighlightColor blockSelectionColor) {
        this.blockSelectionColor = blockSelectionColor == null ? HighlightColor.CYAN : blockSelectionColor;
    }

    public String getAutoReconnectAddress() {
        return sanitizeAddress(autoReconnectAddress, "donutsmp.net");
    }

    public void setAutoReconnectAddress(String autoReconnectAddress) {
        this.autoReconnectAddress = sanitizeAddress(autoReconnectAddress, "donutsmp.net");
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

    public String getSkinProtectName() {
        return skinProtectName == null || skinProtectName.isBlank() ? "Notch" : skinProtectName;
    }

    public void setSkinProtectName(String skinProtectName) {
        if (skinProtectName == null) {
            this.skinProtectName = "Notch";
            return;
        }

        String normalized = skinProtectName.trim();
        if (normalized.isEmpty()) {
            this.skinProtectName = "Notch";
            return;
        }

        normalized = normalized.replaceAll("[^A-Za-z0-9_]", "");
        this.skinProtectName = normalized.isEmpty() ? "Notch" : normalized.substring(0, Math.min(16, normalized.length()));
    }

    public String getFakeSpawnerBlockId() {
        return sanitizeOptionalTarget(fakeSpawnerBlockId);
    }

    public void setFakeSpawnerBlockId(String fakeSpawnerBlockId) {
        this.fakeSpawnerBlockId = sanitizeOptionalTarget(fakeSpawnerBlockId);
    }

    public String getFakeElytraItemId() {
        return sanitizeOptionalTarget(fakeElytraItemId);
    }

    public void setFakeElytraItemId(String fakeElytraItemId) {
        this.fakeElytraItemId = sanitizeOptionalTarget(fakeElytraItemId);
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
        return getModuleKeyCode(ModuleKey.CHUNK_FINDER);
    }

    public void setChunkFinderKeyCode(int chunkFinderKeyCode) {
        this.chunkFinderKeyCode = normalizeKeyCode(chunkFinderKeyCode);
        setModuleKeyCode(ModuleKey.CHUNK_FINDER, this.chunkFinderKeyCode);
    }

    public int getTimeDebugKeyCode() {
        return getModuleKeyCode(ModuleKey.TIME_DEBUG);
    }

    public void setTimeDebugKeyCode(int timeDebugKeyCode) {
        this.timeDebugKeyCode = normalizeKeyCode(timeDebugKeyCode);
        setModuleKeyCode(ModuleKey.TIME_DEBUG, this.timeDebugKeyCode);
    }

    public int getBlockEspKeyCode() {
        return getModuleKeyCode(ModuleKey.BLOCK_ESP);
    }

    public void setBlockEspKeyCode(int blockEspKeyCode) {
        this.blockEspKeyCode = normalizeKeyCode(blockEspKeyCode);
        setModuleKeyCode(ModuleKey.BLOCK_ESP, this.blockEspKeyCode);
    }

    public int getStorageEspKeyCode() {
        return getModuleKeyCode(ModuleKey.STORAGE_ESP);
    }

    public void setStorageEspKeyCode(int storageEspKeyCode) {
        this.storageEspKeyCode = normalizeKeyCode(storageEspKeyCode);
        setModuleKeyCode(ModuleKey.STORAGE_ESP, this.storageEspKeyCode);
    }

    public int getFreeCamKeyCode() {
        return getModuleKeyCode(ModuleKey.FREE_CAM);
    }

    public void setFreeCamKeyCode(int freeCamKeyCode) {
        this.freeCamKeyCode = normalizeKeyCode(freeCamKeyCode);
        setModuleKeyCode(ModuleKey.FREE_CAM, this.freeCamKeyCode);
    }

    public int getFullBrightKeyCode() {
        return getModuleKeyCode(ModuleKey.FULL_BRIGHT);
    }

    public int getFreeLookKeyCode() {
        return getModuleKeyCode(ModuleKey.FREE_LOOK);
    }

    public void setFullBrightKeyCode(int fullBrightKeyCode) {
        this.fullBrightKeyCode = normalizeKeyCode(fullBrightKeyCode);
        setModuleKeyCode(ModuleKey.FULL_BRIGHT, this.fullBrightKeyCode);
    }

    public void setFreeLookKeyCode(int freeLookKeyCode) {
        this.freeLookKeyCode = normalizeKeyCode(freeLookKeyCode);
        setModuleKeyCode(ModuleKey.FREE_LOOK, this.freeLookKeyCode);
    }

    public int getItemNametagsKeyCode() {
        return getModuleKeyCode(ModuleKey.ITEM_NAMETAGS);
    }

    public void setItemNametagsKeyCode(int itemNametagsKeyCode) {
        this.itemNametagsKeyCode = normalizeKeyCode(itemNametagsKeyCode);
        setModuleKeyCode(ModuleKey.ITEM_NAMETAGS, this.itemNametagsKeyCode);
    }

    public int getModuleKeyCode(ModuleKey moduleKey) {
        if (moduleKey == null) {
            return NO_KEY;
        }
        ensureModuleKeyCodes();
        return normalizeKeyCode(moduleKeyCodes.getOrDefault(moduleKey, NO_KEY));
    }

    public void setModuleKeyCode(ModuleKey moduleKey, int keyCode) {
        if (moduleKey == null) {
            return;
        }
        ensureModuleKeyCodes();
        int normalizedKeyCode = normalizeKeyCode(keyCode);
        moduleKeyCodes.put(moduleKey, normalizedKeyCode);
        switch (moduleKey) {
            case CHUNK_FINDER -> chunkFinderKeyCode = normalizedKeyCode;
            case TIME_DEBUG -> timeDebugKeyCode = normalizedKeyCode;
            case BLOCK_ESP -> blockEspKeyCode = normalizedKeyCode;
            case STORAGE_ESP -> storageEspKeyCode = normalizedKeyCode;
            case FREE_CAM -> freeCamKeyCode = normalizedKeyCode;
            case FREE_LOOK -> freeLookKeyCode = normalizedKeyCode;
            case FULL_BRIGHT -> fullBrightKeyCode = normalizedKeyCode;
            case ITEM_NAMETAGS -> itemNametagsKeyCode = normalizedKeyCode;
            case PRIME_CHUNK_FINDER, PLAYER_CHUNKS, HOLE_ESP, BEDROCK_HOLE_ESP, BREAK_PROGRESS, STASH_FINDER, BLOCK_NOTIFIER, CHAT_MACRO,
                PLAYER_NOTIFIER, AUTO_DISCONNECT, PLAYER_ESP, MOB_ESP, SWING_SPEED, AUTO_REPLENISH, AUTO_BRIDGE, AUTO_TOOL, AUTO_CRYSTAL,
                AIM_ASSIST, AIM_FOV,
                FAST_USE, CUSTOM_FOV, MOTION_BLUR, NO_RENDER, TIME_CHANGER, BLOCK_SELECT, FAKE_MEDIA, FAKE_PAY, FAKE_STATS, AUTO_TOTEM,
                AUTO_ROCKET, NAME_PROTECT, SKIN_PROTECT, AUTO_RECONNECT, HUD, CRAFTER_MACRO, FAKE_SPAWNER, FAKE_ELYTRA,
                BEDROCK_PROTECT, SILENT_SET_HOME, DOUBLE_CLICK, DUAL_ARM_ANIMATION, TUNNEL_BASE_FINDER, AUTO_EAT,
                AUTO_MINE, AUTO_WALK, LSD -> {
            }
        }
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

    public List<ChatMacroEntry> getChatMacros() {
        return chatMacros.stream()
            .map(ChatMacroEntry::copy)
            .toList();
    }

    public void setChatMacros(List<ChatMacroEntry> chatMacros) {
        this.chatMacros = normalizeChatMacros(chatMacros);
    }

    public boolean addChatMacro(String message, int keyCode) {
        ChatMacroEntry normalized = normalizeChatMacro(new ChatMacroEntry(message, keyCode));
        if (normalized == null) {
            return false;
        }
        if (chatMacros.stream().anyMatch(entry -> entry.getKeyCode() == normalized.getKeyCode())) {
            return false;
        }

        chatMacros = new ArrayList<>(chatMacros);
        chatMacros.add(normalized);
        return true;
    }

    public boolean removeChatMacro(ChatMacroEntry entry) {
        if (entry == null) {
            return false;
        }

        chatMacros = new ArrayList<>(chatMacros);
        return chatMacros.removeIf(existing ->
            existing.getKeyCode() == entry.getKeyCode() && existing.getMessage().equals(entry.getMessage()));
    }

    public HuzzConfig copy() {
        HuzzConfig copy = new HuzzConfig();
        copy.setChunkFinderEnabled(chunkFinderEnabled);
        copy.setTimeDebugEnabled(timeDebugEnabled);
        copy.setPrimeChunkFinderEnabled(primeChunkFinderEnabled);
        copy.setPlayerChunkFinderEnabled(playerChunkFinderEnabled);
        copy.setBlockEspEnabled(blockEspEnabled);
        copy.setBlockEspTracers(blockEspTracers);
        copy.setStorageEspEnabled(storageEspEnabled);
        copy.setStorageEspTracers(storageEspTracers);
        copy.setStashFinderEnabled(stashFinderEnabled);
        copy.setBlockNotifierEnabled(blockNotifierEnabled);
        copy.setBlockNotifierDisconnectEnabled(blockNotifierDisconnectEnabled);
        copy.setPlayerNotifierEnabled(playerNotifierEnabled);
        copy.setPlayerEspEnabled(playerEspEnabled);
        copy.setMobEspEnabled(mobEspEnabled);
        copy.setMobEspTracers(mobEspTracers);
        copy.setSkinProtectEnabled(skinProtectEnabled);
        copy.setSwingSpeedEnabled(swingSpeedEnabled);
        copy.setFreeCamEnabled(freeCamEnabled);
        copy.setFreeLookEnabled(freeLookEnabled);
        copy.setCrafterMacroEnabled(crafterMacroEnabled);
        copy.setCrafterMacroMask(crafterMacroMask);
        copy.setChatMacroEnabled(chatMacroEnabled);
        copy.setAutoRocketEnabled(autoRocketEnabled);
        copy.setHoleEspEnabled(holeEspEnabled);
        copy.setBedrockHoleEspEnabled(bedrockHoleEspEnabled);
        copy.setAutoReconnectEnabled(autoReconnectEnabled);
        copy.setAutoDisconnectEnabled(autoDisconnectEnabled);
        copy.setAutoReplenishEnabled(autoReplenishEnabled);
        copy.setAutoBridgeEnabled(autoBridgeEnabled);
        copy.setAutoToolEnabled(autoToolEnabled);
        copy.setAutoCrystalEnabled(autoCrystalEnabled);
        copy.setFastUseEnabled(fastUseEnabled);
        copy.setFullBrightEnabled(fullBrightEnabled);
        copy.setFullBrightFade(fullBrightFade);
        copy.setHudEnabled(hudEnabled);
        copy.setHudFpsEnabled(hudFpsEnabled);
        copy.setHudMinimapEnabled(hudMinimapEnabled);
        copy.setHudClockEnabled(hudClockEnabled);
        copy.setHudArmorEnabled(hudArmorEnabled);
        copy.setHudPotionEffectsEnabled(hudPotionEffectsEnabled);
        copy.setHudMusicEnabled(hudMusicEnabled);
        copy.setHudMusicLargeUi(hudMusicLargeUi);
        copy.setHudMusicThemeEnabled(hudMusicThemeEnabled);
        copy.setMotionBlurEnabled(motionBlurEnabled);
        copy.setCustomFovEnabled(customFovEnabled);
        copy.setNoRenderEnabled(noRenderEnabled);
        copy.setTimeChangerEnabled(timeChangerEnabled);
        copy.setNoRenderWeather(noRenderWeather);
        copy.setNoRenderParticles(noRenderParticles);
        copy.setNoRenderOverworldFog(noRenderOverworldFog);
        copy.setNoRenderChunkCulling(noRenderChunkCulling);
        copy.setBreakProgressEnabled(breakProgressEnabled);
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
        copy.setAutoRocketDelayMs(autoRocketDelayMs);
        copy.setAutoCrystalDelayMs(autoCrystalDelayMs);
        copy.setNameProtectEnabled(nameProtectEnabled);
        copy.setFakeSpawnerEnabled(fakeSpawnerEnabled);
        copy.setFakeElytraEnabled(fakeElytraEnabled);
        copy.setBedrockProtectEnabled(bedrockProtectEnabled);
        copy.setSilentSetHomeEnabled(silentSetHomeEnabled);
        copy.setDoubleClickEnabled(doubleClickEnabled);
        copy.setDualArmAnimationEnabled(dualArmAnimationEnabled);
        copy.setGoonEnabled(goonEnabled);
        copy.setTunnelBaseFinderEnabled(tunnelBaseFinderEnabled);
        copy.setAutoEatEnabled(autoEatEnabled);
        copy.setAutoMineEnabled(autoMineEnabled);
        copy.setAutoWalkEnabled(autoWalkEnabled);
        copy.setLsdEnabled(lsdEnabled);
        copy.setAimAssistEnabled(aimAssistEnabled);
        copy.setAimFovEnabled(aimFovEnabled);
        copy.setAimAssistVisibleOnly(aimAssistVisibleOnly);
        copy.setFullBrightMethod(fullBrightMethod);
        copy.setTunnelBaseFinderMode(tunnelBaseFinderMode);
        copy.setAutoWalkDirection(autoWalkDirection);
        copy.setMusicHudSource(musicHudSource);
        copy.setTimeDebugMode(timeDebugMode);
        copy.setRescanRateMs(rescanRateMs);
        copy.setDeltaPacketThreshold(deltaPacketThreshold);
        copy.setBlockEspRangeBlocks(blockEspRangeBlocks);
        copy.setStorageEspRangeBlocks(storageEspRangeBlocks);
        copy.setStashFinderThreshold(stashFinderThreshold);
        copy.setHoleEspRangeBlocks(holeEspRangeBlocks);
        copy.setAutoReconnectDelayMs(autoReconnectDelayMs);
        copy.setDoubleClickDelayMs(doubleClickDelayMs);
        copy.setTimeDebugMarkDelayMs(timeDebugMarkDelayMs);
        copy.setPlayerChunkFastThresholdMs(playerChunkFastThresholdMs);
        copy.setPlayerChunkBaselineSamples(playerChunkBaselineSamples);
        copy.setFreeCamSpeed(freeCamSpeed);
        copy.setFastUseCooldownTicks(fastUseCooldownTicks);
        copy.setRenderHeight(renderHeight);
        copy.setMotionBlurFrames(motionBlurFrames);
        copy.setCustomFovDegrees(customFovDegrees);
        copy.setTimeChangerValue(timeChangerValue);
        copy.setAimAssistSlot(aimAssistSlot);
        copy.setAimAssistTarget(aimAssistTarget);
        copy.setAimFovRadius(aimFovRadius);
        copy.setAimAssistStrength(aimAssistStrength);
        copy.setFullBrightDefaultGamma(fullBrightDefaultGamma);
        copy.setSwingSpeedMultiplier(swingSpeedMultiplier);
        copy.setBlockSelectionMode(blockSelectionMode);
        copy.setBlockSelectionColor(blockSelectionColor);
        copy.setAutoReconnectAddress(autoReconnectAddress);
        copy.setNameProtectName(nameProtectName);
        copy.setSkinProtectName(skinProtectName);
        copy.setFakeSpawnerBlockId(fakeSpawnerBlockId);
        copy.setFakeElytraItemId(fakeElytraItemId);
        copy.setChunkFinderKeyCode(chunkFinderKeyCode);
        copy.setTimeDebugKeyCode(timeDebugKeyCode);
        copy.setBlockEspKeyCode(blockEspKeyCode);
        copy.setStorageEspKeyCode(storageEspKeyCode);
        copy.setFreeCamKeyCode(freeCamKeyCode);
        copy.setFreeLookKeyCode(freeLookKeyCode);
        copy.setFullBrightKeyCode(fullBrightKeyCode);
        copy.setItemNametagsKeyCode(itemNametagsKeyCode);
        copy.copyModuleKeyCodesFrom(this);
        copy.setBlockEspTargets(blockEspTargets);
        copy.setStorageEspTargets(storageEspTargets);
        copy.setBlockNotifierTargets(blockNotifierTargets);
        copy.setChatMacros(chatMacros);
        return copy;
    }

    public void copyFrom(HuzzConfig other) {
        setChunkFinderEnabled(other.isChunkFinderEnabled());
        setTimeDebugEnabled(other.isTimeDebugEnabled());
        setPrimeChunkFinderEnabled(other.isPrimeChunkFinderEnabled());
        setPlayerChunkFinderEnabled(other.isPlayerChunkFinderEnabled());
        setBlockEspEnabled(other.isBlockEspEnabled());
        setBlockEspTracers(other.isBlockEspTracers());
        setStorageEspEnabled(other.isStorageEspEnabled());
        setStorageEspTracers(other.isStorageEspTracers());
        setStashFinderEnabled(other.isStashFinderEnabled());
        setBlockNotifierEnabled(other.isBlockNotifierEnabled());
        setBlockNotifierDisconnectEnabled(other.isBlockNotifierDisconnectEnabled());
        setPlayerNotifierEnabled(other.isPlayerNotifierEnabled());
        setPlayerEspEnabled(other.isPlayerEspEnabled());
        setMobEspEnabled(other.isMobEspEnabled());
        setMobEspTracers(other.isMobEspTracers());
        setSkinProtectEnabled(other.isSkinProtectEnabled());
        setSwingSpeedEnabled(other.isSwingSpeedEnabled());
        setFreeCamEnabled(other.isFreeCamEnabled());
        setFreeLookEnabled(other.isFreeLookEnabled());
        setCrafterMacroEnabled(other.isCrafterMacroEnabled());
        setCrafterMacroMask(other.getCrafterMacroMask());
        setChatMacroEnabled(other.isChatMacroEnabled());
        setAutoRocketEnabled(other.isAutoRocketEnabled());
        setHoleEspEnabled(other.isHoleEspEnabled());
        setBedrockHoleEspEnabled(other.isBedrockHoleEspEnabled());
        setAutoReconnectEnabled(other.isAutoReconnectEnabled());
        setAutoDisconnectEnabled(other.isAutoDisconnectEnabled());
        setAutoReplenishEnabled(other.isAutoReplenishEnabled());
        setAutoBridgeEnabled(other.isAutoBridgeEnabled());
        setAutoToolEnabled(other.isAutoToolEnabled());
        setAutoCrystalEnabled(other.isAutoCrystalEnabled());
        setFastUseEnabled(other.isFastUseEnabled());
        setFullBrightEnabled(other.isFullBrightEnabled());
        setFullBrightFade(other.isFullBrightFade());
        setHudEnabled(other.isHudEnabled());
        setHudFpsEnabled(other.isHudFpsEnabled());
        setHudMinimapEnabled(other.isHudMinimapEnabled());
        setHudClockEnabled(other.isHudClockEnabled());
        setHudArmorEnabled(other.isHudArmorEnabled());
        setHudPotionEffectsEnabled(other.isHudPotionEffectsEnabled());
        setHudMusicEnabled(other.isHudMusicEnabled());
        setHudMusicLargeUi(other.isHudMusicLargeUi());
        setHudMusicThemeEnabled(other.isHudMusicThemeEnabled());
        setMotionBlurEnabled(other.isMotionBlurEnabled());
        setCustomFovEnabled(other.isCustomFovEnabled());
        setNoRenderEnabled(other.isNoRenderEnabled());
        setTimeChangerEnabled(other.isTimeChangerEnabled());
        setNoRenderWeather(other.isNoRenderWeather());
        setNoRenderParticles(other.isNoRenderParticles());
        setNoRenderOverworldFog(other.isNoRenderOverworldFog());
        setNoRenderChunkCulling(other.isNoRenderChunkCulling());
        setBreakProgressEnabled(other.isBreakProgressEnabled());
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
        setAutoRocketDelayMs(other.getAutoRocketDelayMs());
        setAutoCrystalDelayMs(other.getAutoCrystalDelayMs());
        setNameProtectEnabled(other.isNameProtectEnabled());
        setFakeSpawnerEnabled(other.isFakeSpawnerEnabled());
        setFakeElytraEnabled(other.isFakeElytraEnabled());
        setBedrockProtectEnabled(other.isBedrockProtectEnabled());
        setSilentSetHomeEnabled(other.isSilentSetHomeEnabled());
        setDoubleClickEnabled(other.isDoubleClickEnabled());
        setDualArmAnimationEnabled(other.isDualArmAnimationEnabled());
        setGoonEnabled(other.isGoonEnabled());
        setTunnelBaseFinderEnabled(other.isTunnelBaseFinderEnabled());
        setAutoEatEnabled(other.isAutoEatEnabled());
        setAutoMineEnabled(other.isAutoMineEnabled());
        setAutoWalkEnabled(other.isAutoWalkEnabled());
        setLsdEnabled(other.isLsdEnabled());
        setAimAssistEnabled(other.isAimAssistEnabled());
        setAimFovEnabled(other.isAimFovEnabled());
        setAimAssistVisibleOnly(other.isAimAssistVisibleOnly());
        setFullBrightMethod(other.getFullBrightMethod());
        setTunnelBaseFinderMode(other.getTunnelBaseFinderMode());
        setAutoWalkDirection(other.getAutoWalkDirection());
        setMusicHudSource(other.getMusicHudSource());
        setTimeDebugMode(other.getTimeDebugMode());
        setRescanRateMs(other.getRescanRateMs());
        setDeltaPacketThreshold(other.getDeltaPacketThreshold());
        setBlockEspRangeBlocks(other.getBlockEspRangeBlocks());
        setStorageEspRangeBlocks(other.getStorageEspRangeBlocks());
        setStashFinderThreshold(other.getStashFinderThreshold());
        setHoleEspRangeBlocks(other.getHoleEspRangeBlocks());
        setAutoReconnectDelayMs(other.getAutoReconnectDelayMs());
        setDoubleClickDelayMs(other.getDoubleClickDelayMs());
        setTimeDebugMarkDelayMs(other.getTimeDebugMarkDelayMs());
        setPlayerChunkFastThresholdMs(other.getPlayerChunkFastThresholdMs());
        setPlayerChunkBaselineSamples(other.getPlayerChunkBaselineSamples());
        setFreeCamSpeed(other.getFreeCamSpeed());
        setFastUseCooldownTicks(other.getFastUseCooldownTicks());
        setRenderHeight(other.getRenderHeight());
        setMotionBlurFrames(other.getMotionBlurFrames());
        setCustomFovDegrees(other.getCustomFovDegrees());
        setTimeChangerValue(other.getTimeChangerValue());
        setAimAssistSlot(other.getAimAssistSlot());
        setAimAssistTarget(other.getAimAssistTarget());
        setAimFovRadius(other.getAimFovRadius());
        setAimAssistStrength(other.getAimAssistStrength());
        setFullBrightDefaultGamma(other.getFullBrightDefaultGamma());
        setSwingSpeedMultiplier(other.getSwingSpeedMultiplier());
        setBlockSelectionMode(other.getBlockSelectionMode());
        setBlockSelectionColor(other.getBlockSelectionColor());
        setAutoReconnectAddress(other.getAutoReconnectAddress());
        setNameProtectName(other.getNameProtectName());
        setSkinProtectName(other.getSkinProtectName());
        setFakeSpawnerBlockId(other.getFakeSpawnerBlockId());
        setFakeElytraItemId(other.getFakeElytraItemId());
        setChunkFinderKeyCode(other.getChunkFinderKeyCode());
        setTimeDebugKeyCode(other.getTimeDebugKeyCode());
        setBlockEspKeyCode(other.getBlockEspKeyCode());
        setStorageEspKeyCode(other.getStorageEspKeyCode());
        setFreeCamKeyCode(other.getFreeCamKeyCode());
        setFreeLookKeyCode(other.getFreeLookKeyCode());
        setFullBrightKeyCode(other.getFullBrightKeyCode());
        setItemNametagsKeyCode(other.getItemNametagsKeyCode());
        copyModuleKeyCodesFrom(other);
        setBlockEspTargets(other.getBlockEspTargets());
        setStorageEspTargets(other.getStorageEspTargets());
        setBlockNotifierTargets(other.getBlockNotifierTargets());
        setChatMacros(other.getChatMacros());
    }

    public void disableAllModules() {
        setChunkFinderEnabled(false);
        setTimeDebugEnabled(false);
        setPrimeChunkFinderEnabled(false);
        setBlockEspEnabled(false);
        setStorageEspEnabled(false);
        setStashFinderEnabled(false);
        setBlockNotifierEnabled(false);
        setBlockNotifierDisconnectEnabled(false);
        setPlayerNotifierEnabled(false);
        setPlayerEspEnabled(false);
        setMobEspEnabled(false);
        setSkinProtectEnabled(false);
        setSwingSpeedEnabled(false);
        setFreeCamEnabled(false);
        setFreeLookEnabled(false);
        setCrafterMacroEnabled(false);
        setChatMacroEnabled(false);
        setAutoRocketEnabled(false);
        setHoleEspEnabled(false);
        setBedrockHoleEspEnabled(false);
        setAutoReconnectEnabled(false);
        setAutoDisconnectEnabled(false);
        setAutoReplenishEnabled(false);
        setAutoBridgeEnabled(false);
        setAutoToolEnabled(false);
        setAutoCrystalEnabled(false);
        setFastUseEnabled(false);
        setFullBrightEnabled(false);
        setHudEnabled(false);
        setHudFpsEnabled(false);
        setHudMinimapEnabled(false);
        setHudClockEnabled(false);
        setHudArmorEnabled(false);
        setHudPotionEffectsEnabled(false);
        setHudMusicEnabled(false);
        setMotionBlurEnabled(false);
        setCustomFovEnabled(false);
        setNoRenderEnabled(false);
        setTimeChangerEnabled(false);
        setBreakProgressEnabled(false);
        setItemNametagsEnabled(false);
        setFakeMediaEnabled(false);
        setFakePayEnabled(false);
        setFakeStatsEnabled(false);
        setAutoTotemEnabled(false);
        setNameProtectEnabled(false);
        setFakeSpawnerEnabled(false);
        setFakeElytraEnabled(false);
        setBedrockProtectEnabled(false);
        setSilentSetHomeEnabled(false);
        setDoubleClickEnabled(false);
        setDualArmAnimationEnabled(false);
        setGoonEnabled(false);
        setTunnelBaseFinderEnabled(false);
        setAutoEatEnabled(false);
        setAutoMineEnabled(false);
        setAutoWalkEnabled(false);
        setLsdEnabled(false);
        setAimAssistEnabled(false);
        setAimFovEnabled(false);
        setAimAssistVisibleOnly(false);
        setBlockSelectionMode(BlockSelectionMode.OFF);
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

    private static EnumMap<ModuleKey, Integer> createDefaultModuleKeyCodes() {
        EnumMap<ModuleKey, Integer> keyCodes = new EnumMap<>(ModuleKey.class);
        for (ModuleKey key : ModuleKey.values()) {
            keyCodes.put(key, NO_KEY);
        }
        return keyCodes;
    }

    private void ensureModuleKeyCodes() {
        if (moduleKeyCodes == null) {
            moduleKeyCodes = createDefaultModuleKeyCodes();
        }

        for (ModuleKey key : ModuleKey.values()) {
            moduleKeyCodes.putIfAbsent(key, NO_KEY);
        }

        if (moduleKeyCodes.get(ModuleKey.CHUNK_FINDER) == NO_KEY && chunkFinderKeyCode != NO_KEY) {
            moduleKeyCodes.put(ModuleKey.CHUNK_FINDER, normalizeKeyCode(chunkFinderKeyCode));
        }
        if (moduleKeyCodes.get(ModuleKey.TIME_DEBUG) == NO_KEY && timeDebugKeyCode != NO_KEY) {
            moduleKeyCodes.put(ModuleKey.TIME_DEBUG, normalizeKeyCode(timeDebugKeyCode));
        }
        if (moduleKeyCodes.get(ModuleKey.BLOCK_ESP) == NO_KEY && blockEspKeyCode != NO_KEY) {
            moduleKeyCodes.put(ModuleKey.BLOCK_ESP, normalizeKeyCode(blockEspKeyCode));
        }
        if (moduleKeyCodes.get(ModuleKey.STORAGE_ESP) == NO_KEY && storageEspKeyCode != NO_KEY) {
            moduleKeyCodes.put(ModuleKey.STORAGE_ESP, normalizeKeyCode(storageEspKeyCode));
        }
        if (moduleKeyCodes.get(ModuleKey.FREE_CAM) == NO_KEY && freeCamKeyCode != NO_KEY) {
            moduleKeyCodes.put(ModuleKey.FREE_CAM, normalizeKeyCode(freeCamKeyCode));
        }
        if (moduleKeyCodes.get(ModuleKey.FREE_LOOK) == NO_KEY && freeLookKeyCode != NO_KEY) {
            moduleKeyCodes.put(ModuleKey.FREE_LOOK, normalizeKeyCode(freeLookKeyCode));
        }
        if (moduleKeyCodes.get(ModuleKey.FULL_BRIGHT) == NO_KEY && fullBrightKeyCode != NO_KEY) {
            moduleKeyCodes.put(ModuleKey.FULL_BRIGHT, normalizeKeyCode(fullBrightKeyCode));
        }
        if (moduleKeyCodes.get(ModuleKey.ITEM_NAMETAGS) == NO_KEY && itemNametagsKeyCode != NO_KEY) {
            moduleKeyCodes.put(ModuleKey.ITEM_NAMETAGS, normalizeKeyCode(itemNametagsKeyCode));
        }
    }

    private void copyModuleKeyCodesFrom(HuzzConfig other) {
        ensureModuleKeyCodes();
        for (ModuleKey key : ModuleKey.values()) {
            setModuleKeyCode(key, other.getModuleKeyCode(key));
        }
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

    private static String sanitizeAddress(String value, String fallback) {
        if (value == null) {
            return fallback;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return fallback;
        }

        return normalized.substring(0, Math.min(128, normalized.length()));
    }

    private static String sanitizeOptionalTarget(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? "" : normalized.substring(0, Math.min(128, normalized.length()));
    }

    private static List<ChatMacroEntry> normalizeChatMacros(List<ChatMacroEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return new ArrayList<>();
        }

        LinkedHashSet<Integer> seenKeys = new LinkedHashSet<>();
        ArrayList<ChatMacroEntry> normalized = new ArrayList<>();
        for (ChatMacroEntry entry : entries) {
            ChatMacroEntry clean = normalizeChatMacro(entry);
            if (clean == null || !seenKeys.add(clean.getKeyCode())) {
                continue;
            }
            normalized.add(clean);
        }
        return normalized;
    }

    private static ChatMacroEntry normalizeChatMacro(ChatMacroEntry entry) {
        if (entry == null) {
            return null;
        }

        String message = entry.getMessage() == null ? "" : entry.getMessage().trim();
        int keyCode = normalizeKeyCode(entry.getKeyCode());
        if (message.isEmpty() || keyCode == NO_KEY) {
            return null;
        }

        return new ChatMacroEntry(message.substring(0, Math.min(256, message.length())), keyCode);
    }

    public enum TimeDebugMode {
        CHUNK_FINDER,
        HEATMAP
    }

    public enum FullBrightMethod {
        GAMMA,
        NIGHT_VISION
    }

    public enum TunnelBaseFinderMode {
        MODE_3X3("3x3"),
        MODE_1X2("1x2");

        private final String label;

        TunnelBaseFinderMode(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public enum AutoWalkDirection {
        FORWARD("W"),
        LEFT("A"),
        BACK("S"),
        RIGHT("D");

        private final String label;

        AutoWalkDirection(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public enum MusicHudSource {
        MAC_SPOTIFY_APPLESCRIPT("mac spotify"),
        MAC_APPLE_MUSIC_APPLESCRIPT("mac music"),
        WINDOWS_SMTC("windows smtc");

        private final String label;

        MusicHudSource(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public enum FakeStatsMode {
        EDIT_ALL,
        KEEP_REAL_STATS_AND_UPDATE_MONEY
    }

    public enum AimAssistTarget {
        HEAD("Head"),
        NECK("Neck"),
        CHEST("Chest");

        private final String label;

        AimAssistTarget(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public enum BlockSelectionMode {
        OFF("Off"),
        FILLED("Solid"),
        WIREFRAME("Wire");

        private final String label;

        BlockSelectionMode(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public enum HighlightColor {
        CYAN("Cyan", 0x3FD5FF),
        LIME("Lime", 0x74F46A),
        GOLD("Gold", 0xFFC65A),
        RED("Red", 0xFF6A6A),
        MAGENTA("Magenta", 0xE478FF),
        WHITE("White", 0xF2F7FF);

        private final String label;
        private final int rgb;

        HighlightColor(String label, int rgb) {
            this.label = label;
            this.rgb = rgb;
        }

        public String label() {
            return label;
        }

        public int rgb() {
            return rgb;
        }
    }

    public enum ModuleKey {
        CHUNK_FINDER,
        TIME_DEBUG,
        PRIME_CHUNK_FINDER,
        PLAYER_CHUNKS,
        BLOCK_ESP,
        STORAGE_ESP,
        HOLE_ESP,
        BEDROCK_HOLE_ESP,
        BREAK_PROGRESS,
        STASH_FINDER,
        BLOCK_NOTIFIER,
        CHAT_MACRO,
        PLAYER_NOTIFIER,
        AUTO_DISCONNECT,
        PLAYER_ESP,
        MOB_ESP,
        SWING_SPEED,
        FREE_CAM,
        FREE_LOOK,
        AUTO_REPLENISH,
        AUTO_BRIDGE,
        AUTO_TOOL,
        AUTO_CRYSTAL,
        AIM_ASSIST,
        AIM_FOV,
        FAST_USE,
        CUSTOM_FOV,
        FULL_BRIGHT,
        MOTION_BLUR,
        NO_RENDER,
        TIME_CHANGER,
        BLOCK_SELECT,
        ITEM_NAMETAGS,
        FAKE_MEDIA,
        FAKE_PAY,
        FAKE_STATS,
        AUTO_TOTEM,
        AUTO_ROCKET,
        NAME_PROTECT,
        SKIN_PROTECT,
        AUTO_RECONNECT,
        HUD,
        MUSIC_HUD,
        CRAFTER_MACRO,
        FAKE_SPAWNER,
        FAKE_ELYTRA,
        BEDROCK_PROTECT,
        SILENT_SET_HOME,
        DOUBLE_CLICK,
        DUAL_ARM_ANIMATION,
        GOON,
        TUNNEL_BASE_FINDER,
        AUTO_EAT,
        AUTO_MINE,
        AUTO_WALK,
        LSD
    }

    public static final class ChatMacroEntry {
        private String message = "";
        private int keyCode = NO_KEY;

        public ChatMacroEntry() {
        }

        public ChatMacroEntry(String message, int keyCode) {
            this.message = message;
            this.keyCode = keyCode;
        }

        public String getMessage() {
            return message == null ? "" : message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getKeyCode() {
            return keyCode;
        }

        public void setKeyCode(int keyCode) {
            this.keyCode = keyCode;
        }

        public ChatMacroEntry copy() {
            return new ChatMacroEntry(getMessage(), getKeyCode());
        }
    }
}
