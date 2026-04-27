package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.Comparator;

public final class AutoCrystalController {
    private static final long PLACE_BREAK_WINDOW_MS = 500L;

    private final HuzzConfigManager configManager;

    private BlockPos pendingCrystalPos;
    private long pendingUntilMs;
    private long nextAttackAt;

    public AutoCrystalController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void recordUse(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) {
            return;
        }

        HuzzConfig config = configManager.getConfig();
        if (!config.isAutoCrystalEnabled() || client.currentScreen != null) {
            return;
        }

        boolean holdingCrystal = player.getMainHandStack().isOf(Items.END_CRYSTAL) || player.getOffHandStack().isOf(Items.END_CRYSTAL);
        if (!holdingCrystal || !(client.crosshairTarget instanceof BlockHitResult blockHitResult) || blockHitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        pendingCrystalPos = blockHitResult.getBlockPos().offset(blockHitResult.getSide());
        pendingUntilMs = Util.getMeasuringTimeMs() + PLACE_BREAK_WINDOW_MS;
    }

    public void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null || client.interactionManager == null) {
            clear();
            return;
        }

        HuzzConfig config = configManager.getConfig();
        long now = Util.getMeasuringTimeMs();
        if (!config.isAutoCrystalEnabled() || client.currentScreen != null) {
            clear();
            return;
        }

        refreshPendingTarget(client, player, now);
        if (pendingCrystalPos == null) {
            return;
        }

        if (now > pendingUntilMs) {
            pendingCrystalPos = null;
            pendingUntilMs = 0L;
            return;
        }

        if (now < nextAttackAt) {
            return;
        }

        Box searchBox = new Box(pendingCrystalPos).expand(1.5D, 2.0D, 1.5D);
        EndCrystalEntity crystal = client.world.getEntitiesByClass(EndCrystalEntity.class, searchBox, Entity::isAlive)
            .stream()
            .min(Comparator.comparingDouble(player::squaredDistanceTo))
            .orElse(null);
        if (crystal == null) {
            return;
        }

        client.interactionManager.attackEntity(player, crystal);
        player.swingHand(Hand.MAIN_HAND);
        nextAttackAt = now + config.getAutoCrystalDelayMs();
    }

    private void refreshPendingTarget(MinecraftClient client, ClientPlayerEntity player, long now) {
        if (!client.options.useKey.isPressed()) {
            return;
        }

        boolean holdingCrystal = player.getMainHandStack().isOf(Items.END_CRYSTAL) || player.getOffHandStack().isOf(Items.END_CRYSTAL);
        if (!holdingCrystal || !(client.crosshairTarget instanceof BlockHitResult blockHitResult) || blockHitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        pendingCrystalPos = blockHitResult.getBlockPos().offset(blockHitResult.getSide());
        pendingUntilMs = now + PLACE_BREAK_WINDOW_MS;
    }

    public void clear() {
        pendingCrystalPos = null;
        pendingUntilMs = 0L;
        nextAttackAt = 0L;
    }
}
