package com.example.examplemod.guide;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.skill.ForgingEffect;
import com.example.examplemod.skill.blessing.ForgedBlessing;
import com.example.examplemod.skill.curse.ForgedCurse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

/** Twelve-page interactive guide. Pages 10-12 show only the player's discovered entries. */
public final class ForgingGuideScreen extends Screen {
    private static final int W = 380, H = 266, INK = 0xFF241F1A, MUTED = 0xFF534635;
    private static final int GOLD = 0xFF995E1C, GREEN = 0xFF298150, RED = 0xFF9A4336;
    private static final float TITLE_SCALE = 1.5F, TEXT_SCALE = 1.25F, ICON_SCALE = 1.65F;
    private static final Item[] MATERIALS = {Items.ROTTEN_FLESH, Items.BONE, Items.STRING, Items.GUNPOWDER,
            Items.SLIME_BALL, Items.ENDER_PEARL, Items.BLAZE_ROD, Items.GHAST_TEAR,
            Items.WITHER_SKELETON_SKULL, Items.PHANTOM_MEMBRANE, Items.DRAGON_BREATH,
            Items.SHULKER_SHELL, Items.NETHER_STAR};
    private static final String[] MATERIAL_NAMES = {"Rotten Flesh", "Bone", "String", "Gunpowder", "Slime Ball",
            "Ender Pearl", "Blaze Rod", "Ghast Tear", "Wither Skull", "Phantom Mem.",
            "Dragon Breath", "Shulker Shell", "Nether Star"};
    private static final String[] BLUEPRINT_NAMES = {"Sword", "Axe", "Pickaxe", "Shovel", "Hoe", "Core", "Rod"};
    private static final Item[] BLUEPRINT_BASES = {Items.WOODEN_SWORD, Items.WOODEN_AXE, Items.WOODEN_PICKAXE,
            Items.WOODEN_SHOVEL, Items.WOODEN_HOE, null, Items.STICK};
    private int page = 1, selected = -1, listOffset, blueprint, blockRecipe;
    private int left, top;
    private float scale = 1.0F;

    private ForgingGuideScreen() { super(Component.literal("Forging Guide")); }

    public static void open() {
        ForgingGuideClientState.clear();
        Minecraft.getInstance().setScreen(new ForgingGuideScreen());
        com.example.examplemod.skill.ForgedEffectNetwork.requestGuide();
    }

    @Override protected void init() {
        scale = Math.min(1.0F, Math.min((width - 12.0F) / W, (height - 12.0F) / H));
        left = (width - Math.round(W * scale)) / 2;
        top = (height - Math.round(H * scale)) / 2;
    }

    @Override public void renderBackground(GuiGraphics g, int mx, int my, float partial) {
        g.fill(0, 0, width, height, 0x7714181B);
    }

    @Override public void render(GuiGraphics g, int mx, int my, float partial) {
        renderBackground(g, mx, my, partial);
        g.pose().pushPose();
        g.pose().translate(left * (1.0F - scale), top * (1.0F - scale), 0);
        g.pose().scale(scale, scale, 1);
        g.fill(left, top, left + W, top + H, 0xFF6D5133);
        g.fill(left + 4, top + 4, left + W - 4, top + H - 4, 0xFFFFF8E9);
        g.fill(left + 9, top + 9, left + W - 9, top + H - 9, 0xFFF9EED6);
        g.fill(left + 17, top + 17, left + 42, top + 39, 0xFF4B3828);
        g.drawCenteredString(font, Integer.toString(page), left + 29, top + 24, 0xFFEEE0C2);
        line(g, 16, 48, W - 16, 50, GOLD);
        switch (page) {
            case 1 -> cover(g);
            case 2 -> overview(g);
            case 3 -> blockRecipes(g);
            case 4 -> blueprintRecipes(g);
            case 5 -> materials(g);
            case 6 -> forge(g);
            case 7 -> timing(g);
            case 8 -> anvil(g);
            case 9 -> journal(g);
            case 10 -> discoveries(g, 10);
            case 11 -> discoveries(g, 11);
            case 12 -> discoveries(g, 12);
            default -> {}
        }
        button(g, 17, H - 20, 45, 19, "<", page > 1);
        button(g, W - 62, H - 20, 45, 19, ">", page < 12);
        centered(g, W / 2, H - 17, page + " / 12", MUTED, TEXT_SCALE);
        g.pose().popPose();
        // No widgets are registered. Screen.render would paint another dim background over the book.
    }

