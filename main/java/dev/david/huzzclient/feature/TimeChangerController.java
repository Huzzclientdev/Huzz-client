package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;

public final class TimeChangerController {
    private final HuzzConfigManager configManager;

    public TimeChangerController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public long overrideTime(long originalTime) {
        HuzzConfig config = configManager.getConfig();
        return config.isTimeChangerEnabled() ? config.getTimeChangerValue() : originalTime;
    }
}
