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
    private final boolean lightGrayStyle;

    HuzzActionButton(int x, int y, int width, int height, Text message, boolean accent, Consumer<HuzzActionButton> onPress) {
        this(x, y, width, height, message, accent, false, onPress);
    }

    HuzzActionButton(int x, int y, int width, int height, Text message, boolean accent, boolean lightGrayStyle, Consumer<HuzzActionButton> onPress) {
        super(x, y, width, height, message);
        this.accent = accent;
        this.lightGrayStyle = lightGrayStyle;
        this.onPress = onPress;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        int outlineColor = accent ? HuzzUi.uiAccentColor() : HuzzUi.withAlpha(HuzzUi.uiTextSecondary(), 120);
        int baseColor = accent ? HuzzUi.CHIP_ACCENT_FILL : HuzzUi.withAlpha(HuzzUi.PANEL_BACKGROUND_ALT, 210);

        if (lightGrayStyle) {
            outlineColor = 0xFFB7B7C0;
            baseColor = 0xFF8F8F99;
        }

        if (!active) {
            outlineColor = HuzzUi.withAlpha(HuzzUi.uiTextMuted(), 90);
            baseColor = HuzzUi.withAlpha(HuzzUi.PANEL_BACKGROUND, 150);
        } else if (isHovered()) {
            baseColor = lightGrayStyle ? 0xFFA5A5AE : (accent ? 0xFF443066 : 0xEE2A2A31);
        }

        HuzzUi.drawRoundedRect(context, getX(), getY(), getRight(), getBottom(), outlineColor, 3);
        HuzzUi.drawRoundedRect(context, getX() + 1, getY() + 1, getRight() - 1, getBottom() - 1, baseColor, 2);

        int textColor = active ? (accent ? HuzzUi.uiTextPrimary() : HuzzUi.uiTextSecondary()) : HuzzUi.uiTextMuted();
        if (lightGrayStyle) {
            textColor = 0xFF1E1E22;
        }
        int labelX = getX() + (width - MinecraftClient.getInstance().textRenderer.getWidth(getMessage())) / 2;
        context.drawText(
            MinecraftClient.getInstance().textRenderer,
            getMessage(),
            labelX,
            getY() + (height - 8) / 2,
            textColor,
            false
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
