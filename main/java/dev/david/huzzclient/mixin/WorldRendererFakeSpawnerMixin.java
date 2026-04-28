package dev.david.huzzclient.mixin;

import dev.david.huzzclient.render.FakeSpawnerRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererFakeSpawnerMixin {
    @Shadow
    private ClientWorld world;

    @Shadow
    private EntityRenderManager entityRenderManager;

    @Inject(method = "renderBlockEntities", at = @At("TAIL"))
    private void huzzclient$renderFakeSpawnerEntities(
        MatrixStack matrices,
        WorldRenderState renderStates,
        OrderedRenderCommandQueueImpl queue,
        CallbackInfo ci
    ) {
        FakeSpawnerRenderer.render(
            world,
            entityRenderManager,
            renderStates,
            matrices,
            queue,
            entityRenderManager == null ? 0.0F : net.minecraft.client.MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(false)
        );
    }
}
