package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public final class AreaMinerController {
    private static final double TARGET_TOLERANCE = 0.05D;
    private static final double CORRECTION_TOLERANCE = 0.12D;

    private final HuzzConfigManager configManager;

    private boolean initialized;
    private double originX;
    private double originY;
    private double originZ;
    private Phase phase = Phase.LANE;
    private float forwardYaw;
    private float reverseYaw;
    private float sideYaw;
    private float lockedYaw;
    private int forwardX;
    private int forwardZ;
    private int sideX;
    private int sideZ;
    private int layerIndex;
    private int laneIndex;
    private int direction = 1;
    private int sideStep = 1;
    private boolean forcingKeys;
    private boolean finished;

    public AreaMinerController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null || client.interactionManager == null || client.currentScreen != null) {
            clear(client);
            return;
        }

        HuzzConfig config = configManager.getConfig();
        if (!config.isAreaMinerEnabled()) {
            clear(client);
            return;
        }

        if (!initialized) {
            initialize(player);
        }

        advanceState(player, config);
        if (finished) {
            config.setAreaMinerEnabled(false);
            configManager.save();
            clear(client);
            return;
        }

        Command command = commandFor(player, config);
        forceCommand(client, player, command);
    }

    public void clear() {
        clear(MinecraftClient.getInstance());
    }

    private void clear(MinecraftClient client) {
        stopKeys(client);
        initialized = false;
        originX = 0.0D;
        originY = 0.0D;
        originZ = 0.0D;
        phase = Phase.LANE;
        forwardYaw = 0.0F;
        reverseYaw = 180.0F;
        sideYaw = -90.0F;
        lockedYaw = 0.0F;
        forwardX = 0;
        forwardZ = 1;
        sideX = 1;
        sideZ = 0;
        layerIndex = 0;
        laneIndex = 0;
        direction = 1;
        sideStep = 1;
        finished = false;
    }

    private void initialize(ClientPlayerEntity player) {
        initialized = true;
        originX = player.getX();
        originY = player.getY();
        originZ = player.getZ();
        forwardYaw = snapToRightAngle(player.getYaw());
        reverseYaw = oppositeYaw(forwardYaw);
        setDirectionVectors(forwardYaw);
        sideYaw = yawFromVector(sideX, sideZ);
        setupLayer(0, configManager.getConfig().getAreaMinerSize());
        lockedYaw = forwardYaw;
    }

    private void advanceState(ClientPlayerEntity player, HuzzConfig config) {
        int size = config.getAreaMinerSize();
        int layerCount = Math.max(1, config.getAreaMinerHeight() / 2);

        for (int transitions = 0; transitions < 4; transitions++) {
            if (phase == Phase.DESCEND) {
                if (!reachedDropTarget(player)) {
                    return;
                }

                setupLayer(layerIndex + 1, size);
                continue;
            }

            if (phase == Phase.CONNECTOR) {
                int nextLane = laneIndex + sideStep;
                if (!reachedSide(player, nextLane)) {
                    return;
                }

                laneIndex = nextLane;
                direction *= -1;
                phase = Phase.LANE;
                continue;
            }

            if (!reachedLaneEnd(player, size)) {
                return;
            }

            if (!isLastLane(size)) {
                phase = Phase.CONNECTOR;
                continue;
            }

            if (layerIndex >= layerCount - 1) {
                finished = true;
                return;
            }

            phase = Phase.DESCEND;
        }
    }

    private void setupLayer(int layer, int size) {
        layerIndex = layer;
        phase = Phase.LANE;
        if (layer % 2 == 0) {
            laneIndex = 0;
            sideStep = 1;
            direction = 1;
            return;
        }

        laneIndex = Math.max(0, size - 1);
        sideStep = -1;
        direction = size % 2 == 0 ? 1 : -1;
    }

    private Command commandFor(ClientPlayerEntity player, HuzzConfig config) {
        if (phase == Phase.DESCEND) {
            return new Command(lockedYaw, 90.0F, false, true);
        }

        int size = config.getAreaMinerSize();
        double laneSideError = sideCoordinate(player) - laneIndex;
        if (phase == Phase.LANE && Math.abs(laneSideError) > CORRECTION_TOLERANCE) {
            float yaw = laneSideError < 0.0D ? sideYaw : oppositeYaw(sideYaw);
            return new Command(yaw, 45.0F, true, true);
        }

        if (phase == Phase.CONNECTOR) {
            double targetForward = direction > 0 ? size - 1.0D : 0.0D;
            double forwardError = forwardCoordinate(player) - targetForward;
            if (Math.abs(forwardError) > CORRECTION_TOLERANCE) {
                float yaw = forwardError < 0.0D ? forwardYaw : reverseYaw;
                return new Command(yaw, 45.0F, true, true);
            }

            float yaw = sideStep > 0 ? sideYaw : oppositeYaw(sideYaw);
            return new Command(yaw, 45.0F, true, true);
        }

        float yaw = direction > 0 ? forwardYaw : reverseYaw;
        return new Command(yaw, 45.0F, true, true);
    }

    private boolean reachedLaneEnd(ClientPlayerEntity player, int size) {
        double forward = forwardCoordinate(player);
        return direction > 0 ? forward >= size - 1.0D - TARGET_TOLERANCE : forward <= TARGET_TOLERANCE;
    }

    private boolean reachedSide(ClientPlayerEntity player, int targetLane) {
        double side = sideCoordinate(player);
        return sideStep > 0 ? side >= targetLane - TARGET_TOLERANCE : side <= targetLane + TARGET_TOLERANCE;
    }

    private boolean reachedDropTarget(ClientPlayerEntity player) {
        return verticalDrop(player) >= (layerIndex + 1) * 2.0D - TARGET_TOLERANCE;
    }

    private boolean isLastLane(int size) {
        return sideStep > 0 ? laneIndex >= size - 1 : laneIndex <= 0;
    }

    private double forwardCoordinate(ClientPlayerEntity player) {
        return (player.getX() - originX) * forwardX + (player.getZ() - originZ) * forwardZ;
    }

    private double sideCoordinate(ClientPlayerEntity player) {
        return (player.getX() - originX) * sideX + (player.getZ() - originZ) * sideZ;
    }

    private double verticalDrop(ClientPlayerEntity player) {
        return originY - player.getY();
    }

    private void forceCommand(MinecraftClient client, ClientPlayerEntity player, Command command) {
        lockedYaw = command.yaw();
        player.setYaw(command.yaw());
        player.setHeadYaw(command.yaw());
        player.setBodyYaw(command.yaw());
        player.setPitch(command.pitch());
        player.lastYaw = command.yaw();
        player.lastPitch = command.pitch();
        client.options.forwardKey.setPressed(command.forward());
        client.options.attackKey.setPressed(command.attack());
        forcingKeys = true;
    }

    private void stopKeys(MinecraftClient client) {
        if (forcingKeys && client != null) {
            client.options.forwardKey.setPressed(false);
            client.options.attackKey.setPressed(false);
        }
        forcingKeys = false;
    }

    private void setDirectionVectors(float yaw) {
        if (yaw == 90.0F) {
            forwardX = -1;
            forwardZ = 0;
        } else if (yaw == 180.0F) {
            forwardX = 0;
            forwardZ = -1;
        } else if (yaw == -90.0F) {
            forwardX = 1;
            forwardZ = 0;
        } else {
            forwardX = 0;
            forwardZ = 1;
        }
        sideX = forwardZ;
        sideZ = -forwardX;
    }

    private static float snapToRightAngle(float yaw) {
        float normalized = ((yaw % 360.0F) + 360.0F) % 360.0F;
        int quadrant = Math.round(normalized / 90.0F) % 4;
        return switch (quadrant) {
            case 0 -> 0.0F;
            case 1 -> 90.0F;
            case 2 -> 180.0F;
            default -> -90.0F;
        };
    }

    private static float oppositeYaw(float yaw) {
        float opposite = yaw + 180.0F;
        if (opposite > 180.0F) {
            opposite -= 360.0F;
        }
        return opposite;
    }

    private static float yawFromVector(int x, int z) {
        if (x < 0) {
            return 90.0F;
        }
        if (x > 0) {
            return -90.0F;
        }
        return z < 0 ? 180.0F : 0.0F;
    }

    private enum Phase {
        LANE,
        CONNECTOR,
        DESCEND
    }

    private record Command(float yaw, float pitch, boolean forward, boolean attack) {
    }
}
