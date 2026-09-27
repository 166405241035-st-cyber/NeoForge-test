package com.example.examplemod.skill.blessing;

/** Blessing identities awarded by the Rhythm Forging minigame. */
public enum ForgedBlessing {
    DOUBLE_TRIGGER("Double Trigger"),
    POWER_STRIKE("Power Strike"),
    HUNTERS_FORTUNE("Hunter's Fortune"),
    LIFE_STEAL("Life Steal"),
    DURABILITY_GUARD("Durability Guard"),
    MINERS_FORTUNE("Miner's Fortune"),
    VEIN_BREAKER("Vein Breaker"),
    MINING_HASTE("Mining Haste"),
    EXPERIENCE_BOOST("Experience Boost"),
    DIVINE_EXECUTION("Divine Execution");

    private final String displayName;
    ForgedBlessing(String displayName) { this.displayName = displayName; }
    public String displayName() { return displayName; }
}
