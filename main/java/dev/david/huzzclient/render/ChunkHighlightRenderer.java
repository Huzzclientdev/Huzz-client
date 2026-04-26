package dev.david.huzzclient.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.david.huzzclient.HuzzClient;
import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.feature.BlockEspTracker;
import dev.david.huzzclient.feature.BedrockHoleEspTracker;
import dev.david.huzzclient.feature.HoleEspTracker;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.command.RenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import java.util.List;

public final class ChunkHighlightRenderer {
    private static final double TRACER_FORWARD_OFFSET = 0.5D;
    private static final float PLAYER_ESP_RED = 0.44F;
    private static final float PLAYER_ESP_GREEN = 0.68F;
    private static final float PLAYER_ESP_BLUE = 1.0F;
    private static final float MOB_ESP_RED = 0.45F;
    private static final float MOB_ESP_GREEN = 1.0F;
    private static final float MOB_ESP_BLUE = 0.45F;
    private static final float HOLE_ESP_SINGLE_RED = 1.0F;
    private static final float HOLE_ESP_SINGLE_GREEN = 0.16F;
    private static final float HOLE_ESP_SINGLE_BLUE = 0.16F;
    private static final float HOLE_ESP_LINE_RED = 1.0F;
    private static final float HOLE_ESP_LINE_GREEN = 0.9F;
    private static final float HOLE_ESP_LINE_BLUE = 0.2F;
    private static final float LINE_WIDTH = 2.0F;
    private static final double BLOCK_BOX_INSET = 0.03D;

