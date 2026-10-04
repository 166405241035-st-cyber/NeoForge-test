package com.example.examplemod.skill.farming;

import static com.example.examplemod.skill.passive.PassiveEffectSupport.canUseTimedTrigger;
import static com.example.examplemod.skill.passive.PassiveEffectSupport.tierValue;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.skill.EffectTier;
import com.example.examplemod.skill.ForgedEffectRuntime;
import com.example.examplemod.skill.ForgedFarmingPlotData;
import com.example.examplemod.skill.ForgedSkillSounds;
import com.example.examplemod.skill.ForgingEffect;
import com.example.examplemod.skill.blessing.DoubleTriggerRuntime;
import com.example.examplemod.skill.blessing.ForgedBlessingRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Tilling, crop growth, farming plots and placement protection. */
public final class FarmingEffectHandlers {
    private FarmingEffectHandlers() {}

    private static final double[] ROTTEN_COMPOST_CHANCE = {0.15D, 0.25D, 0.35D};

    private static final double[] ORGANIC_CATALYST_COOLDOWN = {200.0D, 140.0D, 100.0D};

    private static final double[] NETHER_MUTATION_CHANCE = {0.05D, 0.10D, 0.20D};

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        ItemStack tool = player.getMainHandItem();
        BlockPos clicked = event.getPos();

        EffectTier explosiveTilling = ForgedEffectRuntime.tier(tool, ForgingEffect.EXPLOSIVE_TILLING);
        if (explosiveTilling != null) {
            long cd = switch (explosiveTilling) { case I -> 120L; case II -> 80L; case III -> 40L; };
            if (canUseTimedTrigger(player, "ExplosiveTilling", cd)) {
                // Normal cast tills one 3x3 patch. Double Trigger adds a second
                // non-overlapping 3x3 patch in front, using the same cooldown.
                int changed = tillThreeByThree(player, clicked);
                if (DoubleTriggerRuntime.rollResult(player, tool)) {
                    BlockPos bonusCenter = clicked.relative(player.getDirection(), 3);
                    tillThreeByThree(player, bonusCenter);
                }

                // Only the normal patch contributes durability cost.
                if (changed > 0 && tool.isDamageableItem())
                    ForgedBlessingRuntime.damage(tool, changed);
                if (changed > 0) ForgedSkillSounds.play(player, ForgingEffect.EXPLOSIVE_TILLING);
            }
        }

        boolean tillableSoil = player.level().getBlockState(clicked).is(Blocks.DIRT)
                || player.level().getBlockState(clicked).is(Blocks.GRASS_BLOCK)
                || player.level().getBlockState(clicked).is(Blocks.DIRT_PATH)
                || player.level().getBlockState(clicked).is(Blocks.FARMLAND);

        // Rotten Compost no longer hydrates farmland. It only rolls a bonus soil-block drop on tilling.
        EffectTier rottenCompost = ForgedEffectRuntime.tier(tool, ForgingEffect.ROTTEN_COMPOST);
        if (rottenCompost != null && tillableSoil
                && player.getRandom().nextDouble() < tierValue(rottenCompost, ROTTEN_COMPOST_CHANCE)) {
            // Rotten Compost reward pool follows the eight soil blocks from the design reference.
            ItemStack soilDrop = switch (player.getRandom().nextInt(8)) {
                case 0 -> new ItemStack(Blocks.GRASS_BLOCK);
                case 1 -> new ItemStack(Blocks.PODZOL);
                case 2 -> new ItemStack(Blocks.MYCELIUM);
                case 3 -> new ItemStack(Blocks.DIRT_PATH);
                case 4 -> new ItemStack(Blocks.COARSE_DIRT);
                case 5 -> new ItemStack(Blocks.ROOTED_DIRT);
                case 6 -> new ItemStack(Blocks.DIRT);
                default -> new ItemStack(Blocks.FARMLAND);
            };
            if (DoubleTriggerRuntime.rollResult(player, tool)) soilDrop.setCount(2);
            Block.popResource(player.level(), clicked, soilDrop);
            ForgedSkillSounds.play(player, ForgingEffect.ROTTEN_COMPOST);
        }

        EffectTier floraAegis = ForgedEffectRuntime.tier(tool, ForgingEffect.FLORA_AEGIS);
        if (floraAegis != null && tillableSoil) {
            player.getPersistentData().putLong("ForgedProtectedFarmlandPos", clicked.asLong());
        }

