package dev.david.huzzclient.mixin;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = GameRenderer.class, priority = 1100)
public abstract class GameRendererMotionBlurMixin {
}
