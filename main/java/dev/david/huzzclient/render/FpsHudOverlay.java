package dev.david.huzzclient.render;

import dev.david.huzzclient.HuzzClient;
import dev.david.huzzclient.config.HuzzConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public final class FpsHudOverlay {
    private static final int BREAK_BAR_WIDTH = 58;
    private static final int BREAK_BAR_HEIGHT = 5;
    private static final float BREAK_BAR_SMOOTHING = 0.22F;

    private static float displayedBreakProgress;

    private FpsHudOverlay() {
    }

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        HuzzConfig config = HuzzClient.getConfigManager().getConfig();
        if (client.options.hudHidden) {
            return;
        }

        renderBreakProgress(context, client, config);
        renderFps(context, client, config);
    }

    private static void renderFps(DrawContext context, MinecraftClient client, HuzzConfig config) {
        if (!config.isFpsHudEnabled()) {
            return;
        }

        String label = "FPS: " + client.getCurrentFps();
        int x = context.getScaledWindowWidth() - client.textRenderer.getWidth(label) - 6;
        context.drawTextWithShadow(client.textRenderer, Text.literal(label), x, 6, 0xFFFFFFFF);
    }

    private static void renderBreakProgress(DrawContext context, MinecraftClient client, HuzzConfig config) {
        if (!config.isBreakProgressEnabled() || client.interactionManager == null) {
            displayedBreakProgress = 0.0F;
            return;
        }

        float targetProgress = client.interactionManager.isBreakingBlock()
            ? MathHelper.clamp(client.interactionManager.getBlockBreakingProgress() / 10.0F, 0.0F, 1.0F)
            : 0.0F;
        displayedBreakProgress += (targetProgress - displayedBreakProgress) * BREAK_BAR_SMOOTHING;
        if (displayedBreakProgress <= 0.003F) {
            displayedBreakProgress = 0.0F;
            return;
        }

        int centerX = context.getScaledWindowWidth() / 2;
        int centerY = context.getScaledWindowHeight() / 2;
        int left = centerX - BREAK_BAR_WIDTH / 2;
        int top = centerY - 18;
        int right = left + BREAK_BAR_WIDTH;
        int bottom = top + BREAK_BAR_HEIGHT;
        int fillRight = left + Math.max(1, Math.round((BREAK_BAR_WIDTH - 2) * displayedBreakProgress)) + 1;
        context.fill(left, top, right, bottom, 0x99000000);
        context.fill(left + 1, top + 1, fillRight, bottom - 1, 0xFF67E8A1);
    }
}
