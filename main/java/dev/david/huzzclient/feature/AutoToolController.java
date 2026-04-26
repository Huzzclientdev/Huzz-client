package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public final class AutoToolController {
    private final HuzzConfigManager configManager;

    public AutoToolController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null || client.interactionManager == null) {
            return;
        }

        HuzzConfig config = configManager.getConfig();
        if (!config.isAutoToolEnabled() || client.currentScreen != null || !client.interactionManager.isBreakingBlock()) {
            return;
        }

        if (!(client.crosshairTarget instanceof BlockHitResult blockHitResult) || blockHitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockState blockState = client.world.getBlockState(blockHitResult.getBlockPos());
        int bestSlot = bestHotbarSlot(player, blockState);
        if (bestSlot >= 0 && bestSlot != player.getInventory().getSelectedSlot()) {
            player.getInventory().setSelectedSlot(bestSlot);
        }
    }

    private static int bestHotbarSlot(ClientPlayerEntity player, BlockState blockState) {
        int bestSlot = -1;
        float bestScore = 1.0F;

        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (stack.isEmpty()) {
                continue;
            }

            float score = stack.getMiningSpeedMultiplier(blockState);
            if (stack.isSuitableFor(blockState)) {
                score += 1000.0F;
            }

            if (score > bestScore) {
                bestScore = score;
                bestSlot = slot;
            }
        }

        return bestSlot;
    }
}
