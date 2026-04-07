package dev.david.huzzclient.mixin;

import dev.david.huzzclient.HuzzClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Redirect(
        method = "updateMouse",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"
        ),
        require = 0
    )
    private void huzzclient$applyFreeLook(ClientPlayerEntity player, double deltaX, double deltaY) {
        if (HuzzClient.getFreeCamController().applyMouseDelta(HuzzClient.client(), deltaX, deltaY)) {
            return;
        }
        if (HuzzClient.getFreeLookController().applyMouseDelta(HuzzClient.client(), deltaX, deltaY)) {
            return;
        }

        player.changeLookDirection(deltaX, deltaY);
    }
}
