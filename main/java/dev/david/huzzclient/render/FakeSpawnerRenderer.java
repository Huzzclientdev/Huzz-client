package dev.david.huzzclient.render;

import dev.david.huzzclient.HuzzClient;
import dev.david.huzzclient.config.HuzzConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.entity.MobSpawnerBlockEntityRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class FakeSpawnerRenderer {
    private static final int RENDER_SCAN_RADIUS_XZ = 8;
    private static final int RENDER_SCAN_RADIUS_Y = 4;
    private static final int MAX_RENDERED_FAKE_SPAWNERS = 6;
    private static final float BASE_DISPLAY_SCALE = 0.53125F;
    private static final double SPINNER_SPEED = 1000.0D / 220.0D;

    private static SkeletonEntity cachedSkeleton;
    private static World cachedWorld;

    private FakeSpawnerRenderer() {
    }

    public static void render(
        World world,
        EntityRenderManager entityRenderManager,
        WorldRenderState renderState,
        MatrixStack matrices,
        OrderedRenderCommandQueue queue,
        float tickProgress
    ) {
        if (world == null || entityRenderManager == null || renderState == null || matrices == null || queue == null) {
            return;
        }

        HuzzConfig config = HuzzClient.getConfigManager().getConfig();
        if (!config.isFakeSpawnerEnabled()) {
            return;
        }

        Block targetBlock = resolveBlock(config.getFakeSpawnerBlockId());
        if (targetBlock == null) {
            return;
        }

        Entity displayEntity = getDisplayEntity(world);
        if (displayEntity == null) {
            return;
        }

        Vec3d cameraPos = renderState.cameraRenderState.pos;
        if (cameraPos == null) {
            return;
        }

        BlockPos center = BlockPos.ofFloored(cameraPos);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int rendered = 0;

        for (int y = center.getY() - RENDER_SCAN_RADIUS_Y; y <= center.getY() + RENDER_SCAN_RADIUS_Y && rendered < MAX_RENDERED_FAKE_SPAWNERS; y++) {
            for (int z = center.getZ() - RENDER_SCAN_RADIUS_XZ; z <= center.getZ() + RENDER_SCAN_RADIUS_XZ && rendered < MAX_RENDERED_FAKE_SPAWNERS; z++) {
                for (int x = center.getX() - RENDER_SCAN_RADIUS_XZ; x <= center.getX() + RENDER_SCAN_RADIUS_XZ && rendered < MAX_RENDERED_FAKE_SPAWNERS; x++) {
                    mutable.set(x, y, z);
                    BlockState state = world.getBlockState(mutable);
                    if (!state.isOf(targetBlock)) {
                        continue;
                    }

                    renderSpawnerMob(world, mutable, displayEntity, entityRenderManager, renderState.cameraRenderState, matrices, queue, tickProgress);
                    rendered++;
                }
            }
        }
    }

    private static void renderSpawnerMob(
        World world,
        BlockPos pos,
        Entity displayEntity,
        EntityRenderManager entityRenderManager,
        CameraRenderState cameraRenderState,
        MatrixStack matrices,
        OrderedRenderCommandQueue queue,
        float tickProgress
    ) {
        displayEntity.refreshPositionAndAngles(pos.toCenterPos().x, pos.getY(), pos.toCenterPos().z, 0.0F, 0.0F);

        EntityRenderState displayEntityState = entityRenderManager.getAndUpdateRenderState(displayEntity, tickProgress);
        float displayRotation = (float) ((((double) world.getTime() + tickProgress) * SPINNER_SPEED) % 360.0D) * 10.0F;
        float displayScale = BASE_DISPLAY_SCALE;
        float maxSize = Math.max(displayEntity.getWidth(), displayEntity.getHeight());
        if (maxSize > 1.0F) {
            displayScale /= maxSize;
        }

        matrices.push();
        matrices.translate(pos.getX(), pos.getY(), pos.getZ());
        MobSpawnerBlockEntityRenderer.renderDisplayEntity(
            matrices,
            queue,
            displayEntityState,
            entityRenderManager,
            displayRotation,
            displayScale,
            cameraRenderState
        );
        matrices.pop();
    }

    private static Entity getDisplayEntity(World world) {
        if (cachedSkeleton == null || cachedWorld != world) {
            cachedSkeleton = new SkeletonEntity(EntityType.SKELETON, world);
            cachedWorld = world;
        }
        return cachedSkeleton;
    }

    private static Block resolveBlock(String rawId) {
        Identifier id = parseId(rawId);
        return id == null ? null : Registries.BLOCK.getOptionalValue(id).orElse(null);
    }

    private static Identifier parseId(String rawId) {
        if (rawId == null || rawId.isBlank()) {
            return null;
        }

        return rawId.contains(":")
            ? Identifier.tryParse(rawId)
            : Identifier.tryParse("minecraft", rawId);
    }
}
