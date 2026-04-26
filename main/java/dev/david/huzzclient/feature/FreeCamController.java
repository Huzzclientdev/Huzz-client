package dev.david.huzzclient.feature;

import dev.david.huzzclient.HuzzClient;
import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public final class FreeCamController {
    private static final float CAMERA_DISTANCE = 4.0F;
    private static final float LOOK_DELTA_MULTIPLIER = 0.15F;

    private final HuzzConfigManager configManager;

    private Vec3d previousCameraPos;
    private Vec3d cameraPos;
    private float cameraYaw;
    private float cameraPitch;
    private Boolean previousChunkCullingEnabled;
    private Perspective previousPerspective;
    private boolean unavailable;
    private boolean active;

    public FreeCamController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public boolean isActive() {
        return active && cameraPos != null;
    }

    public boolean isAvailable() {
        return !unavailable;
    }

    public Vec3d getCameraPos(float tickDelta) {
        if (cameraPos == null) {
            return Vec3d.ZERO;
        }

        if (previousCameraPos == null) {
            return cameraPos;
        }

        double clampedDelta = Math.max(0.0D, Math.min(1.0D, tickDelta));
        return previousCameraPos.lerp(cameraPos, clampedDelta);
    }

    public float getCameraYaw() {
        return cameraYaw;
    }

    public float getCameraPitch() {
        return cameraPitch;
    }

    public float getCameraDistance() {
        return CAMERA_DISTANCE;
    }

    public boolean shouldBlockVanillaInput() {
        return isActive();
    }

    public HitResult getCrosshairTarget(ClientPlayerEntity player, float tickDelta) {
        double blockInteractionRange = player.getBlockInteractionRange();
        double entityInteractionRange = player.getEntityInteractionRange();
        double maxInteractionRange = Math.max(blockInteractionRange, entityInteractionRange);
        Vec3d cameraPosition = getCameraPos(tickDelta);
        Vec3d rotation = Vec3d.fromPolar(cameraPitch, cameraYaw);
        Vec3d maxTargetPosition = cameraPosition.add(rotation.multiply(maxInteractionRange));
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) {
            return BlockHitResult.createMissed(cameraPosition, Direction.getFacing(rotation.x, rotation.y, rotation.z), BlockPos.ofFloored(maxTargetPosition));
        }

        HitResult blockHit = world.raycast(new RaycastContext(
            cameraPosition,
            maxTargetPosition,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            player
        ));

        double hitDistanceSquared = maxInteractionRange * maxInteractionRange;
        double actualInteractionRange = maxInteractionRange;
        if (blockHit.getType() != HitResult.Type.MISS) {
            hitDistanceSquared = cameraPosition.squaredDistanceTo(blockHit.getPos());
            actualInteractionRange = Math.sqrt(hitDistanceSquared);
        }

        Vec3d playerCameraPosition = player.getCameraPosVec(tickDelta);
        Box interactionBox = player.getBoundingBox()
            .offset(cameraPosition.subtract(playerCameraPosition))
            .stretch(rotation.multiply(actualInteractionRange))
            .expand(1.0D, 1.0D, 1.0D);
        EntityHitResult entityHit = ProjectileUtil.raycast(
            player,
            cameraPosition,
            cameraPosition.add(rotation.multiply(actualInteractionRange)),
            interactionBox,
            EntityPredicates.CAN_HIT,
            hitDistanceSquared
        );
        if (entityHit != null && cameraPosition.squaredDistanceTo(entityHit.getPos()) < hitDistanceSquared) {
            return clampCrosshairTargetRange(entityHit, cameraPosition, entityInteractionRange);
        }

        return clampCrosshairTargetRange(blockHit, cameraPosition, blockInteractionRange);
    }

    public void tick(MinecraftClient client) {
        if (unavailable) {
            if (configManager.getConfig().isFreeCamEnabled()) {
                configManager.getConfig().setFreeCamEnabled(false);
            }
            clear();
            return;
        }

        try {
            tickInternal(client);
        } catch (Throwable throwable) {
            unavailable = true;
            configManager.getConfig().setFreeCamEnabled(false);
            configManager.save();
            HuzzClient.LOGGER.warn("Disabling Free Cam due to compatibility error", throwable);
            clear();
        }
    }

    private void tickInternal(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            clear();
            return;
        }

        HuzzConfig config = configManager.getConfig();
        if (!config.isFreeCamEnabled()) {
            clear();
            return;
        }

        if (!active || previousCameraPos == null || cameraPos == null) {
            cameraPos = client.player.getCameraPosVec(1.0F);
            previousCameraPos = cameraPos;
            cameraYaw = client.player.getYaw();
            cameraPitch = client.player.getPitch();
            if (previousChunkCullingEnabled == null) {
                previousChunkCullingEnabled = client.chunkCullingEnabled;
                client.chunkCullingEnabled = false;
            }
            if (previousPerspective == null) {
                previousPerspective = client.options.getPerspective();
                client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
            }
            active = true;
        }

        if (client.currentScreen != null) {
            previousCameraPos = cameraPos;
            return;
        }

        previousCameraPos = cameraPos;
        cameraPos = cameraPos.add(computeMotion(client, config));
    }

    public void clear() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (previousChunkCullingEnabled != null) {
            client.chunkCullingEnabled = previousChunkCullingEnabled;
            previousChunkCullingEnabled = null;
        }
        if (previousPerspective != null) {
            client.options.setPerspective(previousPerspective);
            previousPerspective = null;
        }

        active = false;
        previousCameraPos = null;
        cameraPos = null;
    }

    public boolean applyMouseDelta(MinecraftClient client, double deltaX, double deltaY) {
        if (!isActive() || client.player == null) {
            return false;
        }

        cameraYaw += (float)(deltaX * LOOK_DELTA_MULTIPLIER);
        cameraPitch = MathHelper.clamp(cameraPitch + (float)(deltaY * LOOK_DELTA_MULTIPLIER), -90.0F, 90.0F);
        return true;
    }

    private Vec3d computeMotion(MinecraftClient client, HuzzConfig config) {
        double sideways = 0.0D;
        double forward = 0.0D;
        if (client.options.leftKey.isPressed()) {
            sideways += 1.0D;
        }
        if (client.options.rightKey.isPressed()) {
            sideways -= 1.0D;
        }
        if (client.options.forwardKey.isPressed()) {
            forward += 1.0D;
        }
        if (client.options.backKey.isPressed()) {
            forward -= 1.0D;
        }

        double yawRadians = Math.toRadians(cameraYaw);
        double sinYaw = MathHelper.sin((float)yawRadians);
        double cosYaw = MathHelper.cos((float)yawRadians);
        double horizontalX = sideways * cosYaw - forward * sinYaw;
        double horizontalZ = sideways * sinYaw + forward * cosYaw;

        double vertical = 0.0D;
        if (client.options.jumpKey.isPressed()) {
            vertical += 1.0D;
        }
        if (client.options.sneakKey.isPressed()) {
            vertical -= 1.0D;
        }

        Vec3d motion = new Vec3d(horizontalX, 0.0D, horizontalZ).add(0.0D, vertical, 0.0D);
        if (motion.lengthSquared() == 0.0D) {
            return Vec3d.ZERO;
        }

        double speed = config.getFreeCamSpeed() * 0.11D;
        if (client.options.sprintKey.isPressed()) {
            speed *= 1.85D;
        }

        return motion.normalize().multiply(speed);
    }

    private static HitResult clampCrosshairTargetRange(HitResult hitResult, Vec3d cameraPosition, double interactionRange) {
        Vec3d hitPosition = hitResult.getPos();
        if (hitPosition.isInRange(cameraPosition, interactionRange)) {
            return hitResult;
        }

        Direction missDirection = Direction.getFacing(
            hitPosition.x - cameraPosition.x,
            hitPosition.y - cameraPosition.y,
            hitPosition.z - cameraPosition.z
        );
        return BlockHitResult.createMissed(hitPosition, missDirection, BlockPos.ofFloored(hitPosition));
    }
}
