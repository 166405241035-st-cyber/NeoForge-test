package com.example.examplemod.forging.session;

import com.example.examplemod.forging.blueprint.ForgingBlueprintType;
import com.example.examplemod.forging.material.ForgingMetal;
import com.example.examplemod.forging.material.MonsterMaterial;
import com.example.examplemod.forging.result.ForgedCoreResult;
import com.example.examplemod.forging.result.ForgedHeadResult;
import com.example.examplemod.forging.result.ForgedRodResult;
import com.example.examplemod.item.ForgedCoreItem;
import com.example.examplemod.item.ForgedHeadItem;
import com.example.examplemod.item.ForgedRodItem;
import com.example.examplemod.skill.EffectPool;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** One forge output per recipe accepted by the server. A client cannot request a reward without paying for a forge attempt. */
public final class ForgeRewardSession {
    private static final long TIMEOUT_MS = 10 * 60 * 1000L;
    private static final Map<UUID, Pending> PENDING = new ConcurrentHashMap<>();

    private record Pending(ForgingBlueprintType blueprint, ForgingMetal metal,
                           MonsterMaterial material, long createdAt) {}

    private ForgeRewardSession() {}

    public static void begin(Player player, ForgingBlueprintType blueprint,
                             ForgingMetal metal, MonsterMaterial material) {
        long now = System.currentTimeMillis();
        PENDING.entrySet().removeIf(entry -> now - entry.getValue().createdAt() > TIMEOUT_MS);
        PENDING.put(player.getUUID(), new Pending(blueprint, metal, material, now));
    }

    public static boolean accept(Player player, ItemStack stack) {
        Pending pending = PENDING.get(player.getUUID());
        if (pending == null || System.currentTimeMillis() - pending.createdAt() > TIMEOUT_MS) {
            PENDING.remove(player.getUUID());
            return false;
        }
        if (!matches(pending, stack)) return false;
        return PENDING.remove(player.getUUID(), pending);
    }

    private static boolean matches(Pending pending, ItemStack stack) {
        if (pending.blueprint().isHead()) {
            ForgedHeadResult result = ForgedHeadItem.readResult(stack);
            return result != null && result.metal() == pending.metal()
                    && result.blueprint() == pending.blueprint().headType()
                    && result.monsterMaterial() == pending.material()
                    && EffectPool.getAllowedEffects(result.monsterMaterial(), result.blueprint())
                            .contains(result.effect());
        }
        if (pending.blueprint() == ForgingBlueprintType.CORE) {
            ForgedCoreResult result = ForgedCoreItem.readResult(stack);
            return result != null && result.metal() == pending.metal()
                    && result.monsterMaterial() == pending.material();
        }
        ForgedRodResult result = ForgedRodItem.readResult(stack);
        return result != null && result.metal() == pending.metal()
                && result.monsterMaterial() == pending.material();
    }
}
