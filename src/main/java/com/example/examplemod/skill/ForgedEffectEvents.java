package com.example.examplemod.skill;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.skill.combat.CombatEffectHandlers;
import com.example.examplemod.skill.farming.FarmingEffectHandlers;
import com.example.examplemod.skill.mining.MiningEffectHandlers;
import com.example.examplemod.skill.passive.PlayerEffectTicker;
import com.example.examplemod.skill.storage.ForgedStorageSkills;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/** Central entry point delegating forged equipment behavior to focused skill modules. */
@EventBusSubscriber(modid = ExampleMod.MODID)
public final class ForgedEffectEvents {
    private ForgedEffectEvents() {}

    @SubscribeEvent
    public static void onLivingAttack(LivingIncomingDamageEvent event) {
        CombatEffectHandlers.onLivingAttack(event);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        PlayerEffectTicker.onPlayerTick(event);
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        MiningEffectHandlers.onBreakSpeed(event);
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        MiningEffectHandlers.onBlockDrops(event);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        ForgedStorageSkills.onPlayerClone(event);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        CombatEffectHandlers.onLivingDeath(event);
    }

    @SubscribeEvent
    public static void onUltimateBedrockLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        MiningEffectHandlers.onUltimateBedrockLeftClick(event);
    }

    @SubscribeEvent
    public static void onUltimateBedrockBreak(BlockEvent.BreakEvent event) {
        MiningEffectHandlers.onUltimateBedrockBreak(event);
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        MiningEffectHandlers.onBlockBreak(event);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        FarmingEffectHandlers.onRightClickBlock(event);
    }

    @SubscribeEvent
    public static void onNatureGodBlessHeldAura(PlayerTickEvent.Post event) {
        FarmingEffectHandlers.onNatureGodBlessHeldAura(event);
    }

    @SubscribeEvent
    public static void onHyperGrowthPlotParticles(LevelTickEvent.Post event) {
        FarmingEffectHandlers.onHyperGrowthPlotParticles(event);
    }

    @SubscribeEvent
    public static void onNatureGodGrowth(CropGrowEvent.Pre event) {
        FarmingEffectHandlers.onNatureGodGrowth(event);
    }

    @SubscribeEvent
    public static void onHyperGrowth(CropGrowEvent.Pre event) {
        FarmingEffectHandlers.onHyperGrowth(event);
    }

    @SubscribeEvent
    public static void onFarmlandTrample(net.neoforged.neoforge.event.level.BlockEvent.FarmlandTrampleEvent event) {
        FarmingEffectHandlers.onFarmlandTrample(event);
    }

    @SubscribeEvent
    public static void onEntityPlace(EntityPlaceEvent event) {
        FarmingEffectHandlers.onEntityPlace(event);
    }
}
