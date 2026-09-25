package com.example.examplemod;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ForgedEffectKeybinds {
    private static int timeStopShakeTicks;
    private static int timeStopShakeTotal;

    private ForgedEffectKeybinds() {}

    public static void startTimeStopShake(int ticks) {
        timeStopShakeTicks = Math.max(0, ticks);
        timeStopShakeTotal = Math.max(1, ticks);
    }

    public static final Lazy<KeyMapping> USE_SKILL = Lazy.of(() -> new KeyMapping(
            "key.examplemod.use_skill",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.examplemod"
    ));

    public static final Lazy<KeyMapping> CYCLE_SKILL = Lazy.of(() -> new KeyMapping(
            "key.examplemod.cycle_skill",
            KeyConflictContext.IN_GAME,
            KeyModifier.SHIFT,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.examplemod"
    ));

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(USE_SKILL.get());
        event.register(CYCLE_SKILL.get());
    }

    @EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
    public static final class ClientEvents {
        private ClientEvents() {}

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            if (timeStopShakeTicks > 0) {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                if (mc.player != null && mc.screen == null) {
                    // Time Stop is an instant impact: strong shake that fades quickly.
                    double remaining = (double) timeStopShakeTicks / timeStopShakeTotal;
                    float strength = (float)(1.15D * remaining);
                    float phase = timeStopShakeTicks * 2.35F;
                    mc.player.setYRot(mc.player.getYRot() + (float)Math.sin(phase) * strength);
                    mc.player.setXRot(mc.player.getXRot() + (float)Math.cos(phase * 1.47F) * strength * 0.65F);
                }
                timeStopShakeTicks--;
            }

            while (CYCLE_SKILL.get().consumeClick()) {
                ForgedEffectNetwork.sendAction(0);
            }

            while (USE_SKILL.get().consumeClick()) {
                ForgedEffectNetwork.sendAction(1);
            }
        }
    }
}
