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
    private String status = "Insert the required components";

    private static final int BG = 0xFF20252B, PANEL = 0xFF171B20, PANEL2 = 0xFF292F36;
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
        // outer frame
        g.fill(x,y,x+320,y+262,0xFF101317);
        g.fill(x+2,y+2,x+318,y+260,FRAME);
        g.fill(x+6,y+6,x+314,y+256,BG);

        // title
        g.fill(x+12,y+10,x+308,y+42,PANEL);
        g.fill(x+12,y+42,x+308,y+45,COPPER);

        // recipe area
        g.fill(x+16,y+51,x+304,y+119,PANEL);
        // Blueprint / Material cards
        card(g,x+26,y+55,50,52,0xFF477DB1);
        card(g,x+87,y+55,50,52,0xFF8B50AF);
        // Metals card
        card(g,x+146,y+51,82,62,FRAME);
        // Fuel card
        card(g,x+244,y+55,40,52,0xFF555D66);

        slot(g,x+41,y+64,0xFF58A4EA);
        slot(g,x+102,y+64,0xFFB565DB);
        slot(g,x+156,y+56,COPPER); slot(g,x+180,y+56,COPPER); slot(g,x+204,y+56,COPPER);
        slot(g,x+168,y+80,COPPER); slot(g,x+192,y+80,COPPER);
        slot(g,x+255,y+64,0xFF777E86);

        // status + button area
        g.fill(x+16,y+124,x+304,y+162,PANEL);
        g.fill(x+16,y+161,x+304,y+164,FRAME);

        // inventory
        g.fill(x+62,y+169,x+258,y+255,PANEL);
        for(int r=0;r<3;r++) for(int c=0;c<9;c++) invSlot(g,x+74+c*18,y+178+r*18);
        for(int c=0;c<9;c++) invSlot(g,x+74+c*18,y+236);
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
        g.drawCenteredString(font,"MONSTER FORGE",160,14,TEXT);
        g.drawCenteredString(font,"Prepare components for forging",160,28,MUTED);

        g.drawCenteredString(font,"BLUEPRINT",51,47,0xFF9CCFFF);
        g.drawCenteredString(font,"MATERIAL",112,47,0xFFE2B6FF);
        g.drawCenteredString(font,"METAL INGOTS",187,43,0xFFFFC27A);
        g.drawCenteredString(font,"FUEL",264,47,0xFFB5BAC0);
        g.drawCenteredString(font,"RESERVED",264,93,0xFF70777E);

        boolean ready=menu.hasValidRecipe();
        g.drawCenteredString(font,ready ? "RECIPE READY" : status,160,125,ready?READY:WARN);
        g.drawCenteredString(font,"INVENTORY",160,166,MUTED);
    }

    @Override public void render(GuiGraphics g,int mx,int my,float pt){
        renderBackground(g,mx,my,pt); super.render(g,mx,my,pt); renderTooltip(g,mx,my);
    }
}
