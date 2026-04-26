package dev.david.huzzclient.mixin;

import dev.david.huzzclient.HuzzClient;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemModelManager.class)
public abstract class ItemModelManagerMixin {
    @ModifyVariable(method = "updateForLivingEntity", at = @At("HEAD"), argsOnly = true)
    private ItemStack huzzclient$aliasLivingItem(ItemStack stack) {
        return HuzzClient.getVisualAliasController().aliasItemStack(stack);
    }

    @ModifyVariable(method = "updateForNonLivingEntity", at = @At("HEAD"), argsOnly = true)
    private ItemStack huzzclient$aliasNonLivingItem(ItemStack stack) {
        return HuzzClient.getVisualAliasController().aliasItemStack(stack);
    }

    @ModifyVariable(method = "clearAndUpdate", at = @At("HEAD"), argsOnly = true)
    private ItemStack huzzclient$aliasClearAndUpdateItem(ItemStack stack) {
        return HuzzClient.getVisualAliasController().aliasItemStack(stack);
    }

    @ModifyVariable(method = "update", at = @At("HEAD"), argsOnly = true)
    private ItemStack huzzclient$aliasUpdatedItem(ItemStack stack) {
        return HuzzClient.getVisualAliasController().aliasItemStack(stack);
    }
}
