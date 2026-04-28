package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.util.math.MathHelper;

public final class DualArmAnimationController {
    private final HuzzConfigManager configManager;

    public DualArmAnimationController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void apply(PlayerEntityModel model, PlayerEntityRenderState state) {
        HuzzConfig config = configManager.getConfig();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || state.id != client.player.getId()) {
            return;
        }

        if (config.isGoonEnabled()) {
            float wave = MathHelper.sin(state.age * 1.4F);
            model.rightArm.pitch = -0.75F + wave * 0.55F;
            model.rightArm.yaw = -0.24F;
            model.rightArm.roll = 0.06F + wave * 0.05F;
            model.rightSleeve.pitch = model.rightArm.pitch;
            model.rightSleeve.yaw = model.rightArm.yaw;
            model.rightSleeve.roll = model.rightArm.roll;
            return;
        }

        if (!config.isDualArmAnimationEnabled()) {
            return;
        }

        float wave = MathHelper.sin(state.age * 0.45F);
        model.rightArm.pitch = -1.6F + wave * 1.25F;
        model.leftArm.pitch = -1.6F - wave * 1.25F;
        model.rightArm.yaw = 0.2F;
        model.leftArm.yaw = -0.2F;
        model.rightArm.roll = 0.18F;
        model.leftArm.roll = -0.18F;
        model.rightSleeve.pitch = model.rightArm.pitch;
        model.rightSleeve.yaw = model.rightArm.yaw;
        model.rightSleeve.roll = model.rightArm.roll;
        model.leftSleeve.pitch = model.leftArm.pitch;
        model.leftSleeve.yaw = model.leftArm.yaw;
        model.leftSleeve.roll = model.leftArm.roll;
    }
}
