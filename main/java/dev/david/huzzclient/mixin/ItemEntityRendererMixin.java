package dev.david.huzzclient.mixin;

import dev.david.huzzclient.HuzzClient;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin extends EntityRenderer<ItemEntity, ItemEntityRenderState> {
    private static final double NAMETAG_RANGE_SQUARED = 4096.0D;

    protected ItemEntityRendererMixin(EntityRendererFactory.Context context) {
        super(context);
    }

    @Inject(method = "updateRenderState", at = @At("TAIL"))
    private void huzzclient$updateNametag(ItemEntity itemEntity, ItemEntityRenderState itemEntityRenderState, float tickProgress, CallbackInfo ci) {
        if (!HuzzClient.getConfigManager().getConfig().isItemNametagsEnabled()) {
            return;
        }

        itemEntityRenderState.displayName = formatLabel(itemEntity.getStack());
        itemEntityRenderState.nameLabelPos = itemEntity.getAttachments().getPointNullable(
            EntityAttachmentType.NAME_TAG,
            0,
            itemEntity.getLerpedYaw(tickProgress)
        );
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void huzzclient$renderNametag(
        ItemEntityRenderState itemEntityRenderState,
        MatrixStack matrices,
        OrderedRenderCommandQueue queue,
        CameraRenderState cameraRenderState,
        CallbackInfo ci
    ) {
        if (!HuzzClient.getConfigManager().getConfig().isItemNametagsEnabled()
            || itemEntityRenderState.displayName == null
            || itemEntityRenderState.squaredDistanceToCamera > NAMETAG_RANGE_SQUARED) {
            return;
        }

        renderLabelIfPresent(itemEntityRenderState, matrices, queue, cameraRenderState);
    }

    private static Text formatLabel(ItemStack stack) {
        return Text.empty()
            .append(stack.getName().copy())
            .append(Text.literal(" x" + stack.getCount()));
    }
}
