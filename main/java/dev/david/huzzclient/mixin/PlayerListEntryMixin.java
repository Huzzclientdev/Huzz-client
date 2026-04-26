package dev.david.huzzclient.mixin;

import dev.david.huzzclient.HuzzClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin {
    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    private void huzzclient$applyTabSkinProtect(CallbackInfoReturnable<SkinTextures> cir) {
        PlayerListEntry entry = (PlayerListEntry) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && entry.getProfile().id().equals(client.player.getUuid())) {
            cir.setReturnValue(HuzzClient.getSkinProtectController().applyOwnSkin(cir.getReturnValue()));
        }
    }
}
