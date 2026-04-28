package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Util;

public final class AutoReplenishController {
    private static final long ACTION_COOLDOWN_MS = 150L;

    private final HuzzConfigManager configManager;

    private Item trackedBlockItem;
    private int trackedHotbarSlot = -1;
    private long nextActionAt;
    private boolean automationOpenedInventory;

    public AutoReplenishController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.interactionManager == null) {
            clear();
            return;
        }

        HuzzConfig config = configManager.getConfig();
        if ((!config.isAutoReplenishEnabled() && !config.isAutoBridgeEnabled())
            || (client.currentScreen != null && !(client.currentScreen instanceof InventoryScreen))) {
            closeInventory(client);
            clearTracking();
            return;
        }

        int selectedSlot = player.getInventory().getSelectedSlot();
        ItemStack selectedStack = player.getInventory().getStack(selectedSlot);
        if (!selectedStack.isEmpty()) {
            if (selectedStack.getItem() instanceof BlockItem) {
                trackedBlockItem = selectedStack.getItem();
                trackedHotbarSlot = selectedSlot;
            } else if (trackedHotbarSlot != selectedSlot) {
                clearTracking();
            }
            return;
        }

        if (trackedBlockItem == null || trackedHotbarSlot != selectedSlot) {
            return;
        }

        long now = Util.getMeasuringTimeMs();
        if (now < nextActionAt) {
            return;
        }

        int sourceSlotId = findMatchingSlotId(player, trackedBlockItem, trackedHotbarSlot);
        if (sourceSlotId < 0) {
            return;
        }

        if (sourceSlotId < 36 && !(client.currentScreen instanceof InventoryScreen)) {
            client.setScreen(new InventoryScreen(player));
            automationOpenedInventory = true;
            nextActionAt = now + ACTION_COOLDOWN_MS;
            return;
        }

        client.interactionManager.clickSlot(
            player.playerScreenHandler.syncId,
            sourceSlotId,
            trackedHotbarSlot,
            SlotActionType.SWAP,
            player
        );
        nextActionAt = now + ACTION_COOLDOWN_MS;
        closeInventory(client);
    }

    public void clear() {
        clearTracking();
        nextActionAt = 0L;
        closeInventory(MinecraftClient.getInstance());
    }

    private void clearTracking() {
        trackedBlockItem = null;
        trackedHotbarSlot = -1;
    }

    private void closeInventory(MinecraftClient client) {
        if (automationOpenedInventory && client.currentScreen instanceof InventoryScreen) {
            client.setScreen(null);
        }
        automationOpenedInventory = false;
    }

    private static int findMatchingSlotId(ClientPlayerEntity player, Item item, int selectedHotbarSlot) {
        for (int inventorySlot = 0; inventorySlot < 36; inventorySlot++) {
            if (inventorySlot == selectedHotbarSlot) {
                continue;
            }

            ItemStack stack = player.getInventory().getStack(inventorySlot);
            if (stack.isEmpty() || stack.getItem() != item) {
                continue;
            }
            return toScreenHandlerSlotId(inventorySlot);
        }
        return -1;
    }

    private static int toScreenHandlerSlotId(int inventorySlot) {
        if (inventorySlot < 9) {
            return 36 + inventorySlot;
        }
        return inventorySlot;
    }
}
