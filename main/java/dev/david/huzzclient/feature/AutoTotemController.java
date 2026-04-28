package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Util;

public final class AutoTotemController {
    private static final int OFFHAND_SLOT_ID = 45;

    private final HuzzConfigManager configManager;

    private Step step = Step.IDLE;
    private long nextActionAt;
    private int sourceSlotId = -1;
    private boolean automationOpenedInventory;

    public AutoTotemController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.interactionManager == null) {
            clear(client);
            return;
        }

        HuzzConfig config = configManager.getConfig();
        if (!config.isAutoTotemEnabled()) {
            clear(client);
            return;
        }

        if (hasOffhandTotem(player)) {
            clear(client);
            return;
        }

        if (client.currentScreen != null && !(client.currentScreen instanceof InventoryScreen)) {
            return;
        }

        long now = Util.getMeasuringTimeMs();
        long actionDelayMs = config.getAutoTotemDelayMs();
        if (step == Step.IDLE) {
            sourceSlotId = findTotemSlot(player.currentScreenHandler);
            if (sourceSlotId < 0) {
                return;
            }

            if (!(client.currentScreen instanceof InventoryScreen)) {
                client.setScreen(new InventoryScreen(player));
                automationOpenedInventory = true;
            }

            step = Step.PICKUP_SOURCE;
            nextActionAt = now + actionDelayMs;
            return;
        }

        if (now < nextActionAt) {
            return;
        }

        ScreenHandler handler = player.currentScreenHandler;
        switch (step) {
            case PICKUP_SOURCE -> {
                client.interactionManager.clickSlot(handler.syncId, sourceSlotId, 0, SlotActionType.PICKUP, player);
                step = Step.PICKUP_OFFHAND;
                nextActionAt = now + actionDelayMs;
            }
            case PICKUP_OFFHAND -> {
                client.interactionManager.clickSlot(handler.syncId, OFFHAND_SLOT_ID, 0, SlotActionType.PICKUP, player);
                step = Step.RETURN_SOURCE;
                nextActionAt = now + actionDelayMs;
            }
            case RETURN_SOURCE -> {
                if (!handler.getCursorStack().isEmpty()) {
                    client.interactionManager.clickSlot(handler.syncId, sourceSlotId, 0, SlotActionType.PICKUP, player);
                    nextActionAt = now + actionDelayMs;
                }
                step = Step.CLOSE;
            }
            case CLOSE -> clear(client);
            case IDLE -> {
            }
        }
    }

    public void clear(MinecraftClient client) {
        if (automationOpenedInventory && client.currentScreen instanceof InventoryScreen) {
            client.setScreen(null);
        }

        step = Step.IDLE;
        nextActionAt = 0L;
        sourceSlotId = -1;
        automationOpenedInventory = false;
    }

    public void recordTotemPop(MinecraftClient client, Entity entity) {
        ClientPlayerEntity player = client.player;
        if (player == null || entity != player) {
            return;
        }

        int switchSlot = configManager.getConfig().getAutoTotemSwitchSlot();
        if (switchSlot > 0) {
            player.getInventory().setSelectedSlot(switchSlot - 1);
        }
    }

    private static boolean hasOffhandTotem(ClientPlayerEntity player) {
        return player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);
    }

    private static int findTotemSlot(ScreenHandler handler) {
        for (int slotId = 9; slotId < handler.slots.size(); slotId++) {
            if (slotId == OFFHAND_SLOT_ID) {
                continue;
            }
            if (handler.getSlot(slotId).getStack().isOf(Items.TOTEM_OF_UNDYING)) {
                return slotId;
            }
        }
        return -1;
    }

    private enum Step {
        IDLE,
        PICKUP_SOURCE,
        PICKUP_OFFHAND,
        RETURN_SOURCE,
        CLOSE
    }
}
