package dev.david.huzzclient.feature;

import com.mojang.authlib.GameProfile;
import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.SkinTextures;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public final class SkinProtectController {
    private final HuzzConfigManager configManager;
    private final AtomicInteger requestVersion = new AtomicInteger();

    private volatile SkinTextures overrideSkin;
    private String requestedName = "";

    public SkinProtectController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        if (client.player == null) {
            clear();
            return;
        }

        HuzzConfig config = configManager.getConfig();
        if (!config.isSkinProtectEnabled()) {
            clear();
            return;
        }

        String targetName = config.getSkinProtectName();
        if (targetName.equalsIgnoreCase(requestedName)) {
            return;
        }

        requestedName = targetName;
        loadSkinAsync(client, targetName, requestVersion.incrementAndGet());
    }

    public SkinTextures applyOwnSkin(SkinTextures original) {
        HuzzConfig config = configManager.getConfig();
        if (!config.isSkinProtectEnabled() || overrideSkin == null) {
            return original;
        }
        return overrideSkin;
    }

    public void clear() {
        requestVersion.incrementAndGet();
        requestedName = "";
        overrideSkin = null;
    }

    private void loadSkinAsync(MinecraftClient client, String username, int version) {
        overrideSkin = null;
        CompletableFuture
            .supplyAsync(() -> client.getApiServices().profileResolver().getProfileByName(username))
            .thenCompose(profile -> fetchSkin(client, profile))
            .thenAccept(skin -> {
                if (version != requestVersion.get()) {
                    return;
                }
                overrideSkin = skin.orElse(null);
            });
    }

    private static CompletableFuture<Optional<SkinTextures>> fetchSkin(MinecraftClient client, Optional<GameProfile> profile) {
        if (profile.isEmpty()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return client.getSkinProvider().fetchSkinTextures(profile.get());
    }
}
