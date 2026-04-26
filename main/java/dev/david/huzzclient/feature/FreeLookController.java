package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;

public final class FreeLookController {
    private static final float CAMERA_DISTANCE = 4.0F;
    private static final float LOOK_DELTA_MULTIPLIER = 0.15F;
    private static final float SMOOTHING_FACTOR = 0.20F;

    private final HuzzConfigManager configManager;

    private float previousCameraYaw;
    private float previousCameraPitch;
    private float cameraYaw;
    private float cameraPitch;
    private float targetCameraYaw;
    private float targetCameraPitch;
    private float lockedPlayerYaw;
    private float lockedPlayerPitch;
    private float lockedPlayerHeadYaw;
    private float lockedPlayerBodyYaw;
    private Perspective previousPerspective;
    private boolean active;

    public FreeLookController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            clear();
            return;
        }

        ClientPlayerEntity player = client.player;

        HuzzConfig config = configManager.getConfig();
        boolean shouldBeActive = config.isFreeLookEnabled() && !config.isFreeCamEnabled();
        if (!shouldBeActive) {
            clear();
            return;
        }

        if (!active) {
            cameraYaw = player.getYaw();
            cameraPitch = player.getPitch();
            previousCameraYaw = cameraYaw;
            previousCameraPitch = cameraPitch;
            targetCameraYaw = cameraYaw;
            targetCameraPitch = cameraPitch;
            lockedPlayerYaw = player.getYaw();
            lockedPlayerPitch = player.getPitch();
            lockedPlayerHeadYaw = player.getHeadYaw();
            lockedPlayerBodyYaw = player.getBodyYaw();
            if (previousPerspective == null) {
                previousPerspective = client.options.getPerspective();
                client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
            }
            active = true;
            return;
        }

        previousCameraYaw = cameraYaw;
        previousCameraPitch = cameraPitch;
        cameraYaw += MathHelper.wrapDegrees(targetCameraYaw - cameraYaw) * SMOOTHING_FACTOR;
        cameraPitch += (targetCameraPitch - cameraPitch) * SMOOTHING_FACTOR;
        cameraPitch = MathHelper.clamp(cameraPitch, -90.0F, 90.0F);

        player.setYaw(lockedPlayerYaw);
        player.setPitch(lockedPlayerPitch);
        player.setHeadYaw(lockedPlayerHeadYaw);
        player.setBodyYaw(lockedPlayerBodyYaw);
    }

    public boolean applyMouseDelta(MinecraftClient client, double deltaX, double deltaY) {
        if (!active || client.player == null || client.currentScreen != null) {
            return false;
        }

        targetCameraYaw += (float) (deltaX * LOOK_DELTA_MULTIPLIER);
        targetCameraPitch = MathHelper.clamp(targetCameraPitch + (float) (deltaY * LOOK_DELTA_MULTIPLIER), -90.0F, 90.0F);
        return true;
    }

    public boolean shouldBlockVanillaInput() {
        return active;
    }

    public boolean isActive() {
        return active;
    }

    public float getCameraYaw() {
        return cameraYaw;
    }

    public float getCameraPitch() {
        return cameraPitch;
    }

    public float getCameraYaw(float tickDelta) {
        return previousCameraYaw + MathHelper.wrapDegrees(cameraYaw - previousCameraYaw) * MathHelper.clamp(tickDelta, 0.0F, 1.0F);
    }

    public float getCameraPitch(float tickDelta) {
        return previousCameraPitch + (cameraPitch - previousCameraPitch) * MathHelper.clamp(tickDelta, 0.0F, 1.0F);
    }

    public float getCameraDistance() {
        return CAMERA_DISTANCE;
    }

    public void clear() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (previousPerspective != null) {
            client.options.setPerspective(previousPerspective);
            previousPerspective = null;
        }
        active = false;
        previousCameraYaw = 0.0F;
        previousCameraPitch = 0.0F;
    }
}