    private void drawScaled(GuiGraphics g, String text, int x, int y, int color, float size) {
        g.pose().pushPose();
        g.pose().translate(left + x, top + y, 0);
        g.pose().scale(size, size, 1.0F);
        g.drawString(font, text, 0, 0, color, false);
        g.pose().popPose();
    }
    private void centered(GuiGraphics g, int x, int y, String text, int color, float size) {
        drawScaled(g, text, x - Math.round(font.width(text) * size / 2.0F), y, color, size);
    }
    private void title(GuiGraphics g, String text) { drawScaled(g, text, 51, 20, INK, TITLE_SCALE); }
    private void label(GuiGraphics g, int x, int y, String text, int color) {
        drawScaled(g, text, x, y, color, TEXT_SCALE);
    }
    private void line(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(left + x1, top + y1, left + x2, top + y2, color);
    }
    private void item(GuiGraphics g, Item item, int x, int y) {
        if (item == null) return;
        g.pose().pushPose();
        g.pose().translate(left + x, top + y, 0);
        g.pose().scale(ICON_SCALE, ICON_SCALE, 1.0F);
        g.renderItem(new ItemStack(item), 0, 0);
        g.pose().popPose();
    }
    private void button(GuiGraphics g, int x, int y, int w, int h, String text, boolean enabled) {
        g.fill(left + x, top + y, left + x + w, top + y + h, enabled ? 0xFFBAA17B : 0xFFD3C3A5);
        centered(g, x + w / 2, y + 3, text, enabled ? INK : MUTED, TEXT_SCALE);
    }
    private int wrap(GuiGraphics g, String content, int x, int y, int maxWidth, int color) {
        for (FormattedCharSequence row : font.split(Component.literal(content), Math.max(1, (int)(maxWidth / TEXT_SCALE)))) {
            g.pose().pushPose();
            g.pose().translate(left + x, top + y, 0);
            g.pose().scale(TEXT_SCALE, TEXT_SCALE, 1.0F);
            g.drawString(font, row, 0, 0, color, false);
            g.pose().popPose();
            y += 16;
        }
        return y;
    }

