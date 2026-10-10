package com.example.examplemod;

/** Shared round count for both forge minigames and server reward validation. */
public final class ForgingRounds {
    private ForgingRounds() {}

    public static int forDifficulty(float difficulty) {
        int level = Math.max(1, Math.min(3, Math.round(difficulty)));
        return level + 4;
    }
}
