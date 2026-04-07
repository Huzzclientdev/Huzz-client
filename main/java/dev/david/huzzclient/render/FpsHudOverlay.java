package dev.david.huzzclient.render;

import dev.david.huzzclient.HuzzClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

public final class FpsHudOverlay {
    private FpsHudOverlay() {
    }

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.hudHidden || !HuzzClient.getConfigManager().getConfig().isFpsHudEnabled()) {
            return;
        }

        String label = "FPS: " + client.getCurrentFps();
        int x = context.getScaledWindowWidth() - client.textRenderer.getWidth(label) - 6;
        context.drawTextWithShadow(client.textRenderer, Text.literal(label), x, 6, 0xFFFFFFFF);
    }
}