        EffectTier organic = ForgedEffectRuntime.tier(tool, ForgingEffect.ORGANIC_CATALYST);
        if (organic != null && canUseTimedTrigger(player, "OrganicCatalyst", Math.round(tierValue(organic, ORGANIC_CATALYST_COOLDOWN)))) {
            BlockPos center = clicked.above();
            boolean doubled = DoubleTriggerRuntime.rollResult(player, tool);
            boolean grewCrop = false;
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, 0, -1), center.offset(1, 0, 1))) {
                if (isCrop(player.level().getBlockState(pos))) {
                    grewCrop = true;
                    BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), player.level(), pos, player);
                    if (doubled) {
                        BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), player.level(), pos, player);
                    }
                }
            }
            if (grewCrop) ForgedSkillSounds.play(player, ForgingEffect.ORGANIC_CATALYST);
        }

    }

    public static void onNatureGodBlessHeldAura(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        if (serverLevel.getGameTime() % 10L != 0L) return;

        // Nature God Bless growth aura follows the player while the forged equipment
        // is held. Tilling is NOT required for this visual/growth-range indicator.
        ItemStack held = player.getMainHandItem();
        if (ForgedEffectRuntime.tier(held, ForgingEffect.NATURE_GOD_BLESS) == null) return;

        double radius = 4.0D;

        // Soft gold dust. Dust particles support RGB + scale, so this is much less
        // visually harsh than END_ROD. Vanilla particles do not expose true alpha,
        // therefore the "transparent" look is achieved with small scale, fewer points
        // and slower refresh.
        var softGold = new net.minecraft.core.particles.DustParticleOptions(
                new org.joml.Vector3f(1.0F, 1.0F, 0.0F), 0.45F);

        for (int i = 0; i < 24; i++) {
            double angle = Math.PI * 2.0D * i / 24.0D;
            serverLevel.sendParticles(softGold,
                    player.getX() + Math.cos(angle) * radius,
                    player.getY() + 1.12D,
                    player.getZ() + Math.sin(angle) * radius,
                    1, 0.015D, 0.015D, 0.015D, 0.0D);
        }
    }

    public static void onHyperGrowthPlotParticles(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (serverLevel.getGameTime() % 40L != 0L) return;

        for (BlockPos farmlandPos : ForgedFarmingPlotData.get(serverLevel)
                .positionsWith(ForgingEffect.HYPER_GROWTH_SOIL)) {
            if (!serverLevel.isLoaded(farmlandPos)) continue;
            BlockState soil = serverLevel.getBlockState(farmlandPos);
            if (!soil.is(Blocks.FARMLAND) && !soil.is(ExampleMod.MOISTURE_RETAIN_FARMLAND.get())) continue;

            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,
                    farmlandPos.getX() + 0.5D, farmlandPos.getY() + 1.08D, farmlandPos.getZ() + 0.5D,
                    2, 0.30D, 0.03D, 0.30D, 0.005D);
        }
    }

    public static void onNatureGodGrowth(CropGrowEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        BlockState cropState = event.getState();
        if (!(cropState.getBlock() instanceof net.minecraft.world.level.block.CropBlock crop)
                || crop.isMaxAge(cropState)) return;

        EffectTier tier = ForgedFarmingPlotData.get(serverLevel)
                .tier(event.getPos().below(), ForgingEffect.NATURE_GOD_BLESS);
        if (tier == null) return;

        // Nature's blessing automatically helps crops grow. Higher tiers trigger more often.
        double growChance = switch (tier) {
            case I -> 0.20D;
            case II -> 0.35D;
            case III -> 0.50D;
        };
        if (serverLevel.random.nextDouble() < growChance) {
            event.setResult(CropGrowEvent.Pre.Result.GROW);
        }
    }

    public static void onHyperGrowth(CropGrowEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        BlockState cropState = event.getState();
        if (!(cropState.getBlock() instanceof net.minecraft.world.level.block.CropBlock crop)
                || crop.isMaxAge(cropState)) return;

        EffectTier tier = ForgedFarmingPlotData.get(serverLevel)
                .tier(event.getPos().below(), ForgingEffect.HYPER_GROWTH_SOIL);
        if (tier == null) return;

        // Hyper Growth is intentionally very noticeable:
        // Tier I = 3x, II = 4x, III = 5x.
        // Force the current natural attempt, then schedule extra crop ticks to
        // approximate the remaining multiplier without requiring the hoe to stay held.
        int multiplier = switch (tier) {
            case I -> 3;
            case II -> 4;
            case III -> 5;
        };
        event.setResult(CropGrowEvent.Pre.Result.GROW);
        for (int i = 1; i < multiplier; i++) {
            serverLevel.scheduleTick(event.getPos(), cropState.getBlock(), i);
        }

        // Purple visual feedback over the affected plot.
        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,
                event.getPos().getX() + 0.5D, event.getPos().getY() + 0.35D, event.getPos().getZ() + 0.5D,
                5, 0.28D, 0.12D, 0.28D, 0.02D);
    }

    public static void onFarmlandTrample(net.neoforged.neoforge.event.level.BlockEvent.FarmlandTrampleEvent event) {
        if (event.getLevel().isClientSide()) return;
        long trampled = event.getPos().asLong();

        // Flora Aegis is stamped when the plot is tilled. The owner does not need to
        // hold the tool or stand beside the crop afterward.
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            for (net.minecraft.server.level.ServerPlayer owner : serverLevel.getServer().getPlayerList().getPlayers()) {
                if (owner.level() == serverLevel
                        && owner.getPersistentData().getLong("ForgedProtectedFarmlandPos") == trampled) {
                    event.setCanceled(true);
                    ForgedSkillSounds.play(owner, ForgingEffect.FLORA_AEGIS);
                    return;
                }
            }
        }
    }

    public static void onEntityPlace(EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Player player)) return;

        // Nether Mutation belongs to the tilled plot. Planting on that plot can mutate
        // even when the forged hoe is no longer held.
        if (!isPlantableCrop(event.getPlacedBlock()) || !(player.level() instanceof ServerLevel serverLevel)) return;
        BlockPos farmlandPos = event.getPos().below();
        EffectTier mutation = ForgedFarmingPlotData.get(serverLevel)
                .tier(farmlandPos, ForgingEffect.NETHER_MUTATION);
        if (mutation == null
                || player.getRandom().nextDouble() >= tierValue(mutation, NETHER_MUTATION_CHANCE)) return;

        // The planted crop mutates into either Nether Wart or a Wither Rose.
        Block mutatedBlock = player.getRandom().nextBoolean() ? Blocks.NETHER_WART : Blocks.WITHER_ROSE;
        player.level().setBlockAndUpdate(event.getPos(), mutatedBlock.defaultBlockState());
        ForgedSkillSounds.play(player, ForgingEffect.NETHER_MUTATION);

        // A second mutation cannot replace the same planted block twice in a useful way,
        // so Double Trigger pays out one matching mutation result as the bonus result.
        if (ForgedFarmingPlotData.get(serverLevel)
                .hasDoubleTrigger(farmlandPos, ForgingEffect.NETHER_MUTATION)
                && player.getRandom().nextDouble() < DoubleTriggerRuntime.CHANCE) {
            Block.popResource(player.level(), event.getPos(), new ItemStack(mutatedBlock));
        }
    }

    private static int tillThreeByThree(Player player, BlockPos center) {
        int changed = 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, 0, -1), center.offset(1, 0, 1))) {
            BlockState state = player.level().getBlockState(pos);
            if ((state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT_PATH)
                    || state.is(Blocks.FARMLAND))
                    && player.level().getBlockState(pos.above()).isAir()) {
                if (!state.is(Blocks.FARMLAND)) changed++;
                player.level().setBlockAndUpdate(pos, Blocks.FARMLAND.defaultBlockState());
            }
        }
        return changed;
    }

    public static boolean isCrop(net.minecraft.world.level.block.state.BlockState state) {
        return state.is(Blocks.WHEAT) || state.is(Blocks.CARROTS) || state.is(Blocks.POTATOES)
                || state.is(Blocks.BEETROOTS) || state.is(Blocks.NETHER_WART)
                || state.is(Blocks.MELON) || state.is(Blocks.PUMPKIN);
    }

    public static boolean isMatureHarvestCrop(net.minecraft.world.level.block.state.BlockState state) {
        if (state.getBlock() instanceof net.minecraft.world.level.block.CropBlock crop) {
            return crop.isMaxAge(state);
        }
        if (state.is(Blocks.NETHER_WART)) {
            return state.getValue(net.minecraft.world.level.block.NetherWartBlock.AGE)
                    >= net.minecraft.world.level.block.NetherWartBlock.MAX_AGE;
        }
        // Melon and pumpkin blocks are already the finished harvest product.
        return state.is(Blocks.MELON) || state.is(Blocks.PUMPKIN);
    }

    private static boolean isPlantableCrop(net.minecraft.world.level.block.state.BlockState state) {
        return state.is(Blocks.WHEAT) || state.is(Blocks.CARROTS) || state.is(Blocks.POTATOES)
                || state.is(Blocks.BEETROOTS) || state.is(Blocks.NETHER_WART)
                || state.is(Blocks.PUMPKIN_STEM) || state.is(Blocks.MELON_STEM);
    }
}
