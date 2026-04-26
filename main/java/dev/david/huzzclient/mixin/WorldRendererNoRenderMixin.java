package dev.david.huzzclient.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import dev.david.huzzclient.HuzzClient;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererNoRenderMixin {
    @Inject(
        method = "renderWeather(Lnet/minecraft/client/render/FrameGraphBuilder;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void huzzclient$skipWeather(FrameGraphBuilder frameGraphBuilder, GpuBufferSlice fogBuffer, CallbackInfo ci) {
        var config = HuzzClient.getConfigManager().getConfig();
        if (config.isNoRenderEnabled() && config.isNoRenderWeather()) {
            ci.cancel();
        }
    }

    @Inject(
        method = "renderParticles(Lnet/minecraft/client/render/FrameGraphBuilder;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void huzzclient$skipParticles(FrameGraphBuilder frameGraphBuilder, GpuBufferSlice fogBuffer, CallbackInfo ci) {
        var config = HuzzClient.getConfigManager().getConfig();
        if (config.isNoRenderEnabled() && config.isNoRenderParticles()) {
            ci.cancel();
        }
    }
}
