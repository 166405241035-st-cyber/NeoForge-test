package com.example.examplemod.skill.client;

import com.example.examplemod.*;
import com.example.examplemod.block.*;
import com.example.examplemod.entity.*;
import com.example.examplemod.item.*;
import com.example.examplemod.skill.*;
import com.example.examplemod.skill.client.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.List;
import java.util.Locale;

@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public final class ForgedSkillHud {
    private ForgedSkillHud() {}

    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        ItemStack tool = mc.player.getMainHandItem();
        List<ForgingEffect> active = ForgedActiveSkills.getActiveEffects(tool);
        if (active.isEmpty()) return;

        int selected = ForgedActiveSkills.normalizeSelected(mc.player, active.size());
        ForgingEffect skill = active.get(selected);

        String useKey = keyName(ForgedEffectKeybinds.USE_SKILL.get());
        String cycleKey = keyName(ForgedEffectKeybinds.CYCLE_SKILL.get());
        String title = "[" + useKey + "] " + skill.displayName();

        String status;
        long slamUntil = mc.player.getPersistentData().getLong("ForgedGravitationalSlamUntil");
        if (skill == ForgingEffect.GRAVATIONAL_SLAM && slamUntil > mc.player.level().getGameTime()) {
            double seconds = (slamUntil - mc.player.level().getGameTime()) / 20.0D;
            status = String.format(Locale.ROOT, "Charging: %.1fs", seconds);
        } else if (skill == ForgingEffect.AEGIS_SHIELD) {
            status = mc.player.getPersistentData().getBoolean("ForgedAegisActive") ? "ACTIVE" : "READY";
        } else {
            long remaining = ForgedActiveSkills.cooldownRemaining(mc.player, skill);
            status = remaining <= 0L ? "READY" : "Cooldown: " + formatTime(remaining);
        }

        GuiGraphics gui = event.getGuiGraphics();
        int margin = 8;
        int line = mc.font.lineHeight + 2;
        String cycle = active.size() > 1 ? "[" + cycleKey + "] Change Skill" : "";
        int width = Math.max(mc.font.width(title), Math.max(mc.font.width(status), mc.font.width(cycle))) + 12;
        int x = gui.guiWidth() - width - margin;
        int y = gui.guiHeight() - 58;

        gui.fill(x - 4, y - 4, x + width, y + line * (active.size() > 1 ? 3 : 2) + 2, 0x90000000);
        gui.drawString(mc.font, Component.literal(title), x, y, 0xFFFFFF, true);
        gui.drawString(mc.font, Component.literal(status), x, y + line,
                status.equals("READY") || status.equals("ACTIVE") ? 0x55FF55 : 0xFFCC55, true);
        if (active.size() > 1) {
            gui.drawString(mc.font, Component.literal(cycle), x, y + line * 2, 0xAAAAAA, true);
        }
    }

    private static String keyName(KeyMapping mapping) {
        return mapping.getTranslatedKeyMessage().getString();
    }

    private static String formatTime(long ticks) {
        long totalSeconds = (ticks + 19L) / 20L;
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        return minutes > 0L ? String.format(Locale.ROOT, "%d:%02d", minutes, seconds) : totalSeconds + "s";
    }
}
