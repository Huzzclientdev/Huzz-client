package dev.david.huzzclient.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

public final class HuzzUi {
    public static final int BACKDROP_COLOR = 0x32000000;
    public static final int BACKDROP_GLOW = 0x16000000;
    public static final int PANEL_OUTLINE = 0xFF7032B4;
    public static final int PANEL_BACKGROUND = 0xC9151519;
    public static final int PANEL_BACKGROUND_ALT = 0xD91D1D23;
    public static final int PANEL_SHADOW = 0x78000000;
    public static final int SURFACE_GLOW = 0x0F9D79D6;
    public static final int TEXT_PRIMARY = 0xFFF2F2F2;
    public static final int TEXT_SECONDARY = 0xFFD4CDE2;
    public static final int TEXT_MUTED = 0xFF9691A4;
    public static final int TEXT_DANGER = 0xFFFF7BB6;
    public static final int TEXT_INFO = 0xFFA58DFF;
    public static final int CHIP_FILL = 0xCC222228;
    public static final int CHIP_ACCENT_FILL = 0xE0322248;
    public static final int TIME_DEBUG_OUTLINE = 0xFF5D8CFF;
    public static final int TIME_DEBUG_FILL = 0xD9181C29;

    private HuzzUi() {
    }

    public static void drawBackdrop(DrawContext context, int width, int height) {
        context.fill(0, 0, width, height, BACKDROP_COLOR);
        context.fill(0, 0, width, height / 3, BACKDROP_GLOW);
    }

    public static void drawPanel(DrawContext context, int x1, int y1, int x2, int y2, int radius, int fillColor, int outlineColor) {
        drawRoundedRect(context, x1 + 2, y1 + 3, x2 + 2, y2 + 3, withAlpha(PANEL_SHADOW, 72), radius);
        drawRoundedRect(context, x1, y1, x2, y2, outlineColor, radius);
        drawRoundedRect(context, x1 + 1, y1 + 1, x2 - 1, y2 - 1, fillColor, Math.max(2, radius - 1));
    }

    public static void drawHeaderBar(DrawContext context, int x1, int y1, int x2, int y2, int fillColor) {
        context.fill(x1, y1, x2, y2, fillColor);
    }

    public static void drawChip(DrawContext context, int x1, int y1, int x2, int y2, boolean accent) {
        int outline = accent ? PANEL_OUTLINE : withAlpha(TEXT_SECONDARY, 180);
        int fill = accent ? CHIP_ACCENT_FILL : CHIP_FILL;
        drawRoundedRect(context, x1, y1, x2, y2, outline, 5);
        drawRoundedRect(context, x1 + 1, y1 + 1, x2 - 1, y2 - 1, fill, 4);
    }

    public static int withAlpha(int color, int alpha) {
        return (MathHelper.clamp(alpha, 0, 255) << 24) | (color & 0x00FFFFFF);
    }

    public static void drawRoundedRect(DrawContext context, int x1, int y1, int x2, int y2, int color, int radius) {
        int width = x2 - x1;
        int height = y2 - y1;
        if (width <= 0 || height <= 0) {
            return;
        }

        int actualRadius = Math.min(radius, Math.min(width / 2, height / 2));
        if (actualRadius <= 1) {
            context.fill(x1, y1, x2, y2, color);
            return;
        }

        for (int row = 0; row < actualRadius; row++) {
            int inset = roundedInset(actualRadius, row);
            context.fill(x1 + inset, y1 + row, x2 - inset, y1 + row + 1, color);
            context.fill(x1 + inset, y2 - row - 1, x2 - inset, y2 - row, color);
        }

        context.fill(x1, y1 + actualRadius, x2, y2 - actualRadius, color);
    }

    private static int roundedInset(int radius, int row) {
        double distance = radius - row - 0.5D;
        return Math.max(0, (int) Math.floor(radius - Math.sqrt(radius * radius - distance * distance)));
    }
}
