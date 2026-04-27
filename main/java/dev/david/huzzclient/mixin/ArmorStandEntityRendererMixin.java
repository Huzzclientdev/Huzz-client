package dev.david.huzzclient.mixin;

import dev.david.huzzclient.HuzzClient;
import net.minecraft.client.render.entity.ArmorStandEntityRenderer;
import net.minecraft.client.render.entity.state.ArmorStandEntityRenderState;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorStandEntityRenderer.class)
public abstract class ArmorStandEntityRendererMixin {
    @Inject(method = "updateRenderState", at = @At("TAIL"))
    private void huzzclient$aliasArmorStandChest(ArmorStandEntity entity, ArmorStandEntityRenderState state, float tickDelta, CallbackInfo ci) {
        state.equippedChestStack = HuzzClient.getVisualAliasController().aliasChestEquipment(state.equippedChestStack);
    }
}
