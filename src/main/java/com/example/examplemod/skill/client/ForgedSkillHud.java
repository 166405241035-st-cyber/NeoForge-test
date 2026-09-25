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

        // Compact HUD: render at 75% scale and keep it tight to the bottom-right corner.
        final float scale = 0.75F;
        final int padding = 3;
        final int line = mc.font.lineHeight + 1;
        String cycle = active.size() > 1 ? "[Shift+R] Change" : "";

        int contentWidth = Math.max(mc.font.width(title), Math.max(mc.font.width(status), mc.font.width(cycle)));
        int rows = active.size() > 1 ? 3 : 2;
        int boxWidth = contentWidth + padding * 2;
        int boxHeight = rows * line + padding * 2;

        int scaledScreenWidth = (int)(gui.guiWidth() / scale);
        int scaledScreenHeight = (int)(gui.guiHeight() / scale);
        int x = scaledScreenWidth - boxWidth - 4;
        int y = scaledScreenHeight - boxHeight - 4;

        gui.pose().pushPose();
        gui.pose().scale(scale, scale, 1.0F);
        gui.fill(x, y, x + boxWidth, y + boxHeight, 0x70000000);
        gui.drawString(mc.font, Component.literal(title), x + padding, y + padding, 0xFFFFFF, true);
        gui.drawString(mc.font, Component.literal(status), x + padding, y + padding + line,
                status.equals("READY") || status.equals("ACTIVE") ? 0x55FF55 : 0xFFCC55, true);
        if (active.size() > 1) {
            gui.drawString(mc.font, Component.literal(cycle), x + padding, y + padding + line * 2, 0xAAAAAA, true);
        }
        gui.pose().popPose();
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
