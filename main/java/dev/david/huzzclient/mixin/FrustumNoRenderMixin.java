package dev.david.huzzclient.mixin;

import dev.david.huzzclient.HuzzClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Frustum.class)
public abstract class FrustumNoRenderMixin {
    @Inject(method = "isVisible(Lnet/minecraft/util/math/Box;)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void huzzclient$disableFrustumChunkCulling(Box box, CallbackInfoReturnable<Boolean> cir) {
        var config = HuzzClient.getConfigManager().getConfig();
        if (!config.isNoRenderEnabled() || !config.isNoRenderChunkCulling()) {
            return;
        }

        if (HuzzClient.getFreeCamController().isActive()) {
            cir.setReturnValue(true);
        }
    }
}
