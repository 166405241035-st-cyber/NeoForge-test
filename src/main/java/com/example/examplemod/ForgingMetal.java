package com.example.examplemod;

/**
 * Metal tier used by the forging system.
 *
 * Current rule:
 * - metal controls base equipment stats (damage/durability) later
 * - metal also controls minigame difficulty now
 *
 * Difficulty is intentionally temporary and easy to rebalance.
 */
public enum ForgingMetal {
    GOLD("Gold", 1),
    IRON("Iron", 1),
    DIAMOND("Diamond", 2),
    NETHERITE("Netherite", 3);

    private final String displayName;
    private final int difficulty;

    ForgingMetal(String displayName, int difficulty) {
        this.displayName = displayName;
        this.difficulty = difficulty;
    }

    public String displayName() {
        return displayName;
    }

    /**
     * Temporary difficulty scale: 1 = easy, 2 = medium, 3 = hard.
     */
    public int difficulty() {
        return difficulty;
    }
}
