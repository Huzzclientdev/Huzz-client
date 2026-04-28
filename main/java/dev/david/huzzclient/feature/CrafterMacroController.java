package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import dev.david.huzzclient.mixin.CrafterScreenAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CrafterScreen;
import net.minecraft.screen.CrafterScreenHandler;

public final class CrafterMacroController {
    private final HuzzConfigManager configManager;

    private int lastSyncId = -1;
    private int lastAppliedMask = -1;

    public CrafterMacroController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        if (!(client.currentScreen instanceof CrafterScreen crafterScreen)) {
            clear();
            return;
        }

        HuzzConfig config = configManager.getConfig();
        if (!config.isCrafterMacroEnabled()) {
            clear();
            return;
        }

        CrafterScreenHandler handler = (CrafterScreenHandler) crafterScreen.getScreenHandler();
        int desiredMask = config.getCrafterMacroMask();
        if (handler.syncId == lastSyncId && desiredMask == lastAppliedMask) {
            return;
        }

        CrafterScreenAccessor accessor = (CrafterScreenAccessor) crafterScreen;
        for (int slot = 0; slot < 9; slot++) {
            boolean shouldBeEnabled = config.isCrafterSlotSelected(slot);
            boolean isDisabled = handler.isSlotDisabled(slot);
            if (isDisabled == !shouldBeEnabled) {
                continue;
            }

            accessor.huzzclient$setSlotEnabled(slot, shouldBeEnabled);
        }

        lastSyncId = handler.syncId;
        lastAppliedMask = desiredMask;
    }

    public void clear() {
        lastSyncId = -1;
        lastAppliedMask = -1;
    }
}
