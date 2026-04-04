package dev.david.huzzclient.mixin;

import dev.david.huzzclient.HuzzClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "hasStatusEffect", at = @At("HEAD"), cancellable = true)
    private void huzzclient$spoofNightVision(RegistryEntry<StatusEffect> effect, CallbackInfoReturnable<Boolean> cir) {
        if ((Object)this == MinecraftClient.getInstance().player
            && effect == StatusEffects.NIGHT_VISION
            && HuzzClient.getFullBrightController().isNightVisionActive()) {
            cir.setReturnValue(true);
        }
    }
}
