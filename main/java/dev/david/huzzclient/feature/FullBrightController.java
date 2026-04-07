package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfigManager;
import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.mixin.SimpleOptionAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.util.math.MathHelper;

public final class FullBrightController {
    private static final double GAMMA_TARGET = 16.0D;
    private static final double GAMMA_STEP = 0.5D;
    private static final float NIGHT_VISION_STEP = 0.03125F;

    private final HuzzConfigManager configManager;

    private boolean startupChecked;
    private boolean wasGammaChanged;
    private float nightVisionStrength;

    public FullBrightController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        if (!startupChecked) {
            checkGammaOnStartup(client);
        }

        HuzzConfig config = configManager.getConfig();
        updateGamma(client, config);
        updateNightVision(config);
    }

    public boolean isNightVisionActive() {
        return nightVisionStrength > 0.0F;
    }

    public float getNightVisionStrength() {
        return nightVisionStrength;
    }

    private void checkGammaOnStartup(MinecraftClient client) {
        double gamma = client.options.getGamma().getValue();
        if (gamma > 1.0D) {
            wasGammaChanged = true;
        } else {
            configManager.getConfig().setFullBrightDefaultGamma(gamma);
        }

        startupChecked = true;
    }

    private void updateGamma(MinecraftClient client, HuzzConfig config) {
        if (isChangingGamma(config)) {
            setGamma(client, GAMMA_TARGET, config.isFullBrightFade());
            return;
        }

        if (wasGammaChanged) {
            resetGamma(client, config.getFullBrightDefaultGamma(), config.isFullBrightFade());
        }
    }

    private void setGamma(MinecraftClient client, double target, boolean fade) {
        wasGammaChanged = true;
        SimpleOption<Double> gammaOption = client.options.getGamma();
        double oldGammaValue = gammaOption.getValue();

        if (!fade || Math.abs(oldGammaValue - target) <= GAMMA_STEP) {
            forceGammaValue(gammaOption, target);
            return;
        }

        forceGammaValue(gammaOption, oldGammaValue < target ? oldGammaValue + GAMMA_STEP : oldGammaValue - GAMMA_STEP);
    }

    private void resetGamma(MinecraftClient client, double target, boolean fade) {
        SimpleOption<Double> gammaOption = client.options.getGamma();
        double oldGammaValue = gammaOption.getValue();

        if (!fade || Math.abs(oldGammaValue - target) <= GAMMA_STEP) {
            forceGammaValue(gammaOption, target);
            wasGammaChanged = false;
            return;
        }

        forceGammaValue(gammaOption, oldGammaValue < target ? oldGammaValue + GAMMA_STEP : oldGammaValue - GAMMA_STEP);
    }

    private void updateNightVision(HuzzConfig config) {
        boolean shouldGiveNightVision = config.isFullBrightEnabled()
            && config.getFullBrightMethod() == HuzzConfig.FullBrightMethod.NIGHT_VISION;

        if (config.isFullBrightFade()) {
            nightVisionStrength += shouldGiveNightVision ? NIGHT_VISION_STEP : -NIGHT_VISION_STEP;
            nightVisionStrength = MathHelper.clamp(nightVisionStrength, 0.0F, 1.0F);
            return;
        }

        nightVisionStrength = shouldGiveNightVision ? 1.0F : 0.0F;
    }

    private boolean isChangingGamma(HuzzConfig config) {
        return config.isFullBrightEnabled() && config.getFullBrightMethod() == HuzzConfig.FullBrightMethod.GAMMA;
    }

    @SuppressWarnings("unchecked")
    private static void forceGammaValue(SimpleOption<Double> gammaOption, double value) {
        ((SimpleOptionAccessor<Double>) (Object) gammaOption).huzzclient$setValue(value);
    }
}
