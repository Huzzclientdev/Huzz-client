package dev.david.huzzclient.render;

import dev.david.huzzclient.HuzzClient;
import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.feature.AimAssistController;
import dev.david.huzzclient.feature.MusicHudController;
import dev.david.huzzclient.ui.HuzzUi;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class FpsHudOverlay {
    private static final int BREAK_BAR_WIDTH = 58;
    private static final int BREAK_BAR_HEIGHT = 5;
    private static final float BREAK_BAR_SMOOTHING = 0.22F;
    private static final int MINIMAP_SIZE = 64;
    private static final int MINIMAP_MARGIN = 6;
    private static final int MINIMAP_RADIUS_BLOCKS = 48;
    private static final int MINIMAP_COORD_GAP = 4;
    private static final int ARMOR_ICON_SIZE = 16;
    private static final int ARMOR_ROW_GAP = 18;
    private static final int MUSIC_HUD_MARGIN = 8;
    private static final int MUSIC_HUD_HEIGHT = 54;
    private static final int MUSIC_HUD_WIDTH = 196;
    private static final int MUSIC_COVER_SIZE = 42;
    private static final int MUSIC_LARGE_HUD_WIDTH = 142;
    private static final int MUSIC_LARGE_LYRICS_HUD_WIDTH = 190;
    private static final int MUSIC_LARGE_HUD_HEIGHT = 184;
    private static final int MUSIC_LARGE_LYRICS_HUD_HEIGHT = 148;
    private static final int MUSIC_LARGE_COVER_SIZE = 118;
    private static final int MUSIC_LARGE_LYRICS_SIZE = 82;
    private static final int MUSIC_PANEL_RADIUS = 6;
    private static final int MUSIC_COVER_RADIUS = 5;
    private static final long MUSIC_LYRIC_TRANSITION_MS = 420L;
    private static final double MUSIC_LYRIC_SCROLL_STIFFNESS = 72.0D;
    private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static float displayedBreakProgress;
    private static String lyricAnimationKey = "";
    private static double displayedLyricScroll;
    private static long displayedLyricScrollAtMs;
    private static long displayedLyricPositionMs;

    private FpsHudOverlay() {
    }

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        HuzzConfig config = HuzzClient.getConfigManager().getConfig();
        if (client.options.hudHidden) {
            return;
        }

        renderBreakProgress(context, client, config);
        renderAimFov(context, client, config);
        renderHud(context, client, config, tickCounter);
        if (config.isHudMusicEnabled()) {
            renderMusicHud(context, client);
        }
    }

    private static void renderAimFov(DrawContext context, MinecraftClient client, HuzzConfig config) {
        if (!config.isAimFovEnabled() || client.player == null || client.world == null) {
            return;
        }

        int centerX = context.getScaledWindowWidth() / 2;
        int centerY = context.getScaledWindowHeight() / 2;
        int radius = config.getAimFovRadius();
        drawCircleOutline(context, centerX, centerY, radius, 0xCCFF74C8);

        AimAssistController.ScreenOffset offset = HuzzClient.getAimAssistController().currentScreenOffset(client);
        if (offset != null) {
            int dotX = centerX + (int) Math.round(offset.x());
            int dotY = centerY + (int) Math.round(offset.y());
            context.fill(dotX - 2, dotY - 2, dotX + 2, dotY + 2, 0xFFFFA6E1);
        }
    }

    private static void renderHud(DrawContext context, MinecraftClient client, HuzzConfig config, RenderTickCounter tickCounter) {
        if (!config.isHudEnabled() || client.player == null || client.world == null) {
            return;
        }

        if (config.isHudMinimapEnabled()) {
            renderMinimap(context, client, tickCounter);
        }
        if (config.isHudClockEnabled()) {
            renderClock(context, client);
        }
        if (config.isHudFpsEnabled()) {
            renderFps(context, client);
        }
        if (config.isHudArmorEnabled()) {
            renderArmorHud(context, client);
        }
        if (config.isHudPotionEffectsEnabled()) {
            renderPotionEffects(context, client, config);
        }
    }

    private static void renderFps(DrawContext context, MinecraftClient client) {
        String label = "FPS: " + client.getCurrentFps();
        int x = context.getScaledWindowWidth() - client.textRenderer.getWidth(label) - 6;
        context.drawTextWithShadow(client.textRenderer, Text.literal(label), x, 6, 0xFFFFFFFF);
    }

    private static void renderClock(DrawContext context, MinecraftClient client) {
        String label = LocalTime.now().format(CLOCK_FORMAT);
        int x = (context.getScaledWindowWidth() - client.textRenderer.getWidth(label)) / 2;
        context.drawTextWithShadow(client.textRenderer, Text.literal(label), x, 6, 0xFFFFFFFF);
    }

    private static void renderMinimap(DrawContext context, MinecraftClient client, RenderTickCounter tickCounter) {
        int left = MINIMAP_MARGIN;
        int top = MINIMAP_MARGIN;
        int right = left + MINIMAP_SIZE;
        int bottom = top + MINIMAP_SIZE;
        int centerX = left + MINIMAP_SIZE / 2;
        int centerY = top + MINIMAP_SIZE / 2;
        int radius = MINIMAP_SIZE / 2 - 4;
        float tickDelta = tickCounter.getTickProgress(false);
        float yaw = client.player.getYaw(tickDelta);
        double yawRadians = Math.toRadians(yaw);
        double sin = Math.sin(yawRadians);
        double cos = Math.cos(yawRadians);
        Vec3d playerPos = client.player.getLerpedPos(tickDelta);

        context.fill(left, top, right, bottom, 0x88101519);
        context.fill(left, top, right, top + 1, 0xFF93D7FF);
        context.fill(left, bottom - 1, right, bottom, 0xFF93D7FF);
        context.fill(left, top, left + 1, bottom, 0xFF93D7FF);
        context.fill(right - 1, top, right, bottom, 0xFF93D7FF);
        context.fill(centerX, top + 3, centerX + 1, bottom - 3, 0x334D6575);
        context.fill(left + 3, centerY, right - 3, centerY + 1, 0x334D6575);

        for (AbstractClientPlayerEntity other : client.world.getPlayers()) {
            if (other == client.player || other.isSpectator()) {
                continue;
            }

            Vec3d otherPos = other.getLerpedPos(tickDelta);
            double dx = otherPos.x - playerPos.x;
            double dz = otherPos.z - playerPos.z;
            double localX = dx * cos + dz * sin;
            double localY = dx * sin - dz * cos;
            double scale = radius / (double) MINIMAP_RADIUS_BLOCKS;
            int dotX = centerX + MathHelper.clamp((int) Math.round(localX * scale), -radius, radius);
            int dotY = centerY + MathHelper.clamp((int) Math.round(localY * scale), -radius, radius);
            context.fill(dotX - 1, dotY - 1, dotX + 2, dotY + 2, 0xFFFF7070);
        }

        context.fill(centerX - 1, centerY - 1, centerX + 2, centerY + 2, 0xFF8BFFAE);
        context.drawText(context.getMatrices() == null ? client.textRenderer : client.textRenderer, Text.literal("Map"), left + 4, top + 4, 0xFFFFFFFF, false);
        String coords = String.format("%.0f %.0f %.0f", playerPos.x, playerPos.y, playerPos.z);
        int coordsX = left + (MINIMAP_SIZE - client.textRenderer.getWidth(coords)) / 2;
        context.drawTextWithShadow(client.textRenderer, Text.literal(coords), coordsX, bottom + MINIMAP_COORD_GAP, 0xFFFFFFFF);
    }

    private static void renderArmorHud(DrawContext context, MinecraftClient client) {
        int centerX = context.getScaledWindowWidth() / 2;
        int baseX = centerX + 100;
        int baseY = context.getScaledWindowHeight() - 20 - ARMOR_ICON_SIZE * 4 + 1;
        EquipmentSlot[] armorSlots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

        for (int row = 0; row < armorSlots.length; row++) {
            ItemStack stack = client.player.getEquippedStack(armorSlots[row]);
            if (stack.isEmpty()) {
                continue;
            }

            int y = baseY + row * ARMOR_ROW_GAP;
            context.drawItem(stack, baseX, y);
            context.drawStackOverlay(client.textRenderer, stack, baseX, y);
            String durability = Integer.toString(stack.getMaxDamage() - stack.getDamage());
            context.drawTextWithShadow(client.textRenderer, Text.literal(durability), baseX + ARMOR_ICON_SIZE + 4, y + 4, durabilityColor(stack));
        }
    }

    private static int durabilityColor(ItemStack stack) {
        if (!stack.isDamageable()) {
            return 0xFFFFFFFF;
        }

        float remaining = (stack.getMaxDamage() - stack.getDamage()) / (float) stack.getMaxDamage();
        if (remaining <= 0.2F) {
            return 0xFFFF6B6B;
        }
        if (remaining <= 0.5F) {
            return 0xFFFFD166;
        }
        return 0xFF8BFFAE;
    }

    private static void renderPotionEffects(DrawContext context, MinecraftClient client, HuzzConfig config) {
        if (client.player == null) {
            return;
        }

        List<StatusEffectInstance> effects = client.player.getStatusEffects()
            .stream()
            .sorted((first, second) -> Integer.compare(second.getDuration(), first.getDuration()))
            .toList();
        if (effects.isEmpty()) {
            return;
        }

        int right = context.getScaledWindowWidth() - 6;
        int top = 18;
        int row = 0;
        for (StatusEffectInstance effect : effects) {
            String name = effect.getEffectType().value().getName().getString();
            int amplifier = effect.getAmplifier();
            String label = amplifier > 0 ? name + " " + (amplifier + 1) : name;
            String duration = formatEffectDuration(effect);
            String line = label + " " + duration;
            int x = right - client.textRenderer.getWidth(line);
            context.drawTextWithShadow(client.textRenderer, Text.literal(line), x, top + row * 10, 0xFFE6F4FF);
            row++;
            if (row >= 6) {
                break;
            }
        }
    }

    private static void renderMusicHud(DrawContext context, MinecraftClient client) {
        MusicHudController.MusicSnapshot music = HuzzClient.getMusicHudController().getSnapshot();
        if (!music.active()) {
            return;
        }
        HuzzConfig config = HuzzClient.getConfigManager().getConfig();
        int accent = config.isHudMusicThemeEnabled() ? withAlpha(music.coverThemeColor(), 255) : 0xFF7ED8FF;
        int accentSoft = withAlpha(accent, 196);

        if (config.isHudMusicLargeUi()) {
            renderLargeMusicHud(context, client, music, accent, accentSoft);
            return;
        }

        int left = MUSIC_HUD_MARGIN;
        int bottom = context.getScaledWindowHeight() - MUSIC_HUD_MARGIN;
        int top = bottom - MUSIC_HUD_HEIGHT;
        int right = left + MUSIC_HUD_WIDTH;
        int coverLeft = left + 6;
        int coverTop = top + 6;
        int contentLeft = coverLeft + MUSIC_COVER_SIZE + 6;

        HuzzUi.drawRoundedRect(context, left, top, right, bottom, accent, MUSIC_PANEL_RADIUS);
        HuzzUi.drawRoundedRect(context, left + 1, top + 1, right - 1, bottom - 1, 0xAA11161D, MUSIC_PANEL_RADIUS - 1);

        int coverRight = coverLeft + MUSIC_COVER_SIZE;
        int coverBottom = coverTop + MUSIC_COVER_SIZE;
        HuzzUi.drawRoundedRect(context, coverLeft, coverTop, coverRight, coverBottom, 0xFF5A6E84, MUSIC_COVER_RADIUS);
        HuzzUi.drawRoundedRect(context, coverLeft + 1, coverTop + 1, coverRight - 1, coverBottom - 1, 0xFF2A3441, MUSIC_COVER_RADIUS - 1);

        drawMusicCover(context, music, coverLeft, coverTop, coverRight, coverBottom, 0xFF2A3441, MUSIC_COVER_RADIUS);

        int textMaxWidth = right - 7 - contentLeft;
        String title = clipLabelToWidth(client, music.title(), textMaxWidth);
        String subtitle = clipLabelToWidth(client, (music.artist() + " - " + music.album()).trim(), textMaxWidth);
        context.drawTextWithShadow(client.textRenderer, Text.literal(title), contentLeft, top + 8, 0xFFFFFFFF);
        context.drawTextWithShadow(client.textRenderer, Text.literal(subtitle), contentLeft, top + 20, 0xFFAFC8DB);

        String timing = clipLabelToWidth(client, formatDuration(music.positionMs()) + " / " + formatDuration(music.durationMs()), textMaxWidth);
        int timingY = top + 34;
        context.drawTextWithShadow(client.textRenderer, Text.literal(timing), contentLeft, timingY, 0xFFE7F0F8);

        int barLeft = contentLeft;
        int barRight = right - 7;
        int barTop = bottom - 8;
        context.fill(barLeft, barTop, barRight, barTop + 2, 0x66344857);
        if (music.durationMs() > 0L) {
            double progress = MathHelper.clamp(music.positionMs() / (double) music.durationMs(), 0.0D, 1.0D);
            int fill = barLeft + (int) Math.round((barRight - barLeft) * progress);
            context.fill(barLeft, barTop, fill, barTop + 2, accentSoft);
        }
    }

    private static void renderLargeMusicHud(DrawContext context, MinecraftClient client, MusicHudController.MusicSnapshot music, int accent, int accentSoft) {
        int left = MUSIC_HUD_MARGIN;
        int bottom = context.getScaledWindowHeight() - MUSIC_HUD_MARGIN;
        HuzzConfig config = HuzzClient.getConfigManager().getConfig();
        boolean lyricsEnabled = config.isHudMusicLyricsEnabled();
        int panelHeight = lyricsEnabled ? MUSIC_LARGE_LYRICS_HUD_HEIGHT : MUSIC_LARGE_HUD_HEIGHT;
        int mediaSize = lyricsEnabled ? MUSIC_LARGE_LYRICS_SIZE : MUSIC_LARGE_COVER_SIZE;
        int top = bottom - panelHeight;
        int right = left + (lyricsEnabled ? MUSIC_LARGE_LYRICS_HUD_WIDTH : MUSIC_LARGE_HUD_WIDTH);
        int coverLeft = lyricsEnabled ? left + 8 : left + 12;
        int coverTop = top + 10;
        int coverRight = lyricsEnabled ? right - 8 : coverLeft + mediaSize;
        int coverBottom = coverTop + mediaSize;

        HuzzUi.drawRoundedRect(context, left, top, right, bottom, accent, MUSIC_PANEL_RADIUS);
        HuzzUi.drawRoundedRect(context, left + 1, top + 1, right - 1, bottom - 1, 0xAA11161D, MUSIC_PANEL_RADIUS - 1);
        HuzzUi.drawRoundedRect(context, coverLeft, coverTop, coverRight, coverBottom, 0xFF5A6E84, MUSIC_COVER_RADIUS + 1);
        HuzzUi.drawRoundedRect(context, coverLeft + 1, coverTop + 1, coverRight - 1, coverBottom - 1, 0xFF2A3441, MUSIC_COVER_RADIUS);
        if (lyricsEnabled) {
            renderLargeLyrics(context, client, music, coverLeft, coverTop, coverRight, coverBottom, accent);
        } else {
            drawMusicCover(context, music, coverLeft, coverTop, coverRight, coverBottom, 0xFF2A3441, MUSIC_COVER_RADIUS + 1);
        }

        String title = clipLabel(music.title(), 21);
        String subtitle = clipLabel((music.artist() + " - " + music.album()).trim(), 24);
        int textLeft = left + 9;
        int textTop = coverBottom + 10;
        context.drawTextWithShadow(client.textRenderer, Text.literal(title), textLeft, textTop, 0xFFFFFFFF);
        context.drawTextWithShadow(client.textRenderer, Text.literal(subtitle), textLeft, textTop + 12, 0xFFAFC8DB);

        String timing = formatDuration(music.positionMs()) + " / " + formatDuration(music.durationMs());
        int timingY = bottom - 22;
        context.drawTextWithShadow(client.textRenderer, Text.literal(timing), textLeft, timingY, 0xFFE7F0F8);

        int barLeft = left + 9;
        int barRight = right - 9;
        int barTop = bottom - 9;
        context.fill(barLeft, barTop, barRight, barTop + 2, 0x66344857);
        if (music.durationMs() > 0L) {
            double progress = MathHelper.clamp(music.positionMs() / (double) music.durationMs(), 0.0D, 1.0D);
            int fill = barLeft + (int) Math.round((barRight - barLeft) * progress);
            context.fill(barLeft, barTop, fill, barTop + 2, accentSoft);
        }
    }

    private static void renderLargeLyrics(
        DrawContext context,
        MinecraftClient client,
        MusicHudController.MusicSnapshot music,
        int left,
        int top,
        int right,
        int bottom,
        int accent
    ) {
        List<MusicHudController.LyricLine> lyrics = music.lyrics();
        HuzzUi.drawRoundedRect(context, left + 1, top + 1, right - 1, bottom - 1, 0xDD11161D, MUSIC_COVER_RADIUS);
        context.fill(left + 5, top + 5, right - 5, top + 6, withAlpha(accent, 90));

        if (lyrics.isEmpty()) {
            String message = "Lyrics unavailable";
            int x = left + ((right - left - client.textRenderer.getWidth(message)) / 2);
            int y = top + ((bottom - top) / 2) - 5;
            context.drawTextWithShadow(client.textRenderer, Text.literal(message), x, y, 0xFFE7F0F8);
            maskRoundedCorners(context, left + 1, top + 1, right - 1, bottom - 1, 0xFF2A3441, MUSIC_COVER_RADIUS);
            return;
        }

        int activeIndex = currentLyricIndex(music, lyrics);
        double targetScroll = lyricTargetScroll(music, lyrics, activeIndex);
        double scroll = smoothLyricScroll(music, lyrics, targetScroll);
        int lineGap = 22;
        int centerY = top + ((bottom - top) / 2) - 5;
        int maxTextWidth = right - left - 12;
        int scrollIndex = (int) Math.floor(scroll);
        int firstIndex = Math.max(0, scrollIndex - 3);
        int lastIndex = Math.min(lyrics.size() - 1, scrollIndex + 5);

        context.enableScissor(left + 2, top + 2, right - 2, bottom - 2);
        try {
            for (int pass = 0; pass < 2; pass++) {
                for (int lineIndex = firstIndex; lineIndex <= lastIndex; lineIndex++) {
                    double distance = Math.abs(lineIndex - scroll);
                    if (distance > 3.25D) {
                        continue;
                    }
                    boolean focused = distance < 0.55D;
                    if ((pass == 0 && focused) || (pass == 1 && !focused)) {
                        continue;
                    }
                    int y = centerY + (int) Math.round((lineIndex - scroll) * lineGap);
                    int color = lyricLineColor(distance);
                    drawLyricLine(context, client, lyrics, lineIndex, left, right, y, maxTextWidth, color);
                }
            }
        } finally {
            context.disableScissor();
        }
        maskRoundedCorners(context, left + 1, top + 1, right - 1, bottom - 1, 0xFF2A3441, MUSIC_COVER_RADIUS);
    }

    private static void drawLyricLine(
        DrawContext context,
        MinecraftClient client,
        List<MusicHudController.LyricLine> lyrics,
        int index,
        int left,
        int right,
        int y,
        int maxTextWidth,
        int color
    ) {
        if (index < 0 || index >= lyrics.size()) {
            return;
        }
        List<String> lines = wrapLabelToWidth(client, lyrics.get(index).text(), maxTextWidth, 2);
        if (lines.isEmpty()) {
            return;
        }
        int firstY = y - ((lines.size() - 1) * 5);
        for (int row = 0; row < lines.size(); row++) {
            String line = lines.get(row);
            int x = left + ((right - left - client.textRenderer.getWidth(line)) / 2);
            context.drawTextWithShadow(client.textRenderer, Text.literal(line), x, firstY + row * 10, color);
        }
    }

    private static float smootherStep(float progress) {
        float clamped = MathHelper.clamp(progress, 0.0F, 1.0F);
        return clamped * clamped * clamped * (clamped * (clamped * 6.0F - 15.0F) + 10.0F);
    }

    private static double smoothLyricScroll(MusicHudController.MusicSnapshot music, List<MusicHudController.LyricLine> lyrics, double targetScroll) {
        String key = music.title() + "\n" + music.artist() + "\n" + music.album() + "\n" + lyrics.size() + "\n" + music.syncedLyrics();
        long now = System.currentTimeMillis();
        if (!key.equals(lyricAnimationKey) || displayedLyricScrollAtMs == 0L) {
            lyricAnimationKey = key;
            displayedLyricScroll = targetScroll;
            displayedLyricScrollAtMs = now;
            displayedLyricPositionMs = music.positionMs();
            return displayedLyricScroll;
        }

        boolean seekedBackward = music.positionMs() + 750L < displayedLyricPositionMs;
        displayedLyricPositionMs = music.positionMs();
        if (seekedBackward) {
            displayedLyricScroll = targetScroll;
            displayedLyricScrollAtMs = now;
            return displayedLyricScroll;
        }

        targetScroll = Math.max(targetScroll, displayedLyricScroll);
        double deltaSeconds = Math.max(0.0D, Math.min(0.08D, (now - displayedLyricScrollAtMs) / 1000.0D));
        displayedLyricScrollAtMs = now;
        double blend = 1.0D - Math.exp(-MUSIC_LYRIC_SCROLL_STIFFNESS * deltaSeconds);
        double delta = MathHelper.clamp(targetScroll - displayedLyricScroll, -1.35D, 1.35D);
        displayedLyricScroll += delta * blend;
        return displayedLyricScroll;
    }

    private static double lyricTargetScroll(MusicHudController.MusicSnapshot music, List<MusicHudController.LyricLine> lyrics, int activeIndex) {
        if (lyrics.isEmpty()) {
            return 0.0D;
        }
        return MathHelper.clamp(activeIndex + smootherStep(lyricTransitionProgress(music, lyrics, activeIndex)), 0.0D, Math.max(0.0D, lyrics.size() - 1.0D));
    }

    private static int lyricLineColor(double distance) {
        double focus = MathHelper.clamp(1.0D - distance / 1.35D, 0.0D, 1.0D);
        int alpha = (int) Math.round(76.0D + focus * 179.0D);
        int base = 0xFFB8C9D8;
        int red = (int) Math.round(((base >>> 16) & 0xFF) + (255 - ((base >>> 16) & 0xFF)) * focus);
        int green = (int) Math.round(((base >>> 8) & 0xFF) + (255 - ((base >>> 8) & 0xFF)) * focus);
        int blue = (int) Math.round((base & 0xFF) + (255 - (base & 0xFF)) * focus);
        return (MathHelper.clamp(alpha, 0, 255) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);
    }

    private static int currentLyricIndex(MusicHudController.MusicSnapshot music, List<MusicHudController.LyricLine> lyrics) {
        if (lyrics.isEmpty()) {
            return 0;
        }
        if (!music.syncedLyrics()) {
            if (music.durationMs() <= 0L) {
                return 0;
            }
            double progress = MathHelper.clamp(music.positionMs() / (double) music.durationMs(), 0.0D, 0.999D);
            return MathHelper.clamp((int) Math.floor(progress * lyrics.size()), 0, lyrics.size() - 1);
        }

        int current = 0;
        long positionMs = music.positionMs();
        for (int index = 0; index < lyrics.size(); index++) {
            if (lyrics.get(index).timeMs() > positionMs) {
                break;
            }
            current = index;
        }
        return current;
    }

    private static float lyricTransitionProgress(MusicHudController.MusicSnapshot music, List<MusicHudController.LyricLine> lyrics, int index) {
        if (lyrics.isEmpty() || index >= lyrics.size() - 1) {
            return 0.0F;
        }

        long nextStartMs;
        if (music.syncedLyrics()) {
            nextStartMs = lyrics.get(index + 1).timeMs();
        } else if (music.durationMs() > 0L) {
            nextStartMs = Math.round(((index + 1) / (double) lyrics.size()) * music.durationMs());
        } else {
            return 0.0F;
        }

        long transitionStart = nextStartMs - MUSIC_LYRIC_TRANSITION_MS;
        long positionMs = music.positionMs();
        if (positionMs <= transitionStart) {
            return 0.0F;
        }
        return MathHelper.clamp((positionMs - transitionStart) / (float) MUSIC_LYRIC_TRANSITION_MS, 0.0F, 1.0F);
    }

    private static void drawMusicCover(
        DrawContext context,
        MusicHudController.MusicSnapshot music,
        int coverLeft,
        int coverTop,
        int coverRight,
        int coverBottom,
        int innerColor,
        int radius
    ) {
        if (music.coverTextureId() != null) {
            int drawSize = Math.max(1, coverRight - coverLeft - 2);
            context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                music.coverTextureId(),
                coverLeft + 1,
                coverTop + 1,
                0,
                0,
                drawSize,
                drawSize,
                drawSize,
                drawSize
            );
            maskRoundedCorners(context, coverLeft + 1, coverTop + 1, coverRight - 1, coverBottom - 1, innerColor, Math.max(1, radius - 1));
        } else {
            HuzzUi.drawRoundedRect(context, coverLeft + 1, coverTop + 1, coverRight - 1, coverBottom - 1, innerColor, Math.max(1, radius - 1));
        }
    }

    private static String formatDuration(long totalMillis) {
        long totalSeconds = Math.max(0L, totalMillis / 1000L);
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        return String.format("%d:%02d", minutes, seconds);
    }

    private static String clipLabel(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength - 3) + "...";
    }

    private static String clipLabelToWidth(MinecraftClient client, String value, int maxWidth) {
        if (value == null || value.isBlank() || maxWidth <= 0) {
            return "";
        }
        String trimmed = value.trim();
        if (client.textRenderer.getWidth(trimmed) <= maxWidth) {
            return trimmed;
        }
        String ellipsis = "...";
        int ellipsisWidth = client.textRenderer.getWidth(ellipsis);
        if (ellipsisWidth >= maxWidth) {
            return "";
        }
        int end = trimmed.length();
        while (end > 0) {
            String candidate = trimmed.substring(0, end).trim() + ellipsis;
            if (client.textRenderer.getWidth(candidate) <= maxWidth) {
                return candidate;
            }
            end--;
        }
        return "";
    }

    private static List<String> wrapLabelToWidth(MinecraftClient client, String value, int maxWidth, int maxLines) {
        if (value == null || value.isBlank() || maxWidth <= 0 || maxLines <= 0) {
            return List.of();
        }

        String[] words = value.trim().split("\\s+");
        List<String> lines = new java.util.ArrayList<>(Math.min(maxLines, 2));
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (client.textRenderer.getWidth(candidate) <= maxWidth) {
                current.setLength(0);
                current.append(candidate);
                continue;
            }

            if (!current.isEmpty()) {
                lines.add(current.toString());
                current.setLength(0);
            }
            if (lines.size() >= maxLines) {
                break;
            }

            if (client.textRenderer.getWidth(word) <= maxWidth) {
                current.append(word);
            } else {
                current.append(clipLabelToWidth(client, word, maxWidth));
            }
        }

        if (!current.isEmpty() && lines.size() < maxLines) {
            lines.add(current.toString());
        }
        return lines;
    }

    private static void maskRoundedCorners(DrawContext context, int x1, int y1, int x2, int y2, int color, int radius) {
        int width = x2 - x1;
        int height = y2 - y1;
        if (width <= 0 || height <= 0) {
            return;
        }

        int actualRadius = Math.min(radius, Math.min(width / 2, height / 2));
        if (actualRadius <= 1) {
            return;
        }

        for (int row = 0; row < actualRadius; row++) {
            int inset = roundedInset(actualRadius, row);
            if (inset <= 0) {
                continue;
            }
            context.fill(x1, y1 + row, x1 + inset, y1 + row + 1, color);
            context.fill(x2 - inset, y1 + row, x2, y1 + row + 1, color);
            context.fill(x1, y2 - row - 1, x1 + inset, y2 - row, color);
            context.fill(x2 - inset, y2 - row - 1, x2, y2 - row, color);
        }
    }

    private static int roundedInset(int radius, int row) {
        double distance = radius - row - 0.5D;
        return Math.max(0, (int) Math.floor(radius - Math.sqrt(radius * radius - distance * distance)));
    }

    private static void drawCircleOutline(DrawContext context, int centerX, int centerY, int radius, int color) {
        int x = radius;
        int y = 0;
        int decision = 1 - radius;
        while (x >= y) {
            plotCirclePoints(context, centerX, centerY, x, y, color);
            y++;
            if (decision < 0) {
                decision += 2 * y + 1;
            } else {
                x--;
                decision += 2 * (y - x) + 1;
            }
        }
    }

    private static void plotCirclePoints(DrawContext context, int centerX, int centerY, int x, int y, int color) {
        drawCirclePoint(context, centerX + x, centerY + y, color);
        drawCirclePoint(context, centerX + y, centerY + x, color);
        drawCirclePoint(context, centerX - y, centerY + x, color);
        drawCirclePoint(context, centerX - x, centerY + y, color);
        drawCirclePoint(context, centerX - x, centerY - y, color);
        drawCirclePoint(context, centerX - y, centerY - x, color);
        drawCirclePoint(context, centerX + y, centerY - x, color);
        drawCirclePoint(context, centerX + x, centerY - y, color);
    }

    private static void drawCirclePoint(DrawContext context, int x, int y, int color) {
        context.fill(x, y, x + 1, y + 1, color);
    }

    private static int withAlpha(int color, int alpha) {
        return (MathHelper.clamp(alpha, 0, 255) << 24) | (color & 0x00FFFFFF);
    }

    private static String formatEffectDuration(StatusEffectInstance effect) {
        if (effect.isInfinite()) {
            return "inf";
        }
        int totalSeconds = Math.max(0, effect.getDuration() / 20);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
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
