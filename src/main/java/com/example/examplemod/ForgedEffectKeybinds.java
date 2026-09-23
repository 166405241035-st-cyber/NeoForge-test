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
    private ForgedEffectKeybinds() {}

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
            while (CYCLE_SKILL.get().consumeClick()) {
                ForgedEffectNetwork.sendAction(0);
            }

            while (USE_SKILL.get().consumeClick()) {
                ForgedEffectNetwork.sendAction(1);
            }
        }
    }
}
