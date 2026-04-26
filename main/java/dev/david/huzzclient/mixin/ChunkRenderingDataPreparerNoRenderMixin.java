package dev.david.huzzclient.mixin;

import dev.david.huzzclient.HuzzClient;
import net.minecraft.client.render.ChunkRenderingDataPreparer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChunkRenderingDataPreparer.class)
public abstract class ChunkRenderingDataPreparerNoRenderMixin {
    @ModifyVariable(
        method = "updateSectionOcclusionGraph(ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;Ljava/util/List;Lit/unimi/dsi/fastutil/longs/LongOpenHashSet;)V",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private boolean huzzclient$disableChunkCulling(boolean cullChunks) {
        var config = HuzzClient.getConfigManager().getConfig();
        if (config.isNoRenderEnabled() && config.isNoRenderChunkCulling()) {
            return false;
        }
        return cullChunks;
    }
}
