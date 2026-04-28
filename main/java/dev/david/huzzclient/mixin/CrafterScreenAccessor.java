package dev.david.huzzclient.mixin;

import net.minecraft.client.gui.screen.ingame.CrafterScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CrafterScreen.class)
public interface CrafterScreenAccessor {
    @Invoker("setSlotEnabled")
    void huzzclient$setSlotEnabled(int slot, boolean enabled);
}