    private void cover(GuiGraphics g) {
        title(g, "Forging Guide");
        item(g, ExampleMod.FORGING_BLOCK_ITEM.get(), 90, 77);
        item(g, ExampleMod.FORGING_ANVIL_ITEM.get(), 168, 77);
        item(g, ExampleMod.FORGED_EQUIPMENT_ITEM.get(), 246, 77);
        label(g, 37, 119, "Forge parts > Assemble gear > Find skills", INK);
        wrap(g, "Recipes, forging steps, and your personal discovery journal.", 37, 148, 310, MUTED);
        label(g, 37, 204, "Craft: Book + Paper + Ink Sac", GOLD);
    }
    private void overview(GuiGraphics g) {
        title(g, "Getting Started");
        item(g, ExampleMod.FORGING_BLOCK_ITEM.get(), 25, 67);
        wrap(g, "1. Add a Blueprint, monster drop, matching base materials, and fuel to the Forge.", 66, 65, 284, INK);
        item(g, ExampleMod.FORGED_HEAD_ITEM.get(), 25, 125);
        wrap(g, "2. Play the timing game to make a Head, Core, and Rod. Blueprints are reusable.", 66, 123, 284, INK);
        item(g, ExampleMod.FORGING_ANVIL_ITEM.get(), 25, 183);
        wrap(g, "3. Assemble all three parts at the Anvil. The rhythm game awards a Blessing or Curse.", 66, 181, 284, INK);
    }
    private void grid(GuiGraphics g, Item[] cells, int x, int y) {
        for (int i = 0; i < 9; i++) {
            int bx = x + (i % 3) * 31, by = y + (i / 3) * 31;
            g.fill(left + bx, top + by, left + bx + 29, top + by + 29, 0xFFE3D0AB);
            if (cells[i] != null) item(g, cells[i], bx + 1, by + 1);
        }
    }
    private void blockRecipes(GuiGraphics g) {
        title(g, "Crafting Stations");
        button(g, 23, 57, 116, 22, blockRecipe == 0 ? "[ Forge ]" : "Forge", true);
        button(g, 145, 57, 116, 22, blockRecipe == 1 ? "[ Anvil ]" : "Anvil", true);
        Item brick = Items.STONE_BRICKS, iron = Items.IRON_INGOT;
        Item[] cells = blockRecipe == 0
                ? new Item[]{brick, iron, brick, iron, Items.FURNACE, iron, brick, iron, brick}
                : new Item[]{iron, iron, iron, null, iron, null, brick, iron, brick};
        grid(g, cells, 47, 92);
        item(g, blockRecipe == 0 ? ExampleMod.FORGING_BLOCK_ITEM.get() : ExampleMod.FORGING_ANVIL_ITEM.get(), 259, 126);
        label(g, 177, 125, "->", GOLD);
        wrap(g, blockRecipe == 0 ? "Forge: make a Head, Core, or Rod." : "Anvil: assemble your forged gear.", 29, 213, 320, INK);
    }
    private Item blueprintItem() {
        return switch (blueprint) {
            case 0 -> ExampleMod.SWORD_HEAD_BLUEPRINT.get();
            case 1 -> ExampleMod.AXE_HEAD_BLUEPRINT.get();
            case 2 -> ExampleMod.PICKAXE_HEAD_BLUEPRINT.get();
            case 3 -> ExampleMod.SHOVEL_HEAD_BLUEPRINT.get();
            case 4 -> ExampleMod.HOE_HEAD_BLUEPRINT.get();
            case 5 -> ExampleMod.CORE_BLUEPRINT.get();
            default -> ExampleMod.ROD_BLUEPRINT.get();
        };
    }
    private void blueprintRecipes(GuiGraphics g) {
        title(g, "Seven Blueprints");
        label(g, 24, 61, "Paper + Ink Sac + example item", INK);
        label(g, 24, 79, "Shapeless crafting recipe", MUTED);
        grid(g, new Item[]{Items.PAPER, Items.INK_SAC, BLUEPRINT_BASES[blueprint],
                null, null, null, null, null, null}, 40, 103);
        label(g, 165, 137, "->", GOLD);
        item(g, blueprintItem(), 230, 129);
        button(g, 26, 205, 27, 21, "<", blueprint > 0);
        label(g, 69, 210, BLUEPRINT_NAMES[blueprint] + " Blueprint", GREEN);
        button(g, 320, 205, 27, 21, ">", blueprint < 6);
        label(g, 26, 230, "Core needs only Paper and Ink Sac.", MUTED);
    }
    private void materials(GuiGraphics g) {
        title(g, "Monster Materials");
        for (int i = 0; i < MATERIALS.length; i++) {
            int x = 18 + (i % 3) * 117, y = 59 + (i / 3) * 34;
            item(g, MATERIALS[i], x, y);
            drawScaled(g, MATERIAL_NAMES[i], x + 29, y + 7, INK, 1.05F);
        }
        wrap(g, "Each material has its own pool of possible skills.", 19, 232, 340, MUTED);
    }
    private void forge(GuiGraphics g) {
        title(g, "Using the Forge");
        item(g, ExampleMod.CORE_BLUEPRINT.get(), 31, 76);
        item(g, Items.BLAZE_ROD, 113, 76);
        item(g, Items.IRON_INGOT, 196, 76);
        item(g, Items.COAL, 279, 76);
        label(g, 25, 112, "Blueprint + Drop + Base Materials + Fuel", INK);
        wrap(g, "Example: Core Blueprint, Blaze Rod, five Iron Ingots, and two Coal to forge a Core.", 26, 138, 325, INK);
        wrap(g, "Heads: Sword 2, Axe 3, Pickaxe 3, Shovel 1, Hoe 2. Rod 2; Core 5.", 26, 171, 325, MUTED);
        wrap(g, "Coal: 8 energy. Lava Bucket: 100. Forge capacity: 150.", 26, 213, 325, MUTED);
    }
    private void timing(GuiGraphics g) {
        title(g, "Forging Minigame");
        label(g, 30, 70, "Hit the timing mark to earn a part.", INK);
        g.fill(left + 39, top + 108, left + 336, top + 123, 0xFF8C7656);
        g.fill(left + 190, top + 105, left + 206, top + 126, GOLD);
        label(g, 36, 147, "Results have Tier I, II, or III.", GREEN);
        wrap(g, "A Head reveals its skill right away. Core and Rod skills appear after assembly at the Anvil.", 36, 174, 310, INK);
    }
    private void anvil(GuiGraphics g) {
        title(g, "Assembling Gear");
        item(g, ExampleMod.FORGED_HEAD_ITEM.get(), 61, 73);
        item(g, ExampleMod.FORGED_CORE_ITEM.get(), 155, 73);
        item(g, ExampleMod.FORGED_ROD_ITEM.get(), 250, 73);
        label(g, 75, 111, "Head   +   Core   +   Rod", INK);
        item(g, ExampleMod.FORGED_EQUIPMENT_ITEM.get(), 174, 144);
        wrap(g, "Matching skills combine, up to Tier III. Finish the rhythm game for a Blessing or Curse.", 29, 187, 320, INK);
    }
    private void journal(GuiGraphics g) {
        title(g, "Discovery Journal");
        journalRow(g, 65, "Skills", ForgingGuideClientState.foundSkills() + " / " + ForgingEffect.values().length, GREEN);
        journalRow(g, 111, "Blessings", ForgingGuideClientState.foundBlessings() + " / " + ForgedBlessing.values().length, GOLD);
        journalRow(g, 157, "Curses", ForgingGuideClientState.foundCurses() + " / " + ForgedCurse.values().length, RED);
        label(g, 27, 218, "Click a category to read discovered entries.", MUTED);
    }
    private void journalRow(GuiGraphics g, int y, String name, String count, int accent) {
        g.fill(left + 24, top + y, left + 352, top + y + 39, 0xFFCAB592);
        g.fill(left + 31, top + y + 7, left + 51, top + y + 32, accent);
        label(g, 65, y + 10, name, INK);
        label(g, 265, y + 10, count, MUTED);
    }
    private void discoveries(GuiGraphics g, int kind) {
        title(g, kind == 10 ? "Discovered Skills" : kind == 11 ? "Discovered Blessings" : "Discovered Curses");
        int total = kind == 10 ? ForgingEffect.values().length : kind == 11 ? ForgedBlessing.values().length : ForgedCurse.values().length;
        int rows = 7;
        for (int row = 0; row < rows && listOffset + row < total; row++) {
            int i = listOffset + row, y = 58 + row * 24;
            boolean known = known(kind, i);
            g.fill(left + 19, top + y, left + 169, top + y + 22,
                    selected == i && known ? 0xFFC0A176 : 0xFFD6C4A3);
            String name = known ? entryName(kind, i) : "???";
            String fitted = font.plainSubstrByWidth(name, (int)(138 / TEXT_SCALE));
            label(g, 25, y + 5, fitted, known ? INK : MUTED);
        }
        line(g, 176, 55, 178, 226, 0xFF9A805A);
        if (selected >= 0 && selected < total && known(kind, selected)) {
            String name = entryName(kind, selected);
            int y = wrap(g, name, 190, 62, 165, kind == 10 ? GREEN : kind == 11 ? GOLD : RED) + 8;
            if (kind == 10) {
                ForgingEffect effect = ForgingEffect.values()[selected];
                label(g, 190, y, "Tier " + roman(ForgingGuideClientState.tier(effect)), INK);
                y += 17;
                label(g, 190, y, effect.material().name().replace('_', ' '), MUTED);
                y += 18;
                drawScaled(g, "I " + (ForgingGuideClientState.hasTier(effect, 1) ? "+" : "?")
                        + "  II " + (ForgingGuideClientState.hasTier(effect, 2) ? "+" : "?")
                        + "  III " + (ForgingGuideClientState.hasTier(effect, 3) ? "+" : "?"), 190, y, MUTED, 1.1F);
                y += 18;
                wrap(g, ForgingGuideDescriptions.effect(effect, ForgingGuideClientState.tier(effect)), 190, y, 167, INK);
            } else {
                wrap(g, kind == 11 ? ForgingGuideDescriptions.blessing(ForgedBlessing.values()[selected])
                        : ForgingGuideDescriptions.curse(ForgedCurse.values()[selected]), 190, y, 167, INK);
            }
        } else wrap(g, "Choose an entry to read its effect. Undiscovered entries stay hidden as ???.", 190, 70, 165, MUTED);
        if (listOffset > 0) button(g, 23, 228, 29, 17, "↑", true);
        if (listOffset + rows < total) button(g, 130, 228, 29, 17, "↓", true);
        label(g, 65, 230, (listOffset + 1) + "-" + Math.min(total, listOffset + rows), MUTED);
    }
    private static String roman(int tier) { return tier == 1 ? "I" : tier == 2 ? "II" : "III"; }
    private static boolean known(int kind, int index) {
        return switch (kind) {
            case 10 -> ForgingGuideClientState.tier(ForgingEffect.values()[index]) > 0;
            case 11 -> ForgingGuideClientState.has(ForgedBlessing.values()[index]);
            default -> ForgingGuideClientState.has(ForgedCurse.values()[index]);
        };
    }
    private static String entryName(int kind, int index) {
        return kind == 10 ? ForgingEffect.values()[index].displayName()
                : kind == 11 ? ForgedBlessing.values()[index].displayName()
                : ForgedCurse.values()[index].displayName();
    }

