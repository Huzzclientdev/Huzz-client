package dev.david.huzzclient.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BlockStateModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockRenderManager.class)
public abstract class BlockRenderManagerMixin {
    @Shadow
    public abstract BlockModels getModels();

    @Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
    private void huzzclient$aliasBlockModel(BlockState state, CallbackInfoReturnable<BlockStateModel> cir) {
        BlockState aliasedState = dev.david.huzzclient.HuzzClient.getVisualAliasController().aliasBlockState(state);
        if (aliasedState != null && aliasedState != state) {
            cir.setReturnValue(getModels().getModel(aliasedState));
        }
    }
}
