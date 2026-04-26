package dev.david.huzzclient.mixin;

import dev.david.huzzclient.HuzzClient;
import dev.david.huzzclient.config.HuzzConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "getNightVisionStrength", at = @At("HEAD"), cancellable = true)
    private static void huzzclient$forceNightVision(LivingEntity entity, float tickProgress, CallbackInfoReturnable<Float> cir) {
        if (HuzzClient.getFullBrightController().isNightVisionActive()) {
            cir.setReturnValue(HuzzClient.getFullBrightController().getNightVisionStrength());
        }
    }

    @Inject(method = "updateCrosshairTarget", at = @At("TAIL"), require = 0)
    private void huzzclient$updateFreeCamCrosshairTarget(float tickProgress, CallbackInfo ci) {
        if (!HuzzClient.getFreeCamController().isActive() || client.player == null) {
            return;
        }

        HitResult hitResult = HuzzClient.getFreeCamController().getCrosshairTarget(client.player, tickProgress);
        client.crosshairTarget = hitResult;
        client.targetedEntity = hitResult instanceof EntityHitResult entityHit ? entityHit.getEntity() : null;
    }

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void huzzclient$overrideFov(CallbackInfoReturnable<Float> cir) {
        HuzzConfig config = HuzzClient.getConfigManager().getConfig();
        float fov = cir.getReturnValueF();
        if (config.isCustomFovEnabled()) {
            fov = (float) config.getCustomFovDegrees();
        }
        if (config.isLsdEnabled()) {
            fov *= HuzzClient.getLsdController().fovMultiplier();
        }
        cir.setReturnValue(fov);
    }

}
