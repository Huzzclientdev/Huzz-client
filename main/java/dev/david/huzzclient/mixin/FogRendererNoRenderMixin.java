package dev.david.huzzclient.mixin;

import dev.david.huzzclient.HuzzClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class FogRendererNoRenderMixin {
    @Shadow
    private static boolean fogEnabled;

    @Unique
    private static final ThreadLocal<Boolean> huzzclient$previousFogEnabled = new ThreadLocal<>();

    @Inject(
        method = "applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;",
        at = @At("HEAD")
    )
    private static void huzzclient$beforeApplyFog(
        Camera camera,
        int viewDistance,
        RenderTickCounter tickCounter,
        float skyDarkness,
        ClientWorld world,
        CallbackInfoReturnable<Vector4f> cir
    ) {
        if (!huzzclient$shouldDisableOverworldFog(world)) {
            return;
        }

        huzzclient$previousFogEnabled.set(fogEnabled);
        fogEnabled = false;
    }

    @Inject(
        method = "applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;",
        at = @At("RETURN")
    )
    private static void huzzclient$afterApplyFog(
        Camera camera,
        int viewDistance,
        RenderTickCounter tickCounter,
        float skyDarkness,
        ClientWorld world,
        CallbackInfoReturnable<Vector4f> cir
    ) {
        Boolean previous = huzzclient$previousFogEnabled.get();
        if (previous == null) {
            return;
        }

        fogEnabled = previous;
        huzzclient$previousFogEnabled.remove();
    }

    @Unique
    private static boolean huzzclient$shouldDisableOverworldFog(ClientWorld world) {
        if (world == null || !World.OVERWORLD.equals(world.getRegistryKey())) {
            return false;
        }

        var config = HuzzClient.getConfigManager().getConfig();
        return config.isNoRenderEnabled() && config.isNoRenderOverworldFog();
    }
}
