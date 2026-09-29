package com.example.examplemod;

import com.example.examplemod.block.*;
import com.example.examplemod.entity.*;
import com.example.examplemod.item.*;
import com.example.examplemod.skill.*;
import com.example.examplemod.skill.client.*;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Concept-layout pre-Miniganme 1 screen. */
public class ForgingScreen extends AbstractContainerScreen<ForgeMenu> {
    private String status = "";

    private static final int BG = 0xFF20252B, PANEL = 0xFF171B20, PANEL2 = 0xFF292F36;
    private static final int STEEL_DARK = 0xFF0B0E12, STEEL = 0xFF3A424B, STEEL_LIGHT = 0xFF59636E;
    private static final int FRAME = 0xFF6F3D20, COPPER = 0xFFD27A34, TEXT = 0xFFF4E9D8;
    private static final int MUTED = 0xFFA8B0B8, READY = 0xFF77DC5B, WARN = 0xFFFFB24D;

    public ForgingScreen(ForgeMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        imageWidth = 320;
        imageHeight = 262;
        inventoryLabelX = 75;
        inventoryLabelY = 166;
    }

    @Override protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("FORGE"), b -> startForge())
                .bounds(leftPos + 120, topPos + 137, 80, 20).build());
    }

    private void startForge() {
        if (!menu.hasValidRecipe()) { status = missingRecipeMessage(); return; }
        ForgingBlueprintType blueprint = menu.selectedBlueprint();
        ForgingMetal metal = menu.selectedMetal();
        MonsterMaterial monster = menu.selectedMonster();
        menu.consumeRecipe();
        if (minecraft == null) return;
        if (blueprint == ForgingBlueprintType.CORE) minecraft.setScreen(new CoreTimingBarScreen(metal, monster));
        else if (blueprint == ForgingBlueprintType.ROD) minecraft.setScreen(new RodTimingBarScreen(metal, monster));
        else if (blueprint != null && blueprint.headType() != null) minecraft.setScreen(new TimingBarScreen(metal, blueprint.headType(), monster));
    }

    private String missingRecipeMessage() {
        if (menu.stackAt(ForgeMenu.BLUEPRINT_SLOT).isEmpty()) return "Insert Blueprint";
        if (menu.stackAt(ForgeMenu.MONSTER_SLOT).isEmpty()) return "Insert Material";
        for (int i=ForgeMenu.METAL_START;i<ForgeMenu.METAL_END;i++) if(menu.stackAt(i).isEmpty()) return "Insert 5 Metal Ingots";
        return "Metal Ingots Must Match";
    }

    @Override protected void renderBg(GuiGraphics g,float pt,int mx,int my) {
        int x=leftPos,y=topPos;
        // Forged-anvil frame: black shell, steel bevel, thin copper inlay.
        forgedFrame(g,x,y,320,262);

        // Separate metal nameplate instead of a flat full-width header.
        metalPlate(g,x+65,y+4,190,25);
        g.fill(x+18,y+16,x+65,y+18,STEEL);
        g.fill(x+255,y+16,x+302,y+18,STEEL);
        g.fill(x+18,y+19,x+65,y+20,COPPER);
        g.fill(x+255,y+19,x+302,y+20,COPPER);

        // Open work surface: no bulky component cards.
        g.fill(x+16,y+51,x+304,y+119,PANEL);
        g.fill(x+18,y+53,x+302,y+117,0xFF1D2228);
        slot(g,x+41,y+64,0xFF58A4EA);
        slot(g,x+102,y+64,0xFFB565DB);
        slot(g,x+156,y+56,COPPER); slot(g,x+180,y+56,COPPER); slot(g,x+204,y+56,COPPER);
        slot(g,x+168,y+80,COPPER); slot(g,x+192,y+80,COPPER);
        slot(g,x+255,y+64,0xFF777E86);

        // Thin forged divider and status/button bay.
        g.fill(x+24,y+123,x+296,y+124,STEEL_LIGHT);
        g.fill(x+38,y+124,x+282,y+125,COPPER);
        g.fill(x+28,y+128,x+292,y+160,PANEL);

        // Inventory is inset like a recessed tool tray.
        g.fill(x+58,y+166,x+262,y+256,STEEL_DARK);
        g.fill(x+60,y+168,x+260,y+254,STEEL);
        g.fill(x+63,y+171,x+257,y+252,PANEL);
        for(int r=0;r<3;r++) for(int c=0;c<9;c++) invSlot(g,x+74+c*18,y+178+r*18);
        for(int c=0;c<9;c++) invSlot(g,x+74+c*18,y+236);
    }

    private void forgedFrame(GuiGraphics g,int x,int y,int w,int h){
        g.fill(x,y,x+w,y+h,STEEL_DARK);
        g.fill(x+3,y+3,x+w-3,y+h-3,STEEL_LIGHT);
        g.fill(x+6,y+6,x+w-6,y+h-6,STEEL);
        g.fill(x+8,y+8,x+w-8,y+h-8,COPPER);
        g.fill(x+10,y+10,x+w-10,y+h-10,BG);
        // reinforced corner plates
        corner(g,x+3,y+3); corner(g,x+w-15,y+3);
        corner(g,x+3,y+h-15); corner(g,x+w-15,y+h-15);
    }
    private void corner(GuiGraphics g,int x,int y){
        g.fill(x,y,x+12,y+4,STEEL_LIGHT); g.fill(x,y,x+4,y+12,STEEL_LIGHT);
        g.fill(x+2,y+2,x+10,y+3,COPPER); g.fill(x+2,y+2,x+3,y+10,COPPER);
    }
    private void metalPlate(GuiGraphics g,int x,int y,int w,int h){
        g.fill(x,y,x+w,y+h,STEEL_DARK); g.fill(x+2,y+2,x+w-2,y+h-2,STEEL_LIGHT);
        g.fill(x+4,y+4,x+w-4,y+h-4,PANEL); g.fill(x+5,y+h-6,x+w-5,y+h-4,COPPER);
    }
    private void card(GuiGraphics g,int x,int y,int w,int h,int accent){
        g.fill(x,y,x+w,y+h,0xFF0F1216); g.fill(x+1,y+1,x+w-1,y+h-1,accent);
        g.fill(x+3,y+3,x+w-3,y+h-3,PANEL2);
    }
    private void slot(GuiGraphics g,int x,int y,int accent){
        g.fill(x,y,x+20,y+20,0xFF090B0E); g.fill(x+1,y+1,x+19,y+19,accent);
        g.fill(x+3,y+3,x+17,y+17,0xFF242A31);
    }
    private void invSlot(GuiGraphics g,int x,int y){
        g.fill(x,y,x+20,y+20,0xFF0D1014); g.fill(x+1,y+1,x+19,y+19,0xFF3B424B);
        g.fill(x+3,y+3,x+17,y+17,0xFF272D34);
    }

    @Override protected void renderLabels(GuiGraphics g,int mx,int my){
        g.drawCenteredString(font,"MONSTER FORGE",160,11,TEXT);

        g.drawCenteredString(font,"BLUEPRINT",51,47,0xFF9CCFFF);
        g.drawCenteredString(font,"MATERIAL",112,47,0xFFE2B6FF);
        g.drawCenteredString(font,"METAL INGOTS",187,43,0xFFFFC27A);
        g.drawCenteredString(font,"FUEL",264,47,0xFFB5BAC0);
        g.drawCenteredString(font,"RESERVED",264,93,0xFF70777E);

        boolean ready=menu.hasValidRecipe();
        if (ready) g.drawCenteredString(font,"RECIPE READY",160,125,READY);
        else if (!status.isEmpty()) g.drawCenteredString(font,status,160,125,WARN);
        g.drawCenteredString(font,"INVENTORY",160,166,MUTED);
    }

    @Override public void render(GuiGraphics g,int mx,int my,float pt){
        renderBackground(g,mx,my,pt); super.render(g,mx,my,pt); renderTooltip(g,mx,my);
    }
}
