package com.example.examplemod.guide;

import com.example.examplemod.skill.ForgingEffect;
import com.example.examplemod.skill.blessing.ForgedBlessing;
import com.example.examplemod.skill.curse.ForgedCurse;

/** Snapshot sent by the server when the player opens the guide. */
public final class ForgingGuideClientState {
    private static int[] tiers = new int[ForgingEffect.values().length];
    private static int blessings;
    private static int curses;
    private ForgingGuideClientState() {}

    public static void clear() {
        tiers = new int[ForgingEffect.values().length];
        blessings = curses = 0;
    }

    public static void update(String value) {
        try {
            String[] parts = value.split(";", -1);
            String[] values = parts[0].split(",");
            if (parts.length != 3 || values.length != ForgingEffect.values().length) return;
            int[] parsed = new int[values.length];
            for (int i = 0; i < values.length; i++) parsed[i] = Integer.parseInt(values[i]) & 7;
            int parsedBlessings = Integer.parseInt(parts[1]);
            int parsedCurses = Integer.parseInt(parts[2]);
            tiers = parsed;
            blessings = parsedBlessings;
            curses = parsedCurses;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
            // Ignore malformed snapshots rather than reveal or discard discoveries.
        }
    }

    public static int tier(ForgingEffect effect) {
        int mask = tiers[effect.ordinal()];
        return mask >= 4 ? 3 : mask >= 2 ? 2 : mask == 1 ? 1 : 0;
    }
    public static boolean hasTier(ForgingEffect effect, int tier) { return (tiers[effect.ordinal()] & 1 << (tier - 1)) != 0; }
    public static boolean has(ForgedBlessing blessing) { return (blessings & 1 << blessing.ordinal()) != 0; }
    public static boolean has(ForgedCurse curse) { return (curses & 1 << curse.ordinal()) != 0; }
    public static int foundSkills() { int n = 0; for (int tier : tiers) if (tier > 0) n++; return n; }
    public static int foundBlessings() { return Integer.bitCount(blessings); }
    public static int foundCurses() { return Integer.bitCount(curses); }
}
