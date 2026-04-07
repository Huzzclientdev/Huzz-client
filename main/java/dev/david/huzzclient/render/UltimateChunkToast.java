package dev.david.huzzclient.render;

import dev.david.huzzclient.detector.ChunkPacketTracker;
import dev.david.huzzclient.detector.ObservedActivityTimeTracker;
import dev.david.huzzclient.ui.HuzzUi;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class UltimateChunkToast implements Toast {
    private static final long DISPLAY_TIME_MS = 5000L;
    private static final int TOAST_WIDTH = 220;
    private static final int TOAST_HEIGHT = 40;

    private final Text title;
    private final Text details;
    private final Text score;
    private final int panelFillColor;
    private final int outlineColor;
    private final int detailColor;
    private final int chipFillColor;
    private Visibility visibility = Visibility.SHOW;

    private UltimateChunkToast(
        Text title,
        Text details,
        Text score,
        int panelFillColor,
        int outlineColor,
        int detailColor,
        int chipFillColor
    ) {
        this.title = title;
        this.details = details;
        this.score = score;
        this.panelFillColor = panelFillColor;
        this.outlineColor = outlineColor;
        this.detailColor = detailColor;
        this.chipFillColor = chipFillColor;
    }

    public static UltimateChunkToast chunkFinder(ChunkPacketTracker.MarkedChunk markedChunk) {
        return new UltimateChunkToast(
            Text.literal("Chunk finder hit"),
            Text.literal("[" + markedChunk.chunkX() + ", " + markedChunk.chunkZ() + "]  d" + markedChunk.deltaCount() + " l" + markedChunk.lightCount()),
            Text.literal(Integer.toString(markedChunk.suspicionScore())),
            HuzzUi.PANEL_BACKGROUND_ALT,
            HuzzUi.PANEL_OUTLINE,
            HuzzUi.TEXT_DANGER,
            HuzzUi.CHIP_ACCENT_FILL
        );
    }

    public static UltimateChunkToast timeDebug(ObservedActivityTimeTracker.ObservedChunk markedChunk) {
        long observedSeconds = markedChunk.observedActiveMs() / 1000L;
        long observedTenths = (markedChunk.observedActiveMs() % 1000L) / 100L;
        return new UltimateChunkToast(
            Text.literal("Time debug trace"),
            Text.literal("[" + markedChunk.chunkX() + ", " + markedChunk.chunkZ() + "]  " + observedSeconds + "." + observedTenths + "s observed"),
            Text.literal(Integer.toString(markedChunk.activityScore())),
            HuzzUi.TIME_DEBUG_FILL,
            HuzzUi.TIME_DEBUG_OUTLINE,
            HuzzUi.TEXT_INFO,
            0xFF183442
        );
    }

    public static UltimateChunkToast primeChunk(int chunkX, int chunkZ, int score) {
        return new UltimateChunkToast(
            Text.literal("Prime chunk hit"),
            Text.literal("[" + chunkX + ", " + chunkZ + "] dual-confirmed"),
            Text.literal(Integer.toString(score)),
            0xEE142016,
            0xFF5DDB7F,
            0xFFAAFFBC,
            0xFF163A1E
        );
    }

    public static UltimateChunkToast generic(Text title, Text details, int outlineColor, int detailColor) {
        return new UltimateChunkToast(
            title,
            details,
            Text.literal("!").formatted(Formatting.BOLD),
            HuzzUi.PANEL_BACKGROUND_ALT,
            outlineColor,
            detailColor,
            HuzzUi.CHIP_FILL
        );
    }

    @Override
    public Visibility getVisibility() {
        return visibility;
    }

    @Override
    public int getWidth() {
        return TOAST_WIDTH;
    }

    @Override
    public int getHeight() {
        return TOAST_HEIGHT;
    }

    @Override
    public int getRequiredSpaceCount() {
        return 1;
    }

    @Override
    public float getYPos(int topIndex) {
        return 6.0F + topIndex * 34.0F;
    }

    @Override
    public void update(ToastManager manager, long time) {
        if (time >= DISPLAY_TIME_MS * manager.getNotificationDisplayTimeMultiplier()) {
            visibility = Visibility.HIDE;
        }
    }

    @Override
    public void draw(DrawContext context, TextRenderer textRenderer, long startTime) {
        int width = getWidth();
        int height = getHeight();

        HuzzUi.drawPanel(context, 0, 0, width, height, 8, panelFillColor, outlineColor);
        HuzzUi.drawRoundedRect(context, width - 46, 8, width - 9, 22, outlineColor, 5);
        HuzzUi.drawRoundedRect(context, width - 45, 9, width - 10, 21, chipFillColor, 4);

        ItemStack iconStack = new ItemStack(Items.RECOVERY_COMPASS);
        context.drawItem(iconStack, 10, 10);

        context.drawText(textRenderer, title, 34, 8, HuzzUi.TEXT_PRIMARY, true);
        context.drawText(textRenderer, details, 34, 21, detailColor, false);
        context.drawCenteredTextWithShadow(textRenderer, score, width - 28, 11, HuzzUi.TEXT_PRIMARY);
    }
}
