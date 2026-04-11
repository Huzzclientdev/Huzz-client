package dev.david.huzzclient.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import dev.david.huzzclient.render.MotionBlurRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMotionBlurMixin {
    @Unique
    private final Matrix4f huzzclient$prevModelView = new Matrix4f();
    @Unique
    private final Matrix4f huzzclient$prevProjection = new Matrix4f();
    @Unique
    private double huzzclient$prevCamX;
    @Unique
    private double huzzclient$prevCamY;
    @Unique
    private double huzzclient$prevCamZ;

    @Inject(
        method = "render(Lnet/minecraft/client/util/ObjectAllocator;Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V",
        at = @At("HEAD")
    )
    private void huzzclient$captureMotionBlurState(
        ObjectAllocator allocator,
        RenderTickCounter tickCounter,
        boolean renderBlockOutline,
        Camera camera,
        Matrix4f positionMatrix,
        Matrix4f basicProjectionMatrix,
        Matrix4f projectionMatrix,
        GpuBufferSlice fogBuffer,
        Vector4f fogColor,
        boolean renderSky,
        CallbackInfo ci
    ) {
        MotionBlurRenderer.captureAllocator(allocator);
        Vec3d cameraPos = camera.getCameraPos();
        MotionBlurRenderer.setFrameMotionBlur(
            positionMatrix,
            huzzclient$prevModelView,
            projectionMatrix,
            huzzclient$prevProjection,
            (float) (cameraPos.x - huzzclient$prevCamX),
            (float) (cameraPos.y - huzzclient$prevCamY),
            (float) (cameraPos.z - huzzclient$prevCamZ)
        );
        huzzclient$prevModelView.set(positionMatrix);
        huzzclient$prevProjection.set(projectionMatrix);
        huzzclient$prevCamX = cameraPos.x;
        huzzclient$prevCamY = cameraPos.y;
        huzzclient$prevCamZ = cameraPos.z;
    }
}
