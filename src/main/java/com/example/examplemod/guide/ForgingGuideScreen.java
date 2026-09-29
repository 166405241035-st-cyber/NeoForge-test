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
    private static final int W = 380, H = 266, INK = 0xFF302920, MUTED = 0xFF665640;
    private static final int GOLD = 0xFF995E1C, GREEN = 0xFF298150, RED = 0xFF9A4336;
    private static final Item[] MATERIALS = {Items.ROTTEN_FLESH, Items.BONE, Items.STRING, Items.GUNPOWDER,
            Items.SLIME_BALL, Items.ENDER_PEARL, Items.BLAZE_ROD, Items.GHAST_TEAR,
            Items.WITHER_SKELETON_SKULL, Items.PHANTOM_MEMBRANE, Items.DRAGON_BREATH,
            Items.SHULKER_SHELL, Items.NETHER_STAR};
    private static final String[] MATERIAL_NAMES = {"เนื้อเน่า", "กระดูก", "เส้นใย", "ดินปืน", "สไลม์",
            "เอนเดอร์", "แท่งเบลซ", "น้ำตาแกสต์", "หัววิเทอร์", "เยื่อแฟนทอม",
            "ลมหายใจมังกร", "เปลือกชัลเกอร์", "ดาวเนเธอร์"};
    private static final String[] BLUEPRINT_NAMES = {"Sword", "Axe", "Pickaxe", "Shovel", "Hoe", "Core", "Rod"};
    private static final Item[] BLUEPRINT_BASES = {Items.WOODEN_SWORD, Items.WOODEN_AXE, Items.WOODEN_PICKAXE,
            Items.WOODEN_SHOVEL, Items.WOODEN_HOE, null, Items.STICK};
    private int page = 1, selected = -1, listOffset, blueprint, blockRecipe;
    private int left, top;
    private float scale = 1.0F;

    private ForgingGuideScreen() { super(Component.literal("คู่มือการตีเหล็ก")); }

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
        g.fill(0, 0, width, height, 0xAA14181B);
    }

    @Override public void render(GuiGraphics g, int mx, int my, float partial) {
        renderBackground(g, mx, my, partial);
        g.pose().pushPose();
        g.pose().translate(left * (1.0F - scale), top * (1.0F - scale), 0);
        g.pose().scale(scale, scale, 1);
        g.fill(left, top, left + W, top + H, 0xFF3C3229);
        g.fill(left + 5, top + 5, left + W - 5, top + H - 5, 0xFFE6D7B8);
        g.fill(left + 11, top + 11, left + W - 11, top + H - 11, 0xFFEEDDCA);
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
        g.drawCenteredString(font, page + " / 12", left + W / 2, top + H - 15, MUTED);
        g.pose().popPose();
        super.render(g, mx, my, partial);
    }

    private void title(GuiGraphics g, String text) { g.drawString(font, text, left + 51, top + 24, INK, false); }
    private void label(GuiGraphics g, int x, int y, String text, int color) {
        g.drawString(font, text, left + x, top + y, color, false);
    }
    private void line(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(left + x1, top + y1, left + x2, top + y2, color);
    }
    private void item(GuiGraphics g, Item item, int x, int y) { if (item != null) g.renderItem(new ItemStack(item), left + x, top + y); }
    private void button(GuiGraphics g, int x, int y, int w, int h, String text, boolean enabled) {
        g.fill(left + x, top + y, left + x + w, top + y + h, enabled ? 0xFFBAA17B : 0xFFD3C3A5);
        g.drawCenteredString(font, text, left + x + w / 2, top + y + 5, enabled ? INK : MUTED);
    }
    private int wrap(GuiGraphics g, String content, int x, int y, int maxWidth, int color) {
        for (FormattedCharSequence row : font.split(Component.literal(content), maxWidth)) {
            g.drawString(font, row, left + x, top + y, color, false);
            y += 13;
        }
        return y;
    }

    private void cover(GuiGraphics g) {
        title(g, "คู่มือการตีเหล็ก");
        item(g, ExampleMod.FORGING_BLOCK_ITEM.get(), 91, 85);
        item(g, ExampleMod.FORGING_ANVIL_ITEM.get(), 154, 85);
        item(g, ExampleMod.FORGED_EQUIPMENT_ITEM.get(), 218, 85);
        label(g, 34, 118, "ตีชิ้นส่วน • ประกอบอุปกรณ์ • ค้นพบสกิล", INK);
        wrap(g, "เปิดอ่านสูตรคราฟต์ วิธีตี และบันทึกสิ่งที่คุณค้นพบ", 34, 148, 310, MUTED);
        label(g, 34, 197, "คราฟต์: หนังสือ + กระดาษ + ถุงหมึก", GOLD);
    }
    private void overview(GuiGraphics g) {
        title(g, "เริ่มตีอุปกรณ์");
        item(g, ExampleMod.FORGING_BLOCK_ITEM.get(), 28, 75);
        wrap(g, "1. ใส่ Blueprint + วัตถุดิบมอนสเตอร์ + โลหะ 5 ชิ้น + เชื้อเพลิงในเตา", 54, 74, 292, INK);
        item(g, ExampleMod.FORGED_HEAD_ITEM.get(), 28, 117);
        wrap(g, "2. เล่นมินิเกมเพื่อรับหัว, Core และด้าม โดย Blueprint ใช้ซ้ำได้", 54, 115, 292, INK);
        item(g, ExampleMod.FORGING_ANVIL_ITEM.get(), 28, 162);
        wrap(g, "3. นำทั้งสามชิ้นไปประกอบที่ทั่ง แล้วเล่นมินิเกมจังหวะเพื่อรับพรหรือคำสาป", 54, 159, 292, INK);
    }
    private void grid(GuiGraphics g, Item[] cells, int x, int y) {
        for (int i = 0; i < 9; i++) {
            int bx = x + (i % 3) * 22, by = y + (i / 3) * 22;
            g.fill(left + bx, top + by, left + bx + 20, top + by + 20, 0xFFBDA985);
            if (cells[i] != null) item(g, cells[i], bx + 2, by + 2);
        }
    }
    private void blockRecipes(GuiGraphics g) {
        title(g, "สูตรคราฟต์บล็อก");
        button(g, 23, 57, 116, 19, blockRecipe == 0 ? "[ เตาหลอม ]" : "เตาหลอม", true);
        button(g, 145, 57, 116, 19, blockRecipe == 1 ? "[ ทั่งตีเหล็ก ]" : "ทั่งตีเหล็ก", true);
        Item brick = Items.STONE_BRICKS, iron = Items.IRON_INGOT;
        Item[] cells = blockRecipe == 0
                ? new Item[]{brick, iron, brick, iron, Items.FURNACE, iron, brick, iron, brick}
                : new Item[]{iron, iron, iron, null, iron, null, brick, iron, brick};
        grid(g, cells, 56, 98);
        item(g, blockRecipe == 0 ? ExampleMod.FORGING_BLOCK_ITEM.get() : ExampleMod.FORGING_ANVIL_ITEM.get(), 242, 123);
        label(g, 155, 123, "→", GOLD);
        wrap(g, blockRecipe == 0 ? "เตาหลอม: ใช้ตีหัว Core และด้าม" : "ทั่ง: ใช้ประกอบเครื่องมือที่ตีแล้ว", 34, 194, 310, INK);
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
        title(g, "Blueprint ทั้ง 7 แบบ");
        label(g, 30, 62, "กระดาษ + ถุงหมึก + ของตัวอย่าง", INK);
        label(g, 30, 81, "ผสมแบบไร้รูปร่างในโต๊ะคราฟต์", MUTED);
        grid(g, new Item[]{Items.PAPER, Items.INK_SAC, BLUEPRINT_BASES[blueprint],
                null, null, null, null, null, null}, 53, 109);
        label(g, 144, 133, "→", GOLD);
        item(g, blueprintItem(), 206, 129);
        button(g, 26, 195, 25, 18, "<", blueprint > 0);
        label(g, 65, 200, BLUEPRINT_NAMES[blueprint] + " Blueprint", GREEN);
        button(g, 320, 195, 25, 18, ">", blueprint < 6);
        label(g, 32, 222, "Core ใช้เพียงกระดาษกับถุงหมึก", MUTED);
    }
    private void materials(GuiGraphics g) {
        title(g, "ของมอนสเตอร์ที่ใช้ตี");
        for (int i = 0; i < MATERIALS.length; i++) {
            int x = 22 + (i % 4) * 89, y = 62 + (i / 4) * 40;
            item(g, MATERIALS[i], x, y);
            label(g, x + 19, y + 5, MATERIAL_NAMES[i], INK);
        }
        wrap(g, "วัตถุดิบแต่ละชนิดกำหนดกลุ่มสกิลที่อาจได้ ทดลองเปลี่ยนวัสดุเพื่อค้นพบผลใหม่", 23, 226, 332, MUTED);
    }
    private void forge(GuiGraphics g) {
        title(g, "ใช้เตาหลอม");
        item(g, ExampleMod.CORE_BLUEPRINT.get(), 27, 72);
        item(g, Items.BLAZE_ROD, 84, 72);
        item(g, Items.IRON_INGOT, 141, 72);
        item(g, Items.COAL, 200, 72);
        label(g, 26, 97, "Blueprint  +  ของมอน  +  โลหะ ×5  +  เชื้อเพลิง", INK);
        wrap(g, "ตัวอย่าง: Blueprint ของ Core + แท่งเบลซ + เหล็ก 5 แท่ง + ถ่าน เพื่อเริ่มตี Core", 28, 123, 320, INK);
        wrap(g, "ถ่านให้พลังงาน 8 หน่วย ลาวาหนึ่งถังให้ 100 หน่วย เตาเก็บได้สูงสุด 150 หน่วย", 28, 174, 320, MUTED);
    }
    private void timing(GuiGraphics g) {
        title(g, "มินิเกมตีชิ้นส่วน");
        label(g, 32, 68, "กดให้ตรงจังหวะเพื่อรับชิ้นส่วน", INK);
        g.fill(left + 39, top + 108, left + 336, top + 123, 0xFF8C7656);
        g.fill(left + 190, top + 105, left + 206, top + 126, GOLD);
        label(g, 42, 146, "ผลที่ได้มี Tier I / II / III", GREEN);
        wrap(g, "หัวแสดงสกิลที่ค้นพบทันที ส่วน Core และด้ามจะเผยสกิลเมื่อประกอบที่ทั่ง", 42, 171, 295, INK);
    }
    private void anvil(GuiGraphics g) {
        title(g, "ประกอบที่ทั่ง");
        item(g, ExampleMod.FORGED_HEAD_ITEM.get(), 54, 79);
        item(g, ExampleMod.FORGED_CORE_ITEM.get(), 126, 79);
        item(g, ExampleMod.FORGED_ROD_ITEM.get(), 198, 79);
        label(g, 70, 110, "หัว     +     Core     +     ด้าม", INK);
        item(g, ExampleMod.FORGED_EQUIPMENT_ITEM.get(), 157, 144);
        wrap(g, "สกิลที่ซ้ำกันจะรวม Tier ได้สูงสุด III จากนั้นเล่นมินิเกมจังหวะเพื่อรับพรหรือคำสาป", 31, 181, 316, INK);
    }
    private void journal(GuiGraphics g) {
        title(g, "บันทึกการค้นพบ");
        journalRow(g, 65, "สกิลที่พบ", ForgingGuideClientState.foundSkills() + " / " + ForgingEffect.values().length, GREEN);
        journalRow(g, 111, "พรที่พบ", ForgingGuideClientState.foundBlessings() + " / " + ForgedBlessing.values().length, GOLD);
        journalRow(g, 157, "คำสาปที่พบ", ForgingGuideClientState.foundCurses() + " / " + ForgedCurse.values().length, RED);
        label(g, 27, 215, "คลิกหมวดเพื่ออ่านรายการที่ปลดล็อก", MUTED);
    }
    private void journalRow(GuiGraphics g, int y, String name, String count, int accent) {
        g.fill(left + 24, top + y, left + 352, top + y + 39, 0xFFCAB592);
        g.fill(left + 31, top + y + 7, left + 51, top + y + 32, accent);
        label(g, 65, y + 8, name, INK);
        label(g, 257, y + 8, count, MUTED);
    }
    private void discoveries(GuiGraphics g, int kind) {
        title(g, kind == 10 ? "สกิลที่ปลดล็อก" : kind == 11 ? "พรที่ปลดล็อก" : "คำสาปที่ปลดล็อก");
        int total = kind == 10 ? ForgingEffect.values().length : kind == 11 ? ForgedBlessing.values().length : ForgedCurse.values().length;
        int rows = 7;
        for (int row = 0; row < rows && listOffset + row < total; row++) {
            int i = listOffset + row, y = 58 + row * 24;
            boolean known = known(kind, i);
            g.fill(left + 19, top + y, left + 169, top + y + 22,
                    selected == i && known ? 0xFFC0A176 : 0xFFD6C4A3);
            String name = known ? entryName(kind, i) : "???";
            String fitted = font.plainSubstrByWidth(name, 138);
            label(g, 25, y + 7, fitted, known ? INK : MUTED);
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
                y += 16;
                label(g, 190, y, "I " + (ForgingGuideClientState.hasTier(effect, 1) ? "✓" : "?")
                        + "   II " + (ForgingGuideClientState.hasTier(effect, 2) ? "✓" : "?")
                        + "   III " + (ForgingGuideClientState.hasTier(effect, 3) ? "✓" : "?"), MUTED);
                y += 20;
                wrap(g, ForgingGuideDescriptions.effect(effect, ForgingGuideClientState.tier(effect)), 190, y, 167, INK);
            } else {
                wrap(g, kind == 11 ? ForgingGuideDescriptions.blessing(ForgedBlessing.values()[selected])
                        : ForgingGuideDescriptions.curse(ForgedCurse.values()[selected]), 190, y, 167, INK);
            }
        } else wrap(g, "เลือกสิ่งที่ค้นพบเพื่ออ่านรายละเอียด รายการที่ยังไม่พบจะแสดง ???", 190, 70, 165, MUTED);
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
            if (page == 3 && y >= 57 && y < 77) { if (x >= 23 && x < 139) blockRecipe = 0; if (x >= 145 && x < 261) blockRecipe = 1; return true; }
            if (page == 4 && y >= 195 && y < 214) {
                if (x >= 26 && x < 51) blueprint = Math.max(0, blueprint - 1);
                if (x >= 320 && x < 345) blueprint = Math.min(6, blueprint + 1);
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
        if (key == GLFW.GLFW_KEY_LEFT && page > 1) { changePage(page - 1); return true; }
        if (key == GLFW.GLFW_KEY_RIGHT && page < 12) { changePage(page + 1); return true; }
        return super.keyPressed(key, scan, modifiers);
    }
    @Override public boolean isPauseScreen() { return false; }
}
