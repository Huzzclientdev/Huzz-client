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
        if (!config.isDualArmAnimationEnabled() || client.player == null || state.id != client.player.getId()) {
            return;
        }

        float wave = MathHelper.sin(state.age * 0.45F);
        model.rightArm.pitch = -1.6F + wave * 1.25F;
        model.leftArm.pitch = -1.6F - wave * 1.25F;
        model.rightArm.yaw = 0.2F;
        model.leftArm.yaw = -0.2F;
        model.rightArm.roll = 0.18F;
        model.leftArm.roll = -0.18F;
        model.rightSleeve.setTransform(model.rightArm.getTransform());
        model.leftSleeve.setTransform(model.leftArm.getTransform());
    }
}
