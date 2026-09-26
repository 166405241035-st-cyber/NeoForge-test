package com.example.examplemod;

import com.example.examplemod.block.*;
import com.example.examplemod.entity.*;
import com.example.examplemod.item.*;
import com.example.examplemod.skill.*;
import com.example.examplemod.skill.client.*;

public record ForgingResult(
        int score,
        double accuracy,
        int maxCombo,
        int perfectCount,
        int greatCount,
        int goodCount,
        int missCount) {
}
