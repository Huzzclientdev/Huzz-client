package dev.david.huzzclient.mixin;

import dev.david.huzzclient.render.MotionBlurRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameRenderer.class, priority = 1100)
public abstract class GameRendererMotionBlurMixin {
    @Inject(
        method = "render(Lnet/minecraft/client/render/RenderTickCounter;Z)V",
        at = @At("TAIL")
    )
    private void huzzclient$applyMotionBlur(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        MotionBlurRenderer.applyMotionBlur(tickCounter);
    }
}
