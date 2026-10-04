package com.example.examplemod.skill.mining;

import static com.example.examplemod.skill.farming.FarmingEffectHandlers.isCrop;
import static com.example.examplemod.skill.farming.FarmingEffectHandlers.isMatureHarvestCrop;
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
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Mining speed, drops and block-break effects, including crop harvests. */
public final class MiningEffectHandlers {
    private MiningEffectHandlers() {}

    private static final double[] BONE_DUST_CHANCE = {0.10D, 0.20D, 0.30D};

    private static final double[] SOUL_SAND_CHANCE = {0.10D, 0.20D, 0.35D};

    private static final double[] SCAVENGER_CHANCE = {0.05D, 0.10D, 0.15D};

    private static final double[] UNREFINED_ORE_CHANCE = {0.04D, 0.08D, 0.12D};

    private static final double[] AIRBORNE_MINING_SPEED = {0.25D, 0.45D, 0.70D};

    private static final double[] HEALING_HARVEST_CHANCE = {0.05D, 0.10D, 0.18D};

    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        ItemStack tool = player.getMainHandItem();
        float speed = event.getNewSpeed();

        EffectTier airborne = ForgedEffectRuntime.tier(tool, ForgingEffect.AIRBORNE_MINING);
        if (airborne != null && !player.onGround()) {
            double bonus = tierValue(airborne, AIRBORNE_MINING_SPEED);
            speed = (float)(speed * 5.0D * (1.0D + bonus));
        }

