package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public final class AimAssistController {
    private static final double MAX_TARGET_DISTANCE = 96.0D;
    private static final float MAX_ASSIST_STEP = 4.0F;

    private final HuzzConfigManager configManager;

    public AimAssistController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public boolean applyMouseDelta(MinecraftClient client, ClientPlayerEntity player, double deltaX, double deltaY) {
        if (client.currentScreen != null) {
            return false;
        }

        HuzzConfig config = configManager.getConfig();
        player.changeLookDirection(deltaX, deltaY);
        if (!config.isAimAssistEnabled() || !matchesSlot(config, player)) {
            return true;
        }

        LivingEntity target = findTarget(client, player, config);
        if (target == null) {
            return true;
        }

        Vec3d aimPoint = aimPoint(target, config.getAimAssistTarget());
        Rotation targetRotation = rotationTo(player.getCameraPosVec(1.0F), aimPoint);
        float yawDelta = MathHelper.wrapDegrees(targetRotation.yaw() - player.getYaw());
        float pitchDelta = targetRotation.pitch() - player.getPitch();

        if (config.isAimFovEnabled()) {
            ScreenOffset offset = projectScreenOffset(client, aimPoint);
            if (offset == null || offset.distanceSquared() > (double) config.getAimFovRadius() * config.getAimFovRadius()) {
                return true;
            }
        }

        float strength = (float) config.getAimAssistStrength();
        player.setYaw(player.getYaw() + clampAssist(yawDelta, strength));
        player.setPitch(MathHelper.clamp(player.getPitch() + clampAssist(pitchDelta, strength * 0.8F), -90.0F, 90.0F));
        player.setHeadYaw(player.getYaw());
        return true;
    }

    private LivingEntity findTarget(MinecraftClient client, ClientPlayerEntity player, HuzzConfig config) {
        double maxDistanceSquared = MAX_TARGET_DISTANCE * MAX_TARGET_DISTANCE;
        LivingEntity bestTarget = null;
        double bestScore = Double.MAX_VALUE;

        for (LivingEntity candidate : client.world.getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(MAX_TARGET_DISTANCE), entity ->
            entity != player && entity.isAlive() && !entity.isSpectator())) {
            Vec3d aimPoint = aimPoint(candidate, config.getAimAssistTarget());
            Rotation targetRotation = rotationTo(player.getCameraPosVec(1.0F), aimPoint);
            float yawDelta = MathHelper.wrapDegrees(targetRotation.yaw() - player.getYaw());
            float pitchDelta = targetRotation.pitch() - player.getPitch();
            double distanceSquared = player.squaredDistanceTo(candidate);
            if (distanceSquared > maxDistanceSquared) {
                continue;
            }

            if (config.isAimAssistVisibleOnly() && !isVisible(client, player, aimPoint)) {
                continue;
            }

            if (config.isAimFovEnabled()) {
                ScreenOffset offset = projectScreenOffset(client, aimPoint);
                if (offset == null || offset.distanceSquared() > (double) config.getAimFovRadius() * config.getAimFovRadius()) {
                    continue;
                }
            }

            double score = yawDelta * yawDelta + pitchDelta * pitchDelta + distanceSquared * 0.0008D;
            if (score < bestScore) {
                bestScore = score;
                bestTarget = candidate;
            }
        }

        return bestTarget;
    }

    private static boolean matchesSlot(HuzzConfig config, ClientPlayerEntity player) {
        int requiredSlot = config.getAimAssistSlot();
        return requiredSlot == 0 || player.getInventory().getSelectedSlot() == requiredSlot - 1;
    }

    private static float clampAssist(float delta, float factor) {
        return MathHelper.clamp(delta * factor, -MAX_ASSIST_STEP, MAX_ASSIST_STEP);
    }

    private static Rotation rotationTo(Vec3d origin, Vec3d target) {
        Vec3d delta = target.subtract(origin);
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        float yaw = (float) Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(delta.y, horizontal));
        return new Rotation(yaw, pitch);
    }

    private static Vec3d aimPoint(LivingEntity entity, HuzzConfig.AimAssistTarget target) {
        double yOffset = switch (target) {
            case HEAD -> entity.getStandingEyeHeight() - 0.12D;
            case NECK -> entity.getStandingEyeHeight() - 0.32D;
            case CHEST -> entity.getStandingEyeHeight() * 0.72D;
        };
        return new Vec3d(entity.getX(), entity.getY() + yOffset, entity.getZ());
    }

    private static boolean isVisible(MinecraftClient client, ClientPlayerEntity player, Vec3d aimPoint) {
        if (client.world == null) {
            return false;
        }

        Vec3d cameraPos = player.getCameraPosVec(1.0F);
        HitResult blockHit = client.world.raycast(new RaycastContext(
            cameraPos,
            aimPoint,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            player
        ));
        return blockHit.getType() == HitResult.Type.MISS || cameraPos.squaredDistanceTo(blockHit.getPos()) + 1.0E-4D >= cameraPos.squaredDistanceTo(aimPoint);
    }

    public ScreenOffset currentScreenOffset(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) {
            return null;
        }

        HuzzConfig config = configManager.getConfig();
        if (!config.isAimAssistEnabled() || !config.isAimFovEnabled() || !matchesSlot(config, player)) {
            return null;
        }

        LivingEntity target = findTarget(client, player, config);
        if (target == null) {
            return null;
        }

        Vec3d aimPoint = aimPoint(target, config.getAimAssistTarget());
        return projectScreenOffset(client, aimPoint);
    }

    private static ScreenOffset projectScreenOffset(MinecraftClient client, Vec3d worldPos) {
        if (client.gameRenderer == null) {
            return null;
        }

        Vec3d projected = client.gameRenderer.project(worldPos);
        if (projected.z < 0.0D || projected.z > 1.0D) {
            return null;
        }

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        double offsetX = projected.x * (width / 2.0D);
        double offsetY = -projected.y * (height / 2.0D);
        return new ScreenOffset(offsetX, offsetY);
    }

    public record ScreenOffset(double x, double y) {
        public double distanceSquared() {
            return x * x + y * y;
        }
    }

    private record Rotation(float yaw, float pitch) {
    }
}
