package dev.david.huzzclient.render;

import dev.david.huzzclient.HuzzClient;
import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.feature.AimAssistController;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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
    private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

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
        renderAimFov(context, client, config);
        renderHud(context, client, config, tickCounter);
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
