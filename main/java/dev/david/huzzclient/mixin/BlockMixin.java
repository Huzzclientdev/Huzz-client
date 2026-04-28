package dev.david.huzzclient.mixin;

import dev.david.huzzclient.HuzzClient;
import net.minecraft.block.Block;
import net.minecraft.text.MutableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class BlockMixin {
    @Inject(method = "getName", at = @At("RETURN"), cancellable = true)
    private void huzzclient$aliasBlockName(CallbackInfoReturnable<MutableText> cir) {
        cir.setReturnValue((MutableText) HuzzClient.getVisualAliasController().aliasBlockName((Block) (Object) this, cir.getReturnValue()));
    }
}