        EffectTier frenzy = ForgedEffectRuntime.tier(tool, ForgingEffect.FRENZY_DIGGING);
        if (frenzy != null
                && player.getPersistentData().getInt("ForgedFrenzyChain") >= 5
                && player.level().getGameTime() - player.getPersistentData().getLong("ForgedFrenzyLastMine") <= 100L) {
            double bonus = switch (frenzy) {
                case I -> 0.15D;
                case II -> 0.30D;
                case III -> 0.50D;
            };
            speed = (float)(speed * (1.0D + bonus));
        }
        event.setNewSpeed(speed);
    }

    public static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof Player player)) return;
        ItemStack tool = event.getTool();

        // Ultimate Laser Breaker behaves as Fortune V for normal mining.
        // Rebuild this block's vanilla loot with a temporary Fortune V copy of the forged tool,
        // so every block uses Minecraft's own Fortune loot table instead of our old 0..5 bonus.
        EffectTier ultimate = ForgedEffectRuntime.tier(tool, ForgingEffect.ULTIMATE_LASER_BREAKER);
        if (ultimate != null && event.getLevel() instanceof ServerLevel serverLevel
                && !event.getState().is(Blocks.BEDROCK)) {
            ItemStack fortuneTool = tool.copy();
            Holder<Enchantment> fortune = serverLevel.registryAccess()
                    .registryOrThrow(Registries.ENCHANTMENT)
                    .getHolderOrThrow(Enchantments.FORTUNE);
            fortuneTool.enchant(fortune, 5);

            java.util.List<ItemStack> fortuneDrops = Block.getDrops(
                    event.getState(), serverLevel, event.getPos(), event.getBlockEntity(), player, fortuneTool);
            event.getDrops().clear();
            for (ItemStack dropStack : fortuneDrops) {
                if (!dropStack.isEmpty()) {
                    BlockPos pos = event.getPos();
                    event.getDrops().add(new ItemEntity(serverLevel,
                            pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, dropStack));
                }
            }
        }

        EffectTier autoSmelt = ForgedEffectRuntime.tier(tool, ForgingEffect.AUTO_SMELT_MINING);
        if (autoSmelt != null) {
            Item smelted = null;
            if (event.getState().is(Blocks.IRON_ORE) || event.getState().is(Blocks.DEEPSLATE_IRON_ORE)) smelted = Items.IRON_INGOT;
            else if (event.getState().is(Blocks.GOLD_ORE) || event.getState().is(Blocks.DEEPSLATE_GOLD_ORE)
                    || event.getState().is(Blocks.NETHER_GOLD_ORE)) smelted = Items.GOLD_INGOT;
            else if (event.getState().is(Blocks.COPPER_ORE) || event.getState().is(Blocks.DEEPSLATE_COPPER_ORE)) smelted = Items.COPPER_INGOT;

            if (smelted != null) {
                int output = switch (autoSmelt) {
                    case I -> 1;
                    case II -> 1 + (player.getRandom().nextBoolean() ? 1 : 0);
                    case III -> 2;
                };
                if (DoubleTriggerRuntime.rollResult(player, tool)) output *= 2;

                event.getDrops().clear();
                BlockPos pos = event.getPos();
                event.getDrops().add(new ItemEntity(event.getLevel(),
                        pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        new ItemStack(smelted, output)));
                ForgedSkillSounds.play(player, ForgingEffect.AUTO_SMELT_MINING);
            }
        }

        // Auto Chest Transport belongs to the tilled plot, so the harvesting tool
        // does not matter. Crop drops from this exact plot are inserted directly into
        // the nearest container within the tier range.
        if (event.getLevel() instanceof ServerLevel farmingLevel && isCrop(event.getState())) {
            BlockPos farmlandPos = event.getPos().below();
            EffectTier autoChest = ForgedFarmingPlotData.get(farmingLevel)
                    .tier(farmlandPos, ForgingEffect.AUTO_CHEST_TRANSPORT);
            if (autoChest != null && !event.getDrops().isEmpty()) {
                int range = switch (autoChest) { case I -> 8; case II -> 16; case III -> 32; };
                net.minecraft.world.Container destination = null;
                double best = Double.MAX_VALUE;
                for (BlockPos pos : BlockPos.betweenClosed(
                        farmlandPos.offset(-range, -4, -range), farmlandPos.offset(range, 4, range))) {
                    net.minecraft.world.level.block.entity.BlockEntity be = farmingLevel.getBlockEntity(pos);
                    if (be instanceof net.minecraft.world.Container container) {
                        double dist = pos.distSqr(farmlandPos);
                        if (dist <= (double) range * range && dist < best) {
                            best = dist;
                            destination = container;
                        }
                    }
                }

                if (destination != null) {
                    boolean movedAny = false;
                    for (java.util.Iterator<ItemEntity> it = event.getDrops().iterator(); it.hasNext();) {
                        ItemEntity drop = it.next();
                        ItemStack remaining = drop.getItem().copy();
                        int originalCount = remaining.getCount();
                        for (int slot = 0; slot < destination.getContainerSize() && !remaining.isEmpty(); slot++) {
                            ItemStack existing = destination.getItem(slot);
                            if (existing.isEmpty()) {
                                int move = Math.min(remaining.getCount(), remaining.getMaxStackSize());
                                ItemStack inserted = remaining.copy();
                                inserted.setCount(move);
                                destination.setItem(slot, inserted);
                                remaining.shrink(move);
                            } else if (ItemStack.isSameItemSameComponents(existing, remaining)
                                    && existing.getCount() < existing.getMaxStackSize()) {
                                int move = Math.min(remaining.getCount(),
                                        existing.getMaxStackSize() - existing.getCount());
                                existing.grow(move);
                                remaining.shrink(move);
                                destination.setItem(slot, existing);
                            }
                        }
                        if (remaining.isEmpty()) {
                            it.remove();
                        } else {
                            drop.setItem(remaining);
                        }
                        movedAny |= remaining.getCount() < originalCount;
                    }
                    destination.setChanged();
                    if (movedAny) ForgedSkillSounds.play(player, ForgingEffect.AUTO_CHEST_TRANSPORT);
                }
            }
        }

        // Nature God Bless R: for 15 seconds, mature crops harvested from a
        // Nature-God-Bless plot receive a Fortune-III-like randomized yield bonus.
        // This deliberately applies to crop drops only; Golden Apple rewards remain
        // the separate passive 5/10/20% plot roll.
        if (event.getLevel() instanceof ServerLevel natureFortuneLevel
                && isMatureHarvestCrop(event.getState())
                && player.getPersistentData().getLong("ForgedNatureGodBlessFortuneUntil")
                        > natureFortuneLevel.getGameTime()
                && ForgedFarmingPlotData.get(natureFortuneLevel)
                        .tier(event.getPos().below(), ForgingEffect.NATURE_GOD_BLESS) != null) {
            for (ItemEntity drop : event.getDrops()) {
                ItemStack stack = drop.getItem();
                if (stack.isEmpty()) continue;

                // Fortune III style: random bonus 0..3 additional copies of each
                // normal crop drop stack, rather than a fixed multiplier.
                int bonusCopies = player.getRandom().nextInt(4);
                if (player.getPersistentData().getLong("ForgedNatureGodBlessDoubleUntil")
                        > natureFortuneLevel.getGameTime()) {
                    bonusCopies += player.getRandom().nextInt(4);
                }
                if (bonusCopies > 0) {
                    int bonus = stack.getCount() * bonusCopies;
                    stack.grow(bonus);
                    drop.setItem(stack);
                }
            }
        }

        // Static Hover Drop: freeze only the item entities produced by THIS block.
        // Tier controls hover duration only: I = 5s, II = 10s, III = 20s.
        EffectTier staticHover = ForgedEffectRuntime.tier(tool, ForgingEffect.STATIC_HOVER_DROP);
        if (staticHover != null && !event.getDrops().isEmpty()) {
            int duration = switch (staticHover) { case I -> 100; case II -> 200; case III -> 400; };
            if (DoubleTriggerRuntime.rollResult(player, tool)) duration *= 2;
            long hoverUntil = player.level().getGameTime() + duration;
            for (ItemEntity drop : event.getDrops()) {
                drop.setNoGravity(true);
                drop.setDeltaMovement(Vec3.ZERO);
                drop.getPersistentData().putLong("ForgedStaticHoverUntil", hoverUntil);
            }
            ForgedSkillSounds.play(player, ForgingEffect.STATIC_HOVER_DROP);
        }

        // BlockDropsEvent already contains the drops from THIS block. Moving these
        // entities here makes Void Vacuum work on the same mining action instead of
        // waiting until the next block break.
        EffectTier vacuum = ForgedEffectRuntime.tier(tool, ForgingEffect.VOID_VACUUM_PICK);
        if (vacuum != null && !event.getDrops().isEmpty()) {
            for (ItemEntity drop : event.getDrops()) {
                drop.setPos(player.getX(), player.getY() + 0.5D, player.getZ());
                drop.setDeltaMovement(Vec3.ZERO);
            }
            ForgedSkillSounds.play(player, ForgingEffect.VOID_VACUUM_PICK);

            if (tool.isDamageableItem()) {
                int cost = switch (vacuum) { case I -> 5; case II -> 3; case III -> 1; };
                ForgedBlessingRuntime.damage(tool, cost);
            }
        }
    }

    public static void onUltimateBedrockLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        ItemStack tool = player.getMainHandItem();
        if (ForgedEffectRuntime.tier(tool, ForgingEffect.ULTIMATE_LASER_BREAKER) == null) return;

        BlockPos pos = event.getPos();
        if (!player.level().getBlockState(pos).is(Blocks.BEDROCK)) return;

        // Vanilla never reaches BreakEvent for Bedrock in Survival because hardness is -1.
        // Ultimate therefore handles the mining action at left-click, removes the block,
        // and explicitly drops the collectible Bedrock item.
        event.setCanceled(true);
        player.level().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        Block.popResource(player.level(), pos, new ItemStack(Blocks.BEDROCK));

        if (!player.getAbilities().instabuild && tool.isDamageableItem()) {
            ForgedBlessingRuntime.damage(tool, 1);
        }
    }

    public static void onUltimateBedrockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        ItemStack tool = player.getMainHandItem();
        if (ForgedEffectRuntime.tier(tool, ForgingEffect.ULTIMATE_LASER_BREAKER) == null) return;
        if (!event.getState().is(Blocks.BEDROCK)) return;

        // Bedrock normally has destroy speed -1 and no loot table. Ultimate normal mining
        // explicitly turns it into a collectible Bedrock item. The R laser still skips it.
        event.setCanceled(true);
        if (!player.level().isClientSide()) {
            player.level().setBlockAndUpdate(event.getPos(), Blocks.AIR.defaultBlockState());
            Block.popResource(player.level(), event.getPos(), new ItemStack(Blocks.BEDROCK));
            if (!player.getAbilities().instabuild && tool.isDamageableItem()) {
                ForgedBlessingRuntime.damage(tool, 1);
            }
        }
    }

    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide()) return;
        ItemStack tool = player.getMainHandItem();

        // Removing a farmland block also removes its persistent farming-effect metadata.
        if (player.level() instanceof ServerLevel farmingLevel
                && (event.getState().is(Blocks.FARMLAND)
                    || event.getState().is(ExampleMod.MOISTURE_RETAIN_FARMLAND.get()))) {
            ForgedFarmingPlotData.get(farmingLevel).remove(event.getPos());
        }

        // Remember the latest mined block for the active Magnetic Clumping skill.
        player.getPersistentData().putInt("ForgedLastMinedX", event.getPos().getX());
        player.getPersistentData().putInt("ForgedLastMinedY", event.getPos().getY());
        player.getPersistentData().putInt("ForgedLastMinedZ", event.getPos().getZ());

        if (ForgedEffectRuntime.tier(tool, ForgingEffect.AIRBORNE_MINING) != null && !player.onGround()) {
            ForgedSkillSounds.play(player, ForgingEffect.AIRBORNE_MINING);
        }
        if (ForgedEffectRuntime.tier(tool, ForgingEffect.FRENZY_DIGGING) != null) {
            long now = player.level().getGameTime();
            long last = player.getPersistentData().getLong("ForgedFrenzyLastMine");
            int chain = (last > 0L && now - last <= 100L)
                    ? player.getPersistentData().getInt("ForgedFrenzyChain") + 1 : 1;
            player.getPersistentData().putInt("ForgedFrenzyChain", Math.min(chain, 5));
            player.getPersistentData().putLong("ForgedFrenzyLastMine", now);
            if (chain == 5) ForgedSkillSounds.play(player, ForgingEffect.FRENZY_DIGGING);
        }

        // Multi-block mining skills. Generated block breaks are guarded so they do not
        // recursively trigger another forged mining skill.
        if (!player.getPersistentData().getBoolean("ForgedMultiBreakGuard")) {
            Direction face = directionFromLook(player);

            // Rough Cleave, Tunnel Charge, Linear Blast and Wide Excavation are active R skills.
            // Their old automatic-on-break triggers were removed to prevent double activation.

        }

        EffectTier staticHover = ForgedEffectRuntime.tier(tool, ForgingEffect.STATIC_HOVER_DROP);
        if (staticHover != null) {
            int duration = switch (staticHover) { case I -> 100; case II -> 200; case III -> 400; };
            player.getPersistentData().putLong("ForgedStaticHoverUntil", player.level().getGameTime() + duration);
            player.getPersistentData().putLong("ForgedStaticHoverX", event.getPos().getX());
            player.getPersistentData().putLong("ForgedStaticHoverY", event.getPos().getY());
            player.getPersistentData().putLong("ForgedStaticHoverZ", event.getPos().getZ());
        }

        // Bone Dust Extract: each successfully mined block can create one bonus Bone Meal.
        EffectTier boneDust = ForgedEffectRuntime.tier(tool, ForgingEffect.BONE_DUST_EXTRACT);
        if (boneDust != null && player.getRandom().nextDouble() < tierValue(boneDust, BONE_DUST_CHANCE)) {
            int count = DoubleTriggerRuntime.rollResult(player, tool) ? 2 : 1;
            Block.popResource(player.level(), event.getPos(), new ItemStack(Items.BONE_MEAL, count));
            ForgedSkillSounds.play(player, ForgingEffect.BONE_DUST_EXTRACT);
        }

        // Soul Sand Extraction: each successfully mined block can create one bonus Soul Sand.
        EffectTier soulSand = ForgedEffectRuntime.tier(tool, ForgingEffect.SOUL_SAND_EXTRACTION);
        if (soulSand != null && player.getRandom().nextDouble() < tierValue(soulSand, SOUL_SAND_CHANCE)) {
            int count = DoubleTriggerRuntime.rollResult(player, tool) ? 2 : 1;
            Block.popResource(player.level(), event.getPos(), new ItemStack(Items.SOUL_SAND, count));
            ForgedSkillSounds.play(player, ForgingEffect.SOUL_SAND_EXTRACTION);
        }

        EffectTier scavenger = ForgedEffectRuntime.tier(tool, ForgingEffect.SCAVENGER_DIG);
        if (scavenger != null && player.getRandom().nextDouble() < tierValue(scavenger, SCAVENGER_CHANCE)) {
            Item bonus = switch (player.getRandom().nextInt(4)) {
                case 0 -> Items.BONE;
                case 1 -> Items.ROTTEN_FLESH;
                case 2 -> Items.IRON_NUGGET;
                default -> Items.GOLD_NUGGET;
            };
            int count = DoubleTriggerRuntime.rollResult(player, tool) ? 2 : 1;
            Block.popResource(player.level(), event.getPos(), new ItemStack(bonus, count));
            ForgedSkillSounds.play(player, ForgingEffect.SCAVENGER_DIG);
        }

        // Unrefined Ore Discovery: bonus ore comes out as nuggets, not raw ore.
        EffectTier unrefined = ForgedEffectRuntime.tier(tool, ForgingEffect.UNREFINED_ORE_DISCOVERY);
        if (unrefined != null && player.getRandom().nextDouble() < tierValue(unrefined, UNREFINED_ORE_CHANCE)) {
            Item nugget = player.getRandom().nextBoolean() ? Items.IRON_NUGGET : Items.GOLD_NUGGET;
            int count = DoubleTriggerRuntime.rollResult(player, tool) ? 2 : 1;
            Block.popResource(player.level(), event.getPos(), new ItemStack(nugget, count));
            ForgedSkillSounds.play(player, ForgingEffect.UNREFINED_ORE_DISCOVERY);
        }

        // Nature God Bless reward belongs to the plot and only rolls on a mature harvest.
        if (player.level() instanceof ServerLevel natureLevel && isMatureHarvestCrop(event.getState())) {
            EffectTier natureBless = ForgedFarmingPlotData.get(natureLevel)
                    .tier(event.getPos().below(), ForgingEffect.NATURE_GOD_BLESS);
            if (natureBless != null) {
                double rewardChance = switch (natureBless) {
                    case I -> 0.05D;
                    case II -> 0.10D;
                    case III -> 0.20D;
                };
                if (player.getRandom().nextDouble() < rewardChance) {
                    // Passive Golden-Apple reward is intentionally NOT doubled.
                    // Nature God Bless is HYBRID: only its explicit R activation
                    // participates in Double Trigger.
                    ItemStack reward = new ItemStack(player.getRandom().nextDouble() < 0.01D
                            ? Items.ENCHANTED_GOLDEN_APPLE : Items.GOLDEN_APPLE);
                    Block.popResource(player.level(), event.getPos(), reward);
                    ForgedSkillSounds.play(player, ForgingEffect.NATURE_GOD_BLESS);
                }
            }
        }

        // Healing Harvest is read from the farmland directly below the harvested crop.
        // The player does not need to keep holding the forged hoe.
        if (player.level() instanceof ServerLevel farmingLevel && isMatureHarvestCrop(event.getState())) {
            BlockPos farmlandPos = event.getPos().below();
            EffectTier healingHarvest = ForgedFarmingPlotData.get(farmingLevel)
                    .tier(farmlandPos, ForgingEffect.HEALING_HARVEST);
            if (healingHarvest != null
                    && player.getRandom().nextDouble() < tierValue(healingHarvest, HEALING_HARVEST_CHANCE)) {
                int potionCount = ForgedFarmingPlotData.get(farmingLevel)
                        .hasDoubleTrigger(farmlandPos, ForgingEffect.HEALING_HARVEST)
                        && player.getRandom().nextDouble() < DoubleTriggerRuntime.CHANCE ? 2 : 1;
                for (int i = 0; i < potionCount; i++) {
                    Block.popResource(player.level(), event.getPos(),
                            PotionContents.createItemStack(Items.POTION,
                                    net.minecraft.core.registries.BuiltInRegistries.POTION.getHolderOrThrow(
                                            net.minecraft.resources.ResourceKey.create(
                                                    Registries.POTION,
                                                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "healing")))));
                }
                ForgedSkillSounds.play(player, ForgingEffect.HEALING_HARVEST);
            }
        }
    }

    /**
     * Mirrors the important vanilla melee-critical conditions closely enough for
     * the forged trigger: falling, not grounded, not climbing/in water, and not a passenger.
     */
    private static Direction directionFromLook(Player player) {
        Vec3 look = player.getLookAngle();
        double ax = Math.abs(look.x), ay = Math.abs(look.y), az = Math.abs(look.z);
        if (ay >= ax && ay >= az) return look.y > 0 ? Direction.UP : Direction.DOWN;
        if (ax >= az) return look.x > 0 ? Direction.EAST : Direction.WEST;
        return look.z > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    /**
     * Breaks a fixed plane/depth volume without increasing its dimensions by Tier.
     * width/height describe the face plane; depth extends forward from the broken block.
     */
    private static void breakPlane(Player player, BlockPos origin, Direction face, int width, int height, int depth) {
        if (!(player.level() instanceof ServerLevel level)) return;
        java.util.LinkedHashSet<BlockPos> targets = new java.util.LinkedHashSet<>();
        Direction right = (face.getAxis() == Direction.Axis.Y) ? Direction.EAST
                : (face.getAxis() == Direction.Axis.X ? Direction.SOUTH : Direction.EAST);
        Direction up = (face.getAxis() == Direction.Axis.Y) ? Direction.SOUTH : Direction.UP;
        int w0 = -(width / 2), h0 = -(height / 2);

        for (int d = 0; d < depth; d++) {
            BlockPos center = origin.relative(face, d);
            for (int w = 0; w < width; w++) for (int h = 0; h < height; h++)
                targets.add(center.relative(right, w0 + w).relative(up, h0 + h));
        }
        breakTargets(player, level, origin, targets);
    }

    private static void breakLine(Player player, BlockPos origin, Direction face, int length) {
        if (!(player.level() instanceof ServerLevel level)) return;
        java.util.LinkedHashSet<BlockPos> targets = new java.util.LinkedHashSet<>();
        for (int i = 0; i < length; i++) targets.add(origin.relative(face, i));
        breakTargets(player, level, origin, targets);
    }

    private static void breakTargets(Player player, ServerLevel level, BlockPos origin,
                                     java.util.Set<BlockPos> targets) {
        ItemStack tool = player.getMainHandItem();
        int extraBroken = 0;
        player.getPersistentData().putBoolean("ForgedMultiBreakGuard", true);
        try {
            for (BlockPos pos : targets) {
                if (pos.equals(origin)) continue;
                net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
                if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0F) continue;
                if (!tool.isCorrectToolForDrops(state)) continue;
                if (level.destroyBlock(pos, true, player)) extraBroken++;
            }
        } finally {
            player.getPersistentData().putBoolean("ForgedMultiBreakGuard", false);
        }
        // Project rule: extra blocks cost 50% durability, rounded up.
        int extraCost = (extraBroken + 1) / 2;
        if (extraCost > 0 && tool.isDamageableItem())
            ForgedBlessingRuntime.damage(tool, extraCost);
    }
}
