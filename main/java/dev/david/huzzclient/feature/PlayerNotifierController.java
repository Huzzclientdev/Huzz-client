package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import dev.david.huzzclient.render.UltimateChunkToast;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class PlayerNotifierController {
    private final HuzzConfigManager configManager;
    private final Set<UUID> visiblePlayers = new HashSet<>();

    public PlayerNotifierController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            clear();
            return;
        }

        HuzzConfig config = configManager.getConfig();
        if (!config.isPlayerNotifierEnabled()) {
            clear();
            return;
        }

        Set<UUID> currentPlayers = new HashSet<>();
        for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
            if (player == client.player || player.isSpectator()) {
                continue;
            }

            UUID uuid = player.getUuid();
            currentPlayers.add(uuid);
            if (visiblePlayers.add(uuid)) {
                client.getToastManager().add(UltimateChunkToast.generic(
                    Text.literal("Player detected"),
                    Text.literal(player.getGameProfile().name()),
                    0xFF71A8FF,
                    0xFFC3DEFF
                ));
            }
        }

        visiblePlayers.retainAll(currentPlayers);
    }

    public void clear() {
        visiblePlayers.clear();
    }
}