    private static final RenderPipeline LINE_PIPELINE = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.RENDERTYPE_LINES_SNIPPET)
            .withLocation(Identifier.of(HuzzClient.MOD_ID, "chunk_line_no_depth"))
            .withVertexFormat(VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH, VertexFormat.DrawMode.LINES)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withCull(false)
            .build()
    );

    private static final RenderLayer LINE_LAYER = RenderLayer.of(
        "huzzclient_lines",
        RenderSetup.builder(LINE_PIPELINE)
            .translucent()
            .expectedBufferSize(512)
            .build()
    );

    private static final RenderPipeline QUAD_PIPELINE = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
            .withLocation(Identifier.of(HuzzClient.MOD_ID, "chunk_quad_no_depth"))
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withCull(false)
            .build()
    );

    private static final RenderLayer QUAD_LAYER = RenderLayer.of(
        "huzzclient_quads",
        RenderSetup.builder(QUAD_PIPELINE)
            .translucent()
            .expectedBufferSize(512)
            .build()
    );

    private ChunkHighlightRenderer() {
    }

    public static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        HuzzConfig config = HuzzClient.getConfigManager().getConfig();
        List<BlockEspTracker.EspBlock> blockEspBlocks = HuzzClient.getBlockEspTracker().getHighlightedBlocks();
        List<BlockEspTracker.EspBlock> storageEspBlocks = HuzzClient.getStorageEspTracker().getHighlightedBlocks();
        List<HoleEspTracker.Hole> holeEspHoles = HuzzClient.getHoleEspTracker().getHighlightedHoles();
        List<BedrockHoleEspTracker.Hole> bedrockHoleEspHoles = HuzzClient.getBedrockHoleEspTracker().getHighlightedHoles();
        boolean selectedBlockEnabled = config.getBlockSelectionMode() != HuzzConfig.BlockSelectionMode.OFF;

        if (client.player == null || client.world == null
            || (blockEspBlocks.isEmpty() && storageEspBlocks.isEmpty() && holeEspHoles.isEmpty() && bedrockHoleEspHoles.isEmpty()
            && !config.isPlayerEspEnabled() && !config.isMobEspEnabled() && !selectedBlockEnabled)) {
            return;
        }

        MatrixStack matrices = context.matrices();
        if (matrices == null) {
            return;
        }

        float tickDelta = client.getRenderTickCounter().getTickProgress(false);
        var camera = client.gameRenderer.getCamera();
        double cameraX = camera.getCameraPos().x;
        double cameraY = camera.getCameraPos().y;
        double cameraZ = camera.getCameraPos().z;
        Vec3d playerOrigin = client.player.getLerpedPos(tickDelta)
            .add(0.0D, client.player.getStandingEyeHeight(), 0.0D)
            .add(client.player.getRotationVec(tickDelta).normalize().multiply(TRACER_FORWARD_OFFSET));
        double tracerStartX = playerOrigin.x;
        double tracerStartY = playerOrigin.y;
        double tracerStartZ = playerOrigin.z;
        RenderCommandQueue queue = context.commandQueue().getBatchingQueue(0);
        matrices.push();
        matrices.translate((float) -cameraX, (float) -cameraY, (float) -cameraZ);

        for (BlockEspTracker.EspBlock espBlock : blockEspBlocks) {
            renderBlock(queue, matrices, tracerStartX, tracerStartY, tracerStartZ, espBlock);
        }

        for (BlockEspTracker.EspBlock espBlock : storageEspBlocks) {
            renderBlock(queue, matrices, tracerStartX, tracerStartY, tracerStartZ, espBlock);
        }

        for (HoleEspTracker.Hole hole : holeEspHoles) {
            renderHole(queue, matrices, hole);
        }

        for (BedrockHoleEspTracker.Hole hole : bedrockHoleEspHoles) {
            renderBedrockHole(queue, matrices, hole);
        }

        if (selectedBlockEnabled) {
            renderSelectedBlock(queue, matrices, client, config);
        }

        if (config.isPlayerEspEnabled()) {
            for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
                if (player == client.player || player.isSpectator()) {
                    continue;
                }
                renderPlayer(queue, matrices, tracerStartX, tracerStartY, tracerStartZ, player, tickDelta);
            }
        }

        if (config.isMobEspEnabled()) {
            for (var entity : client.world.getEntities()) {
                if (!(entity instanceof MobEntity mob) || mob.isRemoved() || !mob.isAlive()) {
                    continue;
                }
                renderMob(queue, matrices, tracerStartX, tracerStartY, tracerStartZ, mob, tickDelta, config.isMobEspTracers());
            }
        }
        matrices.pop();
    }

    private static void renderBlock(
        RenderCommandQueue queue,
        MatrixStack matrices,
        double tracerStartX,
        double tracerStartY,
        double tracerStartZ,
        BlockEspTracker.EspBlock espBlock
    ) {
        float red = colorComponent(espBlock.colorRgb(), 16);
        float green = colorComponent(espBlock.colorRgb(), 8);
        float blue = colorComponent(espBlock.colorRgb(), 0);
        double x1 = espBlock.blockPos().getX() + BLOCK_BOX_INSET;
        double y1 = espBlock.blockPos().getY() + BLOCK_BOX_INSET;
        double z1 = espBlock.blockPos().getZ() + BLOCK_BOX_INSET;
        double x2 = espBlock.blockPos().getX() + 1.0D - BLOCK_BOX_INSET;
        double y2 = espBlock.blockPos().getY() + 1.0D - BLOCK_BOX_INSET;
        double z2 = espBlock.blockPos().getZ() + 1.0D - BLOCK_BOX_INSET;
        double centerX = espBlock.blockPos().getX() + 0.5D;
        double centerY = espBlock.blockPos().getY() + 0.5D;
        double centerZ = espBlock.blockPos().getZ() + 0.5D;

        queue.submitCustom(matrices, LINE_LAYER, (entry, consumer) -> {
            addBoxOutline(consumer, entry, x1, y1, z1, x2, y2, z2, red, green, blue);
            if (espBlock.tracers()) {
                addLine(consumer, entry, tracerStartX, tracerStartY, tracerStartZ, centerX, centerY, centerZ, red, green, blue);
            }
        });
    }

    private static void renderSelectedBlock(
        RenderCommandQueue queue,
        MatrixStack matrices,
        MinecraftClient client,
        HuzzConfig config
    ) {
        HitResult hitResult = client.crosshairTarget;
        if (!(hitResult instanceof BlockHitResult blockHit) || client.world == null) {
            return;
        }

        if (client.world.getBlockState(blockHit.getBlockPos()).isAir()) {
            return;
        }

        float red = colorComponent(config.getBlockSelectionColor().rgb(), 16);
        float green = colorComponent(config.getBlockSelectionColor().rgb(), 8);
        float blue = colorComponent(config.getBlockSelectionColor().rgb(), 0);
        double x1 = blockHit.getBlockPos().getX() + BLOCK_BOX_INSET;
        double y1 = blockHit.getBlockPos().getY() + BLOCK_BOX_INSET;
        double z1 = blockHit.getBlockPos().getZ() + BLOCK_BOX_INSET;
        double x2 = blockHit.getBlockPos().getX() + 1.0D - BLOCK_BOX_INSET;
        double y2 = blockHit.getBlockPos().getY() + 1.0D - BLOCK_BOX_INSET;
        double z2 = blockHit.getBlockPos().getZ() + 1.0D - BLOCK_BOX_INSET;

        if (config.getBlockSelectionMode() == HuzzConfig.BlockSelectionMode.FILLED) {
            queue.submitCustom(matrices, QUAD_LAYER, (entry, consumer) ->
                addBoxFill(consumer, entry, x1, y1, z1, x2, y2, z2, red, green, blue, 0.18F));
        }

        queue.submitCustom(matrices, LINE_LAYER, (entry, consumer) ->
            addBoxOutline(consumer, entry, x1, y1, z1, x2, y2, z2, red, green, blue));
    }

    private static void renderPlayer(
        RenderCommandQueue queue,
        MatrixStack matrices,
        double tracerStartX,
        double tracerStartY,
        double tracerStartZ,
        AbstractClientPlayerEntity player,
        float tickDelta
    ) {
        Vec3d lerpedPos = player.getLerpedPos(tickDelta);
        Box box = player.getBoundingBox().offset(lerpedPos.x - player.getX(), lerpedPos.y - player.getY(), lerpedPos.z - player.getZ());
        double centerX = (box.minX + box.maxX) * 0.5D;
        double centerY = box.minY + (box.maxY - box.minY) * 0.5D;
        double centerZ = (box.minZ + box.maxZ) * 0.5D;

        queue.submitCustom(matrices, LINE_LAYER, (entry, consumer) -> {
            addBoxOutline(consumer, entry, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, PLAYER_ESP_RED, PLAYER_ESP_GREEN, PLAYER_ESP_BLUE);
            addLine(consumer, entry, tracerStartX, tracerStartY, tracerStartZ, centerX, centerY, centerZ, PLAYER_ESP_RED, PLAYER_ESP_GREEN, PLAYER_ESP_BLUE);
        });
    }

    private static void renderMob(
        RenderCommandQueue queue,
        MatrixStack matrices,
        double tracerStartX,
        double tracerStartY,
        double tracerStartZ,
        MobEntity mob,
        float tickDelta,
        boolean tracers
    ) {
        Vec3d lerpedPos = mob.getLerpedPos(tickDelta);
        Box box = mob.getBoundingBox().offset(lerpedPos.x - mob.getX(), lerpedPos.y - mob.getY(), lerpedPos.z - mob.getZ());
        double centerX = (box.minX + box.maxX) * 0.5D;
        double centerY = box.minY + (box.maxY - box.minY) * 0.5D;
        double centerZ = (box.minZ + box.maxZ) * 0.5D;

        queue.submitCustom(matrices, LINE_LAYER, (entry, consumer) -> {
            addBoxOutline(consumer, entry, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, MOB_ESP_RED, MOB_ESP_GREEN, MOB_ESP_BLUE);
            if (tracers) {
                addLine(consumer, entry, tracerStartX, tracerStartY, tracerStartZ, centerX, centerY, centerZ, MOB_ESP_RED, MOB_ESP_GREEN, MOB_ESP_BLUE);
            }
        });
    }

    private static void renderHole(
        RenderCommandQueue queue,
        MatrixStack matrices,
        HoleEspTracker.Hole hole
    ) {
        Box box = hole.box().expand(-BLOCK_BOX_INSET);
        float red = hole.length() == 1 ? HOLE_ESP_SINGLE_RED : HOLE_ESP_LINE_RED;
        float green = hole.length() == 1 ? HOLE_ESP_SINGLE_GREEN : HOLE_ESP_LINE_GREEN;
        float blue = hole.length() == 1 ? HOLE_ESP_SINGLE_BLUE : HOLE_ESP_LINE_BLUE;

        queue.submitCustom(matrices, QUAD_LAYER, (entry, consumer) ->
            addBoxFill(consumer, entry, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, red, green, blue, 0.55F));
    }

    private static void renderBedrockHole(
        RenderCommandQueue queue,
        MatrixStack matrices,
        BedrockHoleEspTracker.Hole hole
    ) {
        Box box = hole.box().expand(-BLOCK_BOX_INSET);
        queue.submitCustom(matrices, QUAD_LAYER, (entry, consumer) ->
            addBoxFill(consumer, entry, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, 1.0F, 1.0F, 1.0F, 0.55F));
    }

    private static float[] heatmapColor(long averageResponseMs, int activityScore) {
        float responseHotness = 1.0F - Math.min(1.0F, Math.max(0.0F, (averageResponseMs - 150.0F) / 2350.0F));
        float scoreBoost = Math.min(1.0F, activityScore / 120.0F);
        float intensity = Math.max(0.15F, Math.min(1.0F, responseHotness * 0.75F + scoreBoost * 0.25F));

        if (intensity < 0.5F) {
            float local = intensity / 0.5F;
            return new float[]{
                0.15F + 0.85F * local,
                0.55F + 0.35F * local,
                1.0F - 0.75F * local
            };
        }

        float local = (intensity - 0.5F) / 0.5F;
        return new float[]{
            1.0F,
            0.9F - 0.7F * local,
            0.25F - 0.2F * local
        };
    }

    private static void addBoxOutline(
        VertexConsumer consumer,
        MatrixStack.Entry entry,
        double x1,
        double y1,
        double z1,
        double x2,
        double y2,
        double z2,
        float red,
        float green,
        float blue
    ) {
        addLine(consumer, entry, x1, y1, z1, x2, y1, z1, red, green, blue);
        addLine(consumer, entry, x2, y1, z1, x2, y1, z2, red, green, blue);
        addLine(consumer, entry, x2, y1, z2, x1, y1, z2, red, green, blue);
        addLine(consumer, entry, x1, y1, z2, x1, y1, z1, red, green, blue);

        addLine(consumer, entry, x1, y2, z1, x2, y2, z1, red, green, blue);
        addLine(consumer, entry, x2, y2, z1, x2, y2, z2, red, green, blue);
        addLine(consumer, entry, x2, y2, z2, x1, y2, z2, red, green, blue);
        addLine(consumer, entry, x1, y2, z2, x1, y2, z1, red, green, blue);

        addLine(consumer, entry, x1, y1, z1, x1, y2, z1, red, green, blue);
        addLine(consumer, entry, x2, y1, z1, x2, y2, z1, red, green, blue);
        addLine(consumer, entry, x2, y1, z2, x2, y2, z2, red, green, blue);
        addLine(consumer, entry, x1, y1, z2, x1, y2, z2, red, green, blue);
    }

    private static void addBoxFill(
        VertexConsumer consumer,
        MatrixStack.Entry entry,
        double x1,
        double y1,
        double z1,
        double x2,
        double y2,
        double z2,
        float red,
        float green,
        float blue,
        float alpha
    ) {
        addQuad(consumer, entry, x1, y1, z1, x2, y1, z1, x2, y2, z1, x1, y2, z1, red, green, blue, alpha);
        addQuad(consumer, entry, x1, y1, z2, x1, y2, z2, x2, y2, z2, x2, y1, z2, red, green, blue, alpha);
        addQuad(consumer, entry, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, red, green, blue, alpha);
        addQuad(consumer, entry, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, red, green, blue, alpha);
        addQuad(consumer, entry, x1, y2, z1, x1, y2, z2, x2, y2, z2, x2, y2, z1, red, green, blue, alpha);
        addQuad(consumer, entry, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, red, green, blue, alpha);
    }

    private static void addLine(
        VertexConsumer consumer,
        MatrixStack.Entry entry,
        double startX,
        double startY,
        double startZ,
        double endX,
        double endY,
        double endZ,
        float red,
        float green,
        float blue
    ) {
        float normalX = (float) (endX - startX);
        float normalY = (float) (endY - startY);
        float normalZ = (float) (endZ - startZ);
        float normalLengthSquared = normalX * normalX + normalY * normalY + normalZ * normalZ;

        if (normalLengthSquared > 0.0F) {
            float inverseLength = (float) (1.0D / Math.sqrt(normalLengthSquared));
            normalX *= inverseLength;
            normalY *= inverseLength;
            normalZ *= inverseLength;
        } else {
            normalX = 0.0F;
            normalY = 1.0F;
            normalZ = 0.0F;
        }

        consumer.vertex(entry.getPositionMatrix(), (float) startX, (float) startY, (float) startZ)
            .lineWidth(LINE_WIDTH)
            .color(red, green, blue, 1.0F)
            .normal(entry, normalX, normalY, normalZ);
        consumer.vertex(entry.getPositionMatrix(), (float) endX, (float) endY, (float) endZ)
            .lineWidth(LINE_WIDTH)
            .color(red, green, blue, 1.0F)
            .normal(entry, normalX, normalY, normalZ);
    }

    private static float colorComponent(int color, int shift) {
        return ((color >> shift) & 0xFF) / 255.0F;
    }

    private static void addQuad(
        VertexConsumer consumer,
        MatrixStack.Entry entry,
        double x1,
        double y1,
        double z1,
        double x2,
        double y2,
        double z2,
        double x3,
        double y3,
        double z3,
        double x4,
        double y4,
        double z4,
        float red,
        float green,
        float blue,
        float alpha
    ) {
        consumer.vertex(entry.getPositionMatrix(), (float) x1, (float) y1, (float) z1).color(red, green, blue, alpha);
        consumer.vertex(entry.getPositionMatrix(), (float) x2, (float) y2, (float) z2).color(red, green, blue, alpha);
        consumer.vertex(entry.getPositionMatrix(), (float) x3, (float) y3, (float) z3).color(red, green, blue, alpha);
        consumer.vertex(entry.getPositionMatrix(), (float) x4, (float) y4, (float) z4).color(red, green, blue, alpha);
    }
}
