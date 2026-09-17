package com.example.examplemod;

public record ForgingResult(
        int score,
        double accuracy,
        int maxCombo,
        int perfectCount,
        int greatCount,
        int goodCount,
        int missCount) {
}
