package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class AnchorMacroController {
    private static final long ACTION_COOLDOWN_MS = 55L;
    private static final long SELF_POP_PAUSE_MS = 900L;

    private final HuzzConfigManager configManager;

    private long nextActionAt;
    private long pausedUntilMs;
    private Step step = Step.IDLE;
    private BlockHitResult placeHit;
    private BlockPos anchorPos;

    public AnchorMacroController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null || client.interactionManager == null) {
            clear();
            return;
        }

        HuzzConfig config = configManager.getConfig();
        long now = Util.getMeasuringTimeMs();
        if (!config.isAnchorMacroEnabled() || client.currentScreen != null || now < pausedUntilMs) {
            if (!config.isAnchorMacroEnabled()) {
                resetSequence();
            }
            return;
        }

        if (now < nextActionAt) {
            return;
        }

        if (step == Step.IDLE) {
            if (!client.options.useKey.isPressed() || !(client.crosshairTarget instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK) {
                return;
            }
            placeHit = blockHit;
            anchorPos = blockHit.getBlockPos().offset(blockHit.getSide());
            step = Step.SWITCH_ANCHOR;
        }

        switch (step) {
            case SWITCH_ANCHOR -> {
                if (!selectHotbarItem(player, Items.RESPAWN_ANCHOR)) {
                    resetSequence();
                    return;
                }
                step = Step.PLACE_ANCHOR;
                nextActionAt = now + ACTION_COOLDOWN_MS;
            }
            case PLACE_ANCHOR -> {
                if (placeHit == null) {
                    resetSequence();
                    return;
                }
                client.interactionManager.interactBlock(player, Hand.MAIN_HAND, placeHit);
                player.swingHand(Hand.MAIN_HAND);
                step = Step.SWITCH_GLOWSTONE;
                nextActionAt = now + ACTION_COOLDOWN_MS;
            }
            case SWITCH_GLOWSTONE -> {
                if (!selectHotbarItem(player, Items.GLOWSTONE)) {
                    resetSequence();
                    return;
                }
                step = Step.CHARGE_ANCHOR;
                nextActionAt = now + ACTION_COOLDOWN_MS;
            }
            case CHARGE_ANCHOR -> {
                if (anchorPos == null) {
                    resetSequence();
                    return;
                }
                client.interactionManager.interactBlock(player, Hand.MAIN_HAND, anchorHit(anchorPos));
                player.swingHand(Hand.MAIN_HAND);
                step = Step.SWITCH_DETONATOR;
                nextActionAt = now + ACTION_COOLDOWN_MS;
            }
            case SWITCH_DETONATOR -> {
                int detonatorSlot = findNonGlowstoneSlot(player);
                if (detonatorSlot < 0) {
                    resetSequence();
                    return;
                }
                player.getInventory().setSelectedSlot(detonatorSlot);
                step = Step.DETONATE_ANCHOR;
                nextActionAt = now + ACTION_COOLDOWN_MS;
            }
            case DETONATE_ANCHOR -> {
                if (anchorPos == null) {
                    resetSequence();
                    return;
                }
                client.interactionManager.interactBlock(player, Hand.MAIN_HAND, anchorHit(anchorPos));
                player.swingHand(Hand.MAIN_HAND);
                resetSequence();
                nextActionAt = now + ACTION_COOLDOWN_MS;
            }
            case IDLE -> {
            }
        }
    }

    public void recordTotemPop(MinecraftClient client, Entity entity) {
        if (client.player != null && entity == client.player) {
            pausedUntilMs = Util.getMeasuringTimeMs() + SELF_POP_PAUSE_MS;
            resetSequence();
        }
    }

    public void clear() {
        nextActionAt = 0L;
        pausedUntilMs = 0L;
        resetSequence();
    }

    private void resetSequence() {
        step = Step.IDLE;
        placeHit = null;
        anchorPos = null;
    }

    private static boolean selectHotbarItem(ClientPlayerEntity player, Item item) {
        int selectedSlot = player.getInventory().getSelectedSlot();
        ItemStack selectedStack = player.getInventory().getStack(selectedSlot);
        if (selectedStack.isOf(item)) {
            return true;
        }

        int slot = findHotbarSlot(player, item);
        if (slot < 0) {
            return false;
        }
        player.getInventory().setSelectedSlot(slot);
        return true;
    }

    private static int findNonGlowstoneSlot(ClientPlayerEntity player) {
        int selectedSlot = player.getInventory().getSelectedSlot();
        if (!player.getInventory().getStack(selectedSlot).isOf(Items.GLOWSTONE)) {
            return selectedSlot;
        }

        int anchorSlot = findHotbarSlot(player, Items.RESPAWN_ANCHOR);
        if (anchorSlot >= 0) {
            return anchorSlot;
        }

        for (int slot = 0; slot < 9; slot++) {
            if (!player.getInventory().getStack(slot).isOf(Items.GLOWSTONE)) {
                return slot;
            }
        }
        return -1;
    }

    private static BlockHitResult anchorHit(BlockPos pos) {
        return new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
    }

    private static int findHotbarSlot(ClientPlayerEntity player, Item item) {
        for (int slot = 0; slot < 9; slot++) {
            if (player.getInventory().getStack(slot).isOf(item)) {
                return slot;
            }
        }
        return -1;
    }

    private enum Step {
        IDLE,
        SWITCH_ANCHOR,
        PLACE_ANCHOR,
        SWITCH_GLOWSTONE,
        CHARGE_ANCHOR,
        SWITCH_DETONATOR,
        DETONATE_ANCHOR
    }
}
