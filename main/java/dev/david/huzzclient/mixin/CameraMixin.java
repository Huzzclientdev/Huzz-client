package dev.david.huzzclient.mixin;

import dev.david.huzzclient.HuzzClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void setPos(Vec3d pos);

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Shadow
    protected abstract void moveBy(float x, float y, float z);

    @Invoker("clipToSpace")
    protected abstract float huzzclient$clipToSpace(float distance);

    @Inject(method = "update", at = @At("TAIL"), require = 0)
    private void huzzclient$applyFreeCam(World area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickProgress, CallbackInfo ci) {
        if (HuzzClient.client().player == null) {
            return;
        }

        if (HuzzClient.getFreeCamController().isActive()) {
            setRotation(
                HuzzClient.getFreeCamController().getCameraYaw(),
                HuzzClient.getFreeCamController().getCameraPitch()
            );
            setPos(HuzzClient.getFreeCamController().getCameraPos(tickProgress));
            return;
        }

        if (HuzzClient.getFreeLookController().isActive()) {
            setPos(HuzzClient.client().player.getCameraPosVec(tickProgress));
            setRotation(
                HuzzClient.getFreeLookController().getCameraYaw(),
                HuzzClient.getFreeLookController().getCameraPitch()
            );
            moveBy(-huzzclient$clipToSpace(HuzzClient.getFreeLookController().getCameraDistance()), 0.0F, 0.0F);
        }
    }
}
