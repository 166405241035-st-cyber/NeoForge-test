package com.example.examplemod.skill;

import com.example.examplemod.*;
import com.example.examplemod.block.*;
import com.example.examplemod.entity.*;
import com.example.examplemod.item.*;
import com.example.examplemod.skill.*;
import com.example.examplemod.skill.client.*;

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
