package com.example.examplemod.skill.curse;

/** Curse identities awarded by the Rhythm Forging minigame. */
public enum ForgedCurse {
    TALKATIVE_BLADE("Talkative Blade"),
    BERSERKER("Berserker"),
    GAMBLERS_STRIKE("Gambler's Strike"),
    POWER_ERASURE("Power Erasure"),
    LIFE_EXCHANGE("Life Exchange"),
    VAMPIRE_BLADE("Vampire Blade"),
    LAST_STAND("Last Stand"),
    CRITICAL_FAILURE("Critical Failure");

    private final String displayName;

    ForgedCurse(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
