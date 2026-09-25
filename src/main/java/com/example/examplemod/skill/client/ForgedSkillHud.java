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
    private static int serverSelectedIndex;
    private static String serverCooldownKey = "";
    private static long serverReadyAt;
    private static long serverSlamUntil;
    private static boolean serverAegisActive;

    private ForgedSkillHud() {}

    public static void updateServerState(int selectedIndex, String cooldownKey, long readyAt,
                                         long slamUntil, boolean aegisActive) {
        serverSelectedIndex = Math.max(0, selectedIndex);
        serverCooldownKey = cooldownKey == null ? "" : cooldownKey;
        serverReadyAt = Math.max(0L, readyAt);
        serverSlamUntil = Math.max(0L, slamUntil);
        serverAegisActive = aegisActive;
    }

    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        ItemStack tool = mc.player.getMainHandItem();
        List<ForgingEffect> active = ForgedActiveSkills.getActiveEffects(tool);
        if (active.isEmpty()) return;

        int selected = Math.min(serverSelectedIndex, active.size() - 1);
        ForgingEffect skill = active.get(selected);

        // Show the physical key name (R), not the localized typed character (e.g. Thai พ).
        String useKey = "R";
        String cycleKey = "Shift + R";
        String title = "[" + useKey + "] " + skill.displayName();

        String status;
        long slamUntil = serverSlamUntil;
        if (skill == ForgingEffect.GRAVATIONAL_SLAM && slamUntil > mc.player.level().getGameTime()) {
            double seconds = (slamUntil - mc.player.level().getGameTime()) / 20.0D;
            status = String.format(Locale.ROOT, "Charging: %.1fs", seconds);
        } else if (skill == ForgingEffect.AEGIS_SHIELD) {
            status = serverAegisActive ? "ACTIVE" : "READY";
        } else {
            String expectedKey = ForgedActiveSkills.cooldownKey(skill);
            long remaining = expectedKey != null && expectedKey.equals(serverCooldownKey)
                    ? Math.max(0L, serverReadyAt - mc.player.level().getGameTime()) : 0L;
            status = remaining <= 0L ? "READY" : "CD " + formatTime(remaining);
        }

        GuiGraphics gui = event.getGuiGraphics();
        int margin = 8;
        int line = mc.font.lineHeight + 2;
        String cycle = active.size() > 1 ? "[" + cycleKey + "] Change" : "";
        int width = Math.max(mc.font.width(title), Math.max(mc.font.width(status), mc.font.width(cycle))) + 6;
        int x = gui.guiWidth() - width - margin;
        int y = gui.guiHeight() - (active.size() > 1 ? 46 : 34);

        gui.fill(x - 2, y - 2, x + width, y + line * (active.size() > 1 ? 3 : 2), 0x78000000);
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
