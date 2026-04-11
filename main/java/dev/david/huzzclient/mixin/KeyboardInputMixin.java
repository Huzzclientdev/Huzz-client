package dev.david.huzzclient.mixin;

import dev.david.huzzclient.HuzzClient;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin {
    @Inject(method = "tick", at = @At("TAIL"), require = 0)
    private void huzzclient$blockVanillaMovement(CallbackInfo ci) {
        if (!HuzzClient.getFreeCamController().shouldBlockVanillaInput()) {
            return;
        }

        KeyboardInput input = (KeyboardInput)(Object)this;
        input.playerInput = PlayerInput.DEFAULT;
        ((InputAccessor)input).huzzclient$setMovementVector(Vec2f.ZERO);
    }
}
