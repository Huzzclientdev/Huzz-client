package dev.david.huzzclient.mixin;

import dev.david.huzzclient.HuzzClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "getName", at = @At("RETURN"), cancellable = true)
    private void huzzclient$aliasItemName(CallbackInfoReturnable<Text> cir) {
        cir.setReturnValue(HuzzClient.getVisualAliasController().aliasItemName((ItemStack) (Object) this, cir.getReturnValue()));
    }

    @Inject(method = "getTooltip", at = @At("RETURN"), cancellable = true)
    private void huzzclient$aliasTooltip(Item.TooltipContext context, PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir) {
        cir.setReturnValue(HuzzClient.getVisualAliasController().aliasTooltip((ItemStack) (Object) this, context, player, type, cir.getReturnValue()));
    }
}
