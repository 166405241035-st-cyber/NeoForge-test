package com.example.examplemod;

public enum EffectTier {
    I(1),
    II(2),
    III(3);

    private final int level;

    EffectTier(int level) {
        this.level = level;
    }

    public int level() {
        return level;
    }
}
