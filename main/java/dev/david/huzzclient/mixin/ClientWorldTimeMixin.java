package dev.david.huzzclient.mixin;

import dev.david.huzzclient.HuzzClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class ClientWorldTimeMixin {
    @Inject(method = "getTimeOfDay", at = @At("RETURN"), cancellable = true)
    private void huzzclient$overrideClientTime(CallbackInfoReturnable<Long> cir) {
        if ((Object) this instanceof ClientWorld) {
            cir.setReturnValue(HuzzClient.getTimeChangerController().overrideTime(cir.getReturnValue()));
        }
    }
}
