package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

import java.util.List;
import java.util.Set;

public final class VisualAliasController {
    private static final int PARTICLE_SCAN_RADIUS_XZ = 8;
    private static final int PARTICLE_SCAN_RADIUS_Y = 4;
    private static final int MAX_PARTICLE_MATCHES = 6;

    private final HuzzConfigManager configManager;
    private final Random particleRandom = Random.create();
    private final ThreadLocal<Boolean> tooltipGuard = ThreadLocal.withInitial(() -> false);
    private int particleTicker;
    private static final Set<Block> BEDROCK_PROTECT_BLOCKS = Set.of(
        Blocks.BEDROCK,
        Blocks.GRAVEL,
        Blocks.TUFF,
        Blocks.DEEPSLATE,
        Blocks.DEEPSLATE_COAL_ORE,
        Blocks.DEEPSLATE_COPPER_ORE,
        Blocks.DEEPSLATE_IRON_ORE,
        Blocks.DEEPSLATE_GOLD_ORE,
        Blocks.DEEPSLATE_REDSTONE_ORE,
        Blocks.DEEPSLATE_EMERALD_ORE,
        Blocks.DEEPSLATE_LAPIS_ORE,
        Blocks.DEEPSLATE_DIAMOND_ORE
    );

    public VisualAliasController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }

        HuzzConfig config = configManager.getConfig();
        if (!config.isFakeSpawnerEnabled()) {
            return;
        }

        Block targetBlock = resolveBlock(config.getFakeSpawnerBlockId());
        if (targetBlock == null) {
            return;
        }

        if ((particleTicker++ & 3) != 0) {
            return;
        }

        BlockPos center = client.player.getBlockPos();
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int matches = 0;
        for (int y = center.getY() - PARTICLE_SCAN_RADIUS_Y; y <= center.getY() + PARTICLE_SCAN_RADIUS_Y && matches < MAX_PARTICLE_MATCHES; y++) {
            for (int z = center.getZ() - PARTICLE_SCAN_RADIUS_XZ; z <= center.getZ() + PARTICLE_SCAN_RADIUS_XZ && matches < MAX_PARTICLE_MATCHES; z++) {
                for (int x = center.getX() - PARTICLE_SCAN_RADIUS_XZ; x <= center.getX() + PARTICLE_SCAN_RADIUS_XZ && matches < MAX_PARTICLE_MATCHES; x++) {
                    mutable.set(x, y, z);
                    if (!client.world.getBlockState(mutable).isOf(targetBlock)) {
                        continue;
                    }

                    spawnSpawnerParticles(client, mutable);
                    matches++;
                }
            }
        }
    }

    public BlockState aliasBlockState(BlockState originalState) {
        if (originalState == null) {
            return null;
        }

        HuzzConfig config = configManager.getConfig();
        if (config.isBedrockProtectEnabled() && BEDROCK_PROTECT_BLOCKS.contains(originalState.getBlock())) {
            return Blocks.NETHERITE_BLOCK.getDefaultState();
        }

        if (!config.isFakeSpawnerEnabled()) {
            return originalState;
        }

        Block targetBlock = resolveBlock(config.getFakeSpawnerBlockId());
        if (targetBlock != null && originalState.isOf(targetBlock)) {
            return Blocks.SPAWNER.getDefaultState();
        }
        return originalState;
    }

    public ItemStack aliasItemStack(ItemStack originalStack) {
        if (originalStack == null || originalStack.isEmpty()) {
            return originalStack;
        }

        HuzzConfig config = configManager.getConfig();
        Block fakeSpawnerBlock = resolveBlock(config.getFakeSpawnerBlockId());
        if (config.isFakeSpawnerEnabled() && fakeSpawnerBlock != null && Block.getBlockFromItem(originalStack.getItem()) == fakeSpawnerBlock) {
            return new ItemStack(Blocks.SPAWNER, originalStack.getCount());
        }

        Item fakeElytraItem = resolveItem(config.getFakeElytraItemId());
        if (config.isFakeElytraEnabled() && fakeElytraItem != null && originalStack.isOf(fakeElytraItem)) {
            return new ItemStack(net.minecraft.item.Items.ELYTRA, originalStack.getCount());
        }

        return originalStack;
    }

    public ItemStack aliasChestEquipment(ItemStack originalStack) {
        if (originalStack == null || originalStack.isEmpty()) {
            return originalStack;
        }

        HuzzConfig config = configManager.getConfig();
        Item fakeElytraItem = resolveItem(config.getFakeElytraItemId());
        if (config.isFakeElytraEnabled() && fakeElytraItem != null && originalStack.isOf(fakeElytraItem)) {
            return new ItemStack(net.minecraft.item.Items.ELYTRA, originalStack.getCount());
        }

        return originalStack;
    }

    public Text aliasBlockName(Block block, Text originalName) {
        if (block == null) {
            return originalName;
        }

        HuzzConfig config = configManager.getConfig();
        Block fakeSpawnerBlock = resolveBlock(config.getFakeSpawnerBlockId());
        if (config.isFakeSpawnerEnabled() && fakeSpawnerBlock != null && block == fakeSpawnerBlock) {
            return Text.literal("Skeleton Spawner");
        }

        return originalName;
    }

    public Text aliasItemName(ItemStack stack, Text originalName) {
        if (isFakeSpawnerStack(stack)) {
            return Text.literal("Skeleton Spawner");
        }

        ItemStack aliased = aliasItemStack(stack);
        return aliased == stack ? originalName : aliased.getName();
    }

    public List<Text> aliasTooltip(ItemStack stack, TooltipContext context, PlayerEntity player, TooltipType type, List<Text> originalTooltip) {
        if (tooltipGuard.get()) {
            return originalTooltip;
        }

        if (isFakeSpawnerStack(stack)) {
            return skeletonSpawnerTooltip();
        }
        if (isFakeElytraStack(stack)) {
            return fakeElytraTooltip();
        }

        ItemStack aliased = aliasItemStack(stack);
        if (aliased == stack) {
            return originalTooltip;
        }

        tooltipGuard.set(true);
        try {
            aliased.getTooltip(context, player, type);
            return originalTooltip;
        } finally {
            tooltipGuard.set(false);
        }
    }

    private void spawnSpawnerParticles(MinecraftClient client, BlockPos pos) {
        double x = pos.getX() + particleRandom.nextDouble();
        double y = pos.getY() + particleRandom.nextDouble();
        double z = pos.getZ() + particleRandom.nextDouble();
        client.world.addParticleClient(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
        client.world.addParticleClient(ParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    private static Block resolveBlock(String rawId) {
        Identifier id = parseId(rawId);
        return id == null ? null : Registries.BLOCK.getOptionalValue(id).orElse(null);
    }

    private static Item resolveItem(String rawId) {
        Identifier id = parseId(rawId);
        return id == null ? null : Registries.ITEM.getOptionalValue(id).orElse(null);
    }

    private static Identifier parseId(String rawId) {
        if (rawId == null || rawId.isBlank()) {
            return null;
        }

        return rawId.contains(":")
            ? Identifier.tryParse(rawId)
            : Identifier.tryParse("minecraft", rawId);
    }

    private boolean isFakeSpawnerStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        HuzzConfig config = configManager.getConfig();
        Block fakeSpawnerBlock = resolveBlock(config.getFakeSpawnerBlockId());
        return config.isFakeSpawnerEnabled() && fakeSpawnerBlock != null && Block.getBlockFromItem(stack.getItem()) == fakeSpawnerBlock;
    }

    private boolean isFakeElytraStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        HuzzConfig config = configManager.getConfig();
        Item fakeElytraItem = resolveItem(config.getFakeElytraItemId());
        return config.isFakeElytraEnabled() && fakeElytraItem != null && stack.isOf(fakeElytraItem);
    }

    private static List<Text> skeletonSpawnerTooltip() {
        return List.of(
            Text.literal("Spawner").styled(style -> style.withColor(0xFF55FF).withItalic(false)),
            Text.literal("Skeleton").styled(style -> style.withColor(0xFFFF55).withItalic(false)),
            Text.empty()
                .append(Text.literal("Worth: ").styled(style -> style.withColor(0xAAAAAA).withItalic(false)))
                .append(Text.literal("$0").styled(style -> style.withColor(0x55FF55).withItalic(false))),
            Text.empty(),
            Text.literal("Interact with Spawn Egg:").styled(style -> style.withColor(0xAAAAAA).withItalic(false)),
            Text.literal("Sets Mob Type").styled(style -> style.withColor(0x5555FF).withItalic(false))
        );
    }

    private static List<Text> fakeElytraTooltip() {
        return List.of(
            Text.literal("Elytra").styled(style -> style.withColor(0xFF55FF).withItalic(false)),
            Text.literal("Unbreaking III").styled(style -> style.withColor(0xAAAAAA).withItalic(false)),
            Text.literal("Mending").styled(style -> style.withColor(0xAAAAAA).withItalic(false)),
            Text.empty()
                .append(Text.literal("Worth: ").styled(style -> style.withColor(0xAAAAAA).withItalic(false)))
                .append(Text.literal("$33.88K").styled(style -> style.withColor(0x55FF55).withItalic(false)))
        );
    }
}
