package com.example.examplemod;

import com.example.examplemod.block.*;
import com.example.examplemod.entity.*;
import com.example.examplemod.item.*;
import com.example.examplemod.skill.*;
import com.example.examplemod.skill.client.*;

/**
 * Client hook placeholder kept for compatibility.
 * The Forging Anvil now opens through AnvilMenu/MenuScreens, so it must not
 * construct ForgingAnvilScreen directly.
 */
public final class ForgingClientHooks {
    private ForgingClientHooks() {
    }

    public static void openAnvilScreen() {
        // No-op: the server opens AnvilMenu and NeoForge creates the screen.
    }
}
