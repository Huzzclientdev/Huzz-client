package dev.david.huzzclient.mixin;

import dev.david.huzzclient.HuzzClient;
import dev.david.huzzclient.feature.FakeStatsController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", at = @At("HEAD"))
    private void huzzclient$beginFakeStatsSidebar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        HuzzClient.getFakeStatsController().beginRender(MinecraftClient.getInstance());
    }

    @ModifyArgs(
        method = "method_55439(Lnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/number/NumberFormat;Lnet/minecraft/scoreboard/ScoreboardEntry;)Lnet/minecraft/client/gui/hud/InGameHud$SidebarEntry;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/hud/InGameHud$SidebarEntry;<init>(Lnet/minecraft/text/Text;Lnet/minecraft/text/Text;I)V"
        )
    )
    private void huzzclient$rewriteFakeStatsSidebarEntry(Args args) {
        Text renderedName = args.get(0);
        Text renderedScore = args.get(1);
        FakeStatsController.LiveSidebarEntry updatedEntry = HuzzClient.getFakeStatsController().rewriteLiveSidebarEntry(renderedName, renderedScore);
        if (updatedEntry.name() == renderedName && updatedEntry.score() == renderedScore) {
            return;
        }

        args.set(0, updatedEntry.name());
        args.set(1, updatedEntry.score());
        args.set(2, getTextRenderer().getWidth(updatedEntry.score()));
    }

    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", at = @At("TAIL"))
    private void huzzclient$endFakeStatsSidebar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        HuzzClient.getFakeStatsController().endRender(MinecraftClient.getInstance());
    }
}
