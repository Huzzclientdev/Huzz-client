package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.ItemStack;

public final class AutoEatController {
    private static final int TRIGGER_HUNGER = 6;

    private final HuzzConfigManager configManager;

    private int originalSlot = -1;
    private boolean forcingUseKey;
    private boolean startedEating;

    public AutoEatController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.interactionManager == null) {
            clear(client);
            return;
        }

        HuzzConfig config = configManager.getConfig();
        if (!config.isAutoEatEnabled() || client.currentScreen != null) {
            clear(client);
            return;
        }

        if (forcingUseKey) {
            continueAutomation(client, player);
            return;
        }

        if (player.getHungerManager().getFoodLevel() > TRIGGER_HUNGER || player.isUsingItem()) {
            return;
        }

        int foodSlot = findBestFoodSlot(player);
        if (foodSlot < 0) {
            return;
        }

        originalSlot = player.getInventory().getSelectedSlot();
        player.getInventory().setSelectedSlot(foodSlot);
        client.options.useKey.setPressed(true);
        forcingUseKey = true;
        startedEating = false;
    }

    public void clear() {
        clear(MinecraftClient.getInstance());
    }

    public void clear(MinecraftClient client) {
        if (forcingUseKey && client != null) {
            client.options.useKey.setPressed(false);
            if (client.player != null && originalSlot >= 0) {
                client.player.getInventory().setSelectedSlot(originalSlot);
            }
        }

        originalSlot = -1;
        forcingUseKey = false;
        startedEating = false;
    }

    private void continueAutomation(MinecraftClient client, ClientPlayerEntity player) {
        if (player.getHungerManager().getFoodLevel() > TRIGGER_HUNGER) {
            clear(client);
            return;
        }

        ItemStack selectedStack = player.getMainHandStack();
        if (selectedStack.isEmpty() || selectedStack.get(DataComponentTypes.FOOD) == null) {
            clear(client);
            return;
        }

        client.options.useKey.setPressed(true);
        if (player.isUsingItem()) {
            startedEating = true;
            return;
        }

        if (startedEating) {
            clear(client);
        }
    }

    private static int findBestFoodSlot(ClientPlayerEntity player) {
        int bestSlot = -1;
        int bestNutrition = Integer.MIN_VALUE;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (stack.isEmpty()) {
                continue;
            }

            FoodComponent foodComponent = stack.get(DataComponentTypes.FOOD);
            if (foodComponent == null) {
                continue;
            }

            int nutrition = foodComponent.nutrition();
            if (nutrition > bestNutrition) {
                bestNutrition = nutrition;
                bestSlot = slot;
            }
        }
        return bestSlot;
    }
}
