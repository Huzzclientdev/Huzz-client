package dev.david.huzzclient.ui;

import dev.david.huzzclient.HuzzClient;
import dev.david.huzzclient.config.HuzzConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

public final class HuzzUi {
    public static final int BACKDROP_COLOR = 0x12000000;
    public static final int BACKDROP_GLOW = 0x0A000000;
    public static final int PANEL_OUTLINE = 0xFF7032B4;
    public static final int PANEL_BACKGROUND = 0xB8141418;
    public static final int PANEL_BACKGROUND_ALT = 0xCC1B1B22;
    public static final int PANEL_SHADOW = 0x42000000;
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
        int accent = themedAccent();
        context.fill(0, 0, width, height, BACKDROP_COLOR);
        context.fill(0, 0, width, height / 4, blend(withAlpha(accent, 26), BACKDROP_GLOW, 0.35F));
    }

    public static void drawPanel(DrawContext context, int x1, int y1, int x2, int y2, int radius, int fillColor, int outlineColor) {
        int accent = themedAccent();
        drawRoundedRect(context, x1 + 1, y1 + 2, x2 + 1, y2 + 2, withAlpha(PANEL_SHADOW, 72), radius);
        drawRoundedRect(context, x1, y1, x2, y2, blend(outlineColor, accent, 0.58F), radius);
        drawRoundedRect(context, x1 + 1, y1 + 1, x2 - 1, y2 - 1, blend(fillColor, withAlpha(accent, 255), 0.16F), Math.max(2, radius - 1));
    }

    public static void drawHeaderBar(DrawContext context, int x1, int y1, int x2, int y2, int fillColor) {
        context.fill(x1, y1, x2, y2, blend(fillColor, withAlpha(themedAccent(), 255), 0.42F));
    }

    public static void drawChip(DrawContext context, int x1, int y1, int x2, int y2, boolean accent) {
        int theme = themedAccent();
        int outline = accent ? blend(PANEL_OUTLINE, theme, 0.70F) : withAlpha(uiTextSecondary(), 180);
        int fill = accent ? blend(CHIP_ACCENT_FILL, withAlpha(theme, 255), 0.58F) : blend(CHIP_FILL, withAlpha(theme, 255), 0.18F);
        drawRoundedRect(context, x1, y1, x2, y2, outline, 5);
        drawRoundedRect(context, x1 + 1, y1 + 1, x2 - 1, y2 - 1, fill, 4);
    }

    public static int uiAccentColor() {
        return themedAccent();
    }

    public static int uiTextPrimary() {
        return isThemeLight() ? 0xFF000000 : TEXT_PRIMARY;
    }

    public static int uiTextSecondary() {
        return isThemeLight() ? 0xFF000000 : TEXT_SECONDARY;
    }

    public static int uiTextMuted() {
        return isThemeLight() ? 0xFF000000 : TEXT_MUTED;
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

    private static int themedAccent() {
        try {
            HuzzConfig config = HuzzClient.getConfigManager().getConfig();
            if (config == null || !config.isHudMusicThemeEnabled()) {
                return PANEL_OUTLINE;
            }
            return HuzzClient.getMusicHudController().getSnapshot().coverThemeColor();
        } catch (Exception ignored) {
            return PANEL_OUTLINE;
        }
    }

    private static boolean isThemeLight() {
        int accent = themedAccent();
        int red = (accent >>> 16) & 0xFF;
        int green = (accent >>> 8) & 0xFF;
        int blue = accent & 0xFF;
        double luminance = (0.2126 * red + 0.7152 * green + 0.0722 * blue) / 255.0;
        return luminance >= 0.68;
    }

    private static int blend(int baseColor, int mixColor, float mixRatio) {
        float ratio = MathHelper.clamp(mixRatio, 0.0F, 1.0F);
        float inv = 1.0F - ratio;
        int a = (int) ((((baseColor >>> 24) & 0xFF) * inv) + (((mixColor >>> 24) & 0xFF) * ratio));
        int r = (int) ((((baseColor >>> 16) & 0xFF) * inv) + (((mixColor >>> 16) & 0xFF) * ratio));
        int g = (int) ((((baseColor >>> 8) & 0xFF) * inv) + (((mixColor >>> 8) & 0xFF) * ratio));
        int b = (int) (((baseColor & 0xFF) * inv) + ((mixColor & 0xFF) * ratio));
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }
}