    @Override public boolean mouseClicked(double mx, double my, int button) {
        int x = (int) ((mx - left) / scale), y = (int) ((my - top) / scale);
        if (button == 0) {
            if (y >= H - 20 && y < H - 1) {
                if (x >= 17 && x < 62 && page > 1) changePage(page - 1);
                if (x >= W - 62 && x < W - 17 && page < 12) changePage(page + 1);
                return true;
            }
            if (page == 3 && y >= 57 && y < 79) { if (x >= 23 && x < 139) blockRecipe = 0; if (x >= 145 && x < 261) blockRecipe = 1; return true; }
            if (page == 4 && y >= 205 && y < 226) {
                if (x >= 26 && x < 53) blueprint = Math.max(0, blueprint - 1);
                if (x >= 320 && x < 347) blueprint = Math.min(6, blueprint + 1);
                return true;
            }
            if (page == 9 && x >= 24 && x < 352) {
                if (y >= 65 && y < 104) changePage(10);
                if (y >= 111 && y < 150) changePage(11);
                if (y >= 157 && y < 196) changePage(12);
                return true;
            }
            if (page >= 10 && x >= 19 && x < 169) {
                if (y >= 58 && y < 226) {
                    int i = listOffset + (y - 58) / 24;
                    int total = page == 10 ? ForgingEffect.values().length : page == 11 ? ForgedBlessing.values().length : ForgedCurse.values().length;
                    if (i < total && known(page, i)) selected = i;
                }
                if (y >= 228 && y < 245) {
                    if (x < 54) listOffset = Math.max(0, listOffset - 7);
                    if (x >= 130) listOffset = Math.min(maxOffset(), listOffset + 7);
                }
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }
    private int maxOffset() {
        int total = page == 10 ? ForgingEffect.values().length : page == 11 ? ForgedBlessing.values().length : ForgedCurse.values().length;
        return Math.max(0, ((total - 1) / 7) * 7);
    }
    private void changePage(int next) { page = next; selected = -1; listOffset = 0; }
    @Override public boolean keyPressed(int key, int scan, int modifiers) {
        if (key == GLFW.GLFW_KEY_E) { onClose(); return true; }
        if (key == GLFW.GLFW_KEY_LEFT && page > 1) { changePage(page - 1); return true; }
        if (key == GLFW.GLFW_KEY_RIGHT && page < 12) { changePage(page + 1); return true; }
        return super.keyPressed(key, scan, modifiers);
    }
    @Override public boolean isPauseScreen() { return false; }
}
