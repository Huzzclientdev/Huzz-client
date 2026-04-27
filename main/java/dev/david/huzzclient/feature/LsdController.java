package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;

public final class LsdController {
    private final HuzzConfigManager configManager;

    private long startedAtMs;
    private float jitter;

    public LsdController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        HuzzConfig config = configManager.getConfig();
        if (client.player == null || client.world == null || !config.isLsdEnabled()) {
            clear();
            return;
        }

        if (startedAtMs == 0L) {
            startedAtMs = Util.getMeasuringTimeMs();
        }

        // High-frequency micro-jitter layered on slower waves for disorienting visuals.
        jitter = (float) ((Math.random() - 0.5D) * 4.0D);
    }

    public void clear() {
        startedAtMs = 0L;
        jitter = 0.0F;
    }

    public boolean isActive() {
        return configManager.getConfig().isLsdEnabled() && startedAtMs != 0L;
    }

    public float fovMultiplier() {
        if (!isActive()) {
            return 1.0F;
        }
        float t = elapsedSeconds();
        return 1.0F + (float) Math.sin(t * 2.2F) * 0.28F + (float) Math.sin(t * 6.5F) * 0.12F;
    }

    public float cameraYawOffset() {
        if (!isActive()) {
            return 0.0F;
        }
        float t = elapsedSeconds();
        return (float) Math.sin(t * 1.6F) * 6.5F + jitter;
    }

    public float cameraPitchOffset() {
        if (!isActive()) {
            return 0.0F;
        }
        float t = elapsedSeconds();
        return (float) Math.cos(t * 1.9F) * 3.0F + jitter * 0.45F;
    }

    public float cameraBobX() {
        if (!isActive()) {
            return 0.0F;
        }
        return (float) Math.sin(elapsedSeconds() * 3.3F) * 0.18F;
    }

    public float cameraBobY() {
        if (!isActive()) {
            return 0.0F;
        }
        return (float) Math.cos(elapsedSeconds() * 2.8F) * 0.12F;
    }

    public float cameraBobZ() {
        if (!isActive()) {
            return 0.0F;
        }
        return (float) Math.sin(elapsedSeconds() * 2.1F) * 0.15F;
    }

    public float shaderTime() {
        return isActive() ? elapsedSeconds() : 0.0F;
    }

    public float colorIntensity() {
        if (!isActive()) {
            return 0.0F;
        }
        float t = elapsedSeconds();
        return 0.85F + 0.45F * (0.5F + 0.5F * (float) Math.sin(t * 1.3F));
    }

    public float morphStrength() {
        if (!isActive()) {
            return 0.0F;
        }
        float t = elapsedSeconds();
        return 1.0F + 0.35F * (0.5F + 0.5F * (float) Math.sin(t * 1.9F));
    }

    public float aberrationStrength() {
        if (!isActive()) {
            return 0.0F;
        }
        float t = elapsedSeconds();
        return 1.0F + 0.5F * (0.5F + 0.5F * (float) Math.sin(t * 2.4F + 1.7F));
    }

    private float elapsedSeconds() {
        return (Util.getMeasuringTimeMs() - startedAtMs) / 1000.0F;
    }
}
