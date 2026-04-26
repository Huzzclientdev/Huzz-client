package dev.david.huzzclient.mixin;

import dev.david.huzzclient.ui.HuzzConfigScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void huzzclient$addConfigButton(CallbackInfo ci) {
        int buttonWidth = 98;
        int buttonHeight = 20;
        int x = this.width - buttonWidth - 8;
        int y = 8;
        addDrawableChild(ButtonWidget.builder(
            Text.literal("HuzzClient"),
            button -> {
                if (client != null) {
                    client.setScreen(new HuzzConfigScreen(this));
                }
            }
        ).dimensions(x, y, buttonWidth, buttonHeight).build());
    }
}
