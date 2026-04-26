package dev.david.huzzclient.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

final class HuzzActionButton extends ClickableWidget {
    private final Consumer<HuzzActionButton> onPress;
    private final boolean accent;

    HuzzActionButton(int x, int y, int width, int height, Text message, boolean accent, Consumer<HuzzActionButton> onPress) {
        super(x, y, width, height, message);
        this.accent = accent;
        this.onPress = onPress;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        int outlineColor = accent ? HuzzUi.PANEL_OUTLINE : HuzzUi.withAlpha(HuzzUi.TEXT_SECONDARY, 120);
        int baseColor = accent ? HuzzUi.CHIP_ACCENT_FILL : HuzzUi.withAlpha(HuzzUi.PANEL_BACKGROUND_ALT, 210);

        if (!active) {
            outlineColor = HuzzUi.withAlpha(HuzzUi.TEXT_MUTED, 90);
            baseColor = HuzzUi.withAlpha(HuzzUi.PANEL_BACKGROUND, 150);
        } else if (isHovered()) {
            baseColor = accent ? 0xFF443066 : 0xEE2A2A31;
        }

        HuzzUi.drawRoundedRect(context, getX(), getY(), getRight(), getBottom(), outlineColor, 3);
        HuzzUi.drawRoundedRect(context, getX() + 1, getY() + 1, getRight() - 1, getBottom() - 1, baseColor, 2);

        int textColor = active ? (accent ? HuzzUi.TEXT_PRIMARY : HuzzUi.TEXT_SECONDARY) : HuzzUi.TEXT_MUTED;
        context.drawCenteredTextWithShadow(
            MinecraftClient.getInstance().textRenderer,
            getMessage(),
            getX() + width / 2,
            getY() + (height - 8) / 2,
            textColor
        );
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        appendDefaultNarrations(builder);
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        onPress.accept(this);
    }
}
