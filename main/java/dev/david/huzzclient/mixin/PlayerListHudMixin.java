package dev.david.huzzclient.mixin;

import dev.david.huzzclient.HuzzClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {
    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
    private void huzzclient$decoratePlayerListName(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        cir.setReturnValue(HuzzClient.getPlayerNameController().decoratePlayerListName(entry, cir.getReturnValue()));
    }
}
