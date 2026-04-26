package dev.david.huzzclient.mixin;

import dev.david.huzzclient.HuzzClient;
import dev.david.huzzclient.config.HuzzConfig;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @ModifyConstant(method = "doItemUse", constant = @Constant(intValue = 4))
    private int huzzclient$adjustFastUseCooldown(int vanillaCooldown) {
        HuzzConfig config = HuzzClient.getConfigManager().getConfig();
        if (!config.isFastUseEnabled()) {
            return vanillaCooldown;
        }
        return config.getFastUseCooldownTicks();
    }
}
