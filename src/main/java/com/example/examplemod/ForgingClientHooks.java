package com.example.examplemod;

import net.minecraft.client.Minecraft;

public final class ForgingClientHooks {
    private ForgingClientHooks() {
    }

    public static void openForgingScreen() {
        Minecraft.getInstance().setScreen(new ForgingScreen());
    }

    public static void openAnvilScreen() {
        Minecraft.getInstance().setScreen(new ForgingAnvilScreen());
    }
}
