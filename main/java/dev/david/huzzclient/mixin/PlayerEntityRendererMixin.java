package dev.david.huzzclient.mixin;

import dev.david.huzzclient.HuzzClient;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.PlayerLikeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
    @Inject(method = "updateRenderState", at = @At("TAIL"))
    private void huzzclient$decoratePlayerName(PlayerLikeEntity player, PlayerEntityRenderState state, float tickDelta, CallbackInfo ci) {
        state.equippedChestStack = HuzzClient.getVisualAliasController().aliasChestEquipment(state.equippedChestStack);
        if (state.displayName != null) {
            state.displayName = HuzzClient.getPlayerNameController().decorateRenderName(player, state.displayName);
        }
    }

    @Inject(method = "hasLabel(Lnet/minecraft/entity/PlayerLikeEntity;D)Z", at = @At("RETURN"), cancellable = true)
    private void huzzclient$showOwnNameTag(PlayerLikeEntity player, double squaredDistance, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && HuzzClient.getPlayerNameController().shouldShowOwnNameTag(player)) {
            cir.setReturnValue(true);
        }
    }
}
