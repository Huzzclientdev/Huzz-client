package dev.david.huzzclient.mixin;

import dev.david.huzzclient.HuzzClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.network.ClientPlayerEntity;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
        if (HuzzClient.getAimAssistController().applyMouseDelta(HuzzClient.client(), player, deltaX, deltaY)) {
            return;
        }

        player.changeLookDirection(deltaX, deltaY);
    }

    @Inject(method = "onMouseButton", at = @At("TAIL"))
    private void huzzclient$recordDoubleClick(long window, MouseInput button, int action, CallbackInfo ci) {
        if (action == GLFW.GLFW_PRESS) {
            HuzzClient.getDoubleClickController().recordClick(HuzzClient.client(), button.button());
            if (button.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                HuzzClient.getAutoCrystalController().recordUse(HuzzClient.client());
            }
        }
    }
}
