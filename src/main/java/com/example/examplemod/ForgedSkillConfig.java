package com.example.examplemod;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Configurable cooldowns for forged active skills. Values are seconds. */
public final class ForgedSkillConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<Double> FIREBALL_I, FIREBALL_II, FIREBALL_III;
    public static final ModConfigSpec.ConfigValue<Double> DASH_I, DASH_II, DASH_III;
    public static final ModConfigSpec.ConfigValue<Double> WITHER_I, WITHER_II, WITHER_III;
    public static final ModConfigSpec.ConfigValue<Double> HARPOON_I, HARPOON_II, HARPOON_III;
    public static final ModConfigSpec.ConfigValue<Double> SWAP_I, SWAP_II, SWAP_III;
    public static final ModConfigSpec.ConfigValue<Double> AIR_SLASH_I, AIR_SLASH_II, AIR_SLASH_III;
    public static final ModConfigSpec.ConfigValue<Double> LAVA_I, LAVA_II, LAVA_III;
    public static final ModConfigSpec.ConfigValue<Double> TIME_STOP_I, TIME_STOP_II, TIME_STOP_III;
    public static final ModConfigSpec.ConfigValue<Double> FORTRESS_I, FORTRESS_II, FORTRESS_III;
    public static final ModConfigSpec.ConfigValue<Double> LASER_I, LASER_II, LASER_III;
    public static final ModConfigSpec.ConfigValue<Double> NATURE_I, NATURE_II, NATURE_III;
    public static final ModConfigSpec.ConfigValue<Double> BOOMERANG_I, BOOMERANG_II, BOOMERANG_III;
    public static final ModConfigSpec.ConfigValue<Double> DIVINE_I, DIVINE_II, DIVINE_III;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.comment("Active skill cooldowns in seconds. Each tier can be tuned separately.")
         .push("active_skill_cooldowns");

        FIREBALL_I = value(b,"fireball_shoot","tier_i",4.0); FIREBALL_II=value(b,"fireball_shoot","tier_ii",2.5); FIREBALL_III=value(b,"fireball_shoot","tier_iii",1.5);
        DASH_I=value(b,"front_dash","tier_i",3.5); DASH_II=value(b,"front_dash","tier_ii",2.25); DASH_III=value(b,"front_dash","tier_iii",1.5);
        WITHER_I=value(b,"wither_curse_power","tier_i",10.0); WITHER_II=value(b,"wither_curse_power","tier_ii",10.0); WITHER_III=value(b,"wither_curse_power","tier_iii",10.0);
        HARPOON_I=value(b,"harpoon_pull","tier_i",3.0); HARPOON_II=value(b,"harpoon_pull","tier_ii",2.0); HARPOON_III=value(b,"harpoon_pull","tier_iii",1.25);
        SWAP_I=value(b,"mob_swap","tier_i",6.0); SWAP_II=value(b,"mob_swap","tier_ii",4.0); SWAP_III=value(b,"mob_swap","tier_iii",2.5);
        AIR_SLASH_I=value(b,"air_slash_rupture","tier_i",5.0); AIR_SLASH_II=value(b,"air_slash_rupture","tier_ii",3.5); AIR_SLASH_III=value(b,"air_slash_rupture","tier_iii",2.0);
        LAVA_I=value(b,"lava_wave","tier_i",7.5); LAVA_II=value(b,"lava_wave","tier_ii",5.0); LAVA_III=value(b,"lava_wave","tier_iii",3.5);
        TIME_STOP_I=value(b,"stun_time_stop","tier_i",7.5); TIME_STOP_II=value(b,"stun_time_stop","tier_ii",7.5); TIME_STOP_III=value(b,"stun_time_stop","tier_iii",7.5);
        FORTRESS_I=value(b,"iron_fortress_guard","tier_i",7.5); FORTRESS_II=value(b,"iron_fortress_guard","tier_ii",7.5); FORTRESS_III=value(b,"iron_fortress_guard","tier_iii",7.5);
        LASER_I=value(b,"ultimate_laser_breaker","tier_i",12.5); LASER_II=value(b,"ultimate_laser_breaker","tier_ii",8.5); LASER_III=value(b,"ultimate_laser_breaker","tier_iii",5.0);
        NATURE_I=value(b,"nature_god_bless","tier_i",10.0); NATURE_II=value(b,"nature_god_bless","tier_ii",10.0); NATURE_III=value(b,"nature_god_bless","tier_iii",10.0);
        BOOMERANG_I=value(b,"boomerang_weapon","tier_i",6.0); BOOMERANG_II=value(b,"boomerang_weapon","tier_ii",6.0); BOOMERANG_III=value(b,"boomerang_weapon","tier_iii",6.0);
        DIVINE_I=value(b,"divine_beacon_light","tier_i",7.5); DIVINE_II=value(b,"divine_beacon_light","tier_ii",5.0); DIVINE_III=value(b,"divine_beacon_light","tier_iii",3.0);

        b.pop();
        SPEC = b.build();
    }

    private static ModConfigSpec.ConfigValue<Double> value(ModConfigSpec.Builder b, String skill, String tier, double def) {
        return b.defineInRange(skill + "." + tier, def, 0.0, 300.0);
    }

    private ForgedSkillConfig() {}

    private static long ticks(EffectTier tier, ModConfigSpec.ConfigValue<Double> i,
                              ModConfigSpec.ConfigValue<Double> ii, ModConfigSpec.ConfigValue<Double> iii) {
        double seconds = switch (tier) { case I -> i.get(); case II -> ii.get(); case III -> iii.get(); };
        return Math.max(0L, Math.round(seconds * 20.0D));
    }

    public static long fireball(EffectTier t){return ticks(t,FIREBALL_I,FIREBALL_II,FIREBALL_III);}
    public static long dash(EffectTier t){return ticks(t,DASH_I,DASH_II,DASH_III);}
    public static long wither(EffectTier t){return ticks(t,WITHER_I,WITHER_II,WITHER_III);}
    public static long harpoon(EffectTier t){return ticks(t,HARPOON_I,HARPOON_II,HARPOON_III);}
    public static long swap(EffectTier t){return ticks(t,SWAP_I,SWAP_II,SWAP_III);}
    public static long airSlash(EffectTier t){return ticks(t,AIR_SLASH_I,AIR_SLASH_II,AIR_SLASH_III);}
    public static long lava(EffectTier t){return ticks(t,LAVA_I,LAVA_II,LAVA_III);}
    public static long timeStop(EffectTier t){return ticks(t,TIME_STOP_I,TIME_STOP_II,TIME_STOP_III);}
    public static long fortress(EffectTier t){return ticks(t,FORTRESS_I,FORTRESS_II,FORTRESS_III);}
    public static long laser(EffectTier t){return ticks(t,LASER_I,LASER_II,LASER_III);}
    public static long nature(EffectTier t){return ticks(t,NATURE_I,NATURE_II,NATURE_III);}
    public static long boomerang(EffectTier t){return ticks(t,BOOMERANG_I,BOOMERANG_II,BOOMERANG_III);}
    public static long divine(EffectTier t){return ticks(t,DIVINE_I,DIVINE_II,DIVINE_III);}
}
