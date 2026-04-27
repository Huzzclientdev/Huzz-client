package dev.david.huzzclient.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DrawContext.class)
public interface DrawContextAccessor {
    @Invoker("drawTexturedQuad")
    void huzzclient$drawTexturedQuad(
        RenderPipeline pipeline,
        GpuTextureView texture,
        GpuSampler sampler,
        int x1,
        int y1,
        int x2,
        int y2,
        float u1,
        float v1,
        float u2,
        float v2,
        int color
    );
}
