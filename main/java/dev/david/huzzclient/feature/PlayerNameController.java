package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public final class PlayerNameController {
    private final HuzzConfigManager configManager;

    public PlayerNameController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public Text decorateRenderName(PlayerLikeEntity player, Text original) {
        HuzzConfig config = configManager.getConfig();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || player != client.player) {
            return original;
        }

        return decorateOwnName(config, original.copy());
    }

    public Text decoratePlayerListName(PlayerListEntry entry, Text original) {
        HuzzConfig config = configManager.getConfig();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || entry == null || !entry.getProfile().id().equals(client.player.getUuid())) {
            return original;
        }

        return decorateOwnName(config, original.copy());
    }

    public boolean shouldShowOwnNameTag(PlayerLikeEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || player != client.player) {
            return false;
        }

        HuzzConfig config = configManager.getConfig();
        return config.isFakeMediaEnabled() || config.isNameProtectEnabled();
    }

    private static Text decorateOwnName(HuzzConfig config, MutableText original) {
        MutableText text = config.isNameProtectEnabled()
            ? Text.literal(config.getNameProtectName())
            : original;

        if (config.isFakeMediaEnabled()) {
            return withFakeMediaPrefix(text);
        }
        return text;
    }

    private static Text withFakeMediaPrefix(Text name) {
        return Text.empty()
            .append(Text.literal("+ ").styled(style -> style.withColor(0x3F86D9)))
            .append(name);
    }
}
