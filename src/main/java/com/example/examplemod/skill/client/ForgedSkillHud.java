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
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public final class ForgedSkillHud {
    private static int serverSelectedIndex;
    private static final Map<String, Long> serverReadyAt = new HashMap<>();
    private static long serverSlamUntil;
    private static boolean serverAegisActive;

    private ForgedSkillHud() {}

    public static void updateServerState(int selectedIndex, String cooldownStates, long ignoredReadyAt,
                                         long slamUntil, boolean aegisActive) {
        serverSelectedIndex = Math.max(0, selectedIndex);
        serverReadyAt.clear();
        if (cooldownStates != null && !cooldownStates.isEmpty()) {
            for (String entry : cooldownStates.split(";")) {
                int split = entry.indexOf('=');
                if (split <= 0) continue;
                try {
                    serverReadyAt.put(entry.substring(0, split),
                            Math.max(0L, Long.parseLong(entry.substring(split + 1))));
                } catch (NumberFormatException ignored) {}
            }
        }
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
        GuiGraphics gui = event.getGuiGraphics();

        // Show every active skill on this equipment. The selected skill is highlighted.
        final float scale = 0.75F;
        final int padding = 3;
        final int line = mc.font.lineHeight + 1;
        String controls = active.size() > 1 ? "[R] Use   [Shift+R] Change" : "[R] Use";

        List<String> rows = new java.util.ArrayList<>();
        List<Integer> colors = new java.util.ArrayList<>();
        long now = mc.player.level().getGameTime();

        for (int i = 0; i < active.size(); i++) {
            ForgingEffect skill = active.get(i);
            String status;
            if (skill == ForgingEffect.GRAVATIONAL_SLAM && serverSlamUntil > now) {
                double seconds = (serverSlamUntil - now) / 20.0D;
                status = String.format(Locale.ROOT, "Charging %.1fs", seconds);
            } else if (skill == ForgingEffect.AEGIS_SHIELD && serverAegisActive) {
                status = "ACTIVE";
            } else {
                long remaining = Math.max(0L, serverReadyAt.getOrDefault(skill.name(), 0L) - now);
                status = remaining <= 0L ? "READY" : "CD " + formatTime(remaining);
            }

            String marker = i == selected ? "> " : "  ";
            String shortName = skill == ForgingEffect.DIVINE_BEACON_LIGHT ? "Divine Beacon" : skill.displayName();
            rows.add(marker + shortName + "   " + status);
            colors.add(i == selected ? 0xFFFFFF
                    : (status.equals("READY") || status.equals("ACTIVE") ? 0xB8FFB8 : 0xFFD27F));
        }

        String header = "Active Skill";
        int contentWidth = Math.max(mc.font.width(header), mc.font.width(controls));
        for (String row : rows) contentWidth = Math.max(contentWidth, mc.font.width(row));
        int boxWidth = contentWidth + padding * 2;
        int boxHeight = (rows.size() + 2) * line + padding * 2;

        int scaledScreenWidth = (int)(gui.guiWidth() / scale);
        int scaledScreenHeight = (int)(gui.guiHeight() / scale);
        int x = scaledScreenWidth - boxWidth - 4;
        int y = scaledScreenHeight - boxHeight - 4;

        gui.pose().pushPose();
        gui.pose().scale(scale, scale, 1.0F);
        gui.fill(x, y, x + boxWidth, y + boxHeight, 0x70000000);
        gui.drawString(mc.font, Component.literal(header),
                x + padding, y + padding, 0xFFFFFF, true);
        gui.drawString(mc.font, Component.literal(controls),
                x + padding, y + padding + line, 0xAAAAAA, true);
        for (int i = 0; i < rows.size(); i++) {
            gui.drawString(mc.font, Component.literal(rows.get(i)),
                    x + padding, y + padding + line * (i + 2), colors.get(i), true);
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
