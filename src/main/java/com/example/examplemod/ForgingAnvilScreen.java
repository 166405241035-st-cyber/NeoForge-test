package com.example.examplemod;

import java.util.Random;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Simple Head + Core + Rod GUI matching the approved mockup. */
public class ForgingAnvilScreen extends AbstractContainerScreen<AnvilMenu> {
    private String status = "Insert Head + Core + Rod";

    public ForgingAnvilScreen(AnvilMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth=176;
        imageHeight=178;
        inventoryLabelY=85;
    }

    @Override protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("FORGE"), b -> startForge())
                .bounds(leftPos+63,topPos+66,50,20).build());
    }

    private void startForge() {
        if (!menu.hasValidAssembly()) { status="Need Head + Core + Rod"; return; }
        ForgedHeadResult head=menu.headResult();
        ForgedCoreResult core=menu.coreResult();
        ForgedRodResult rod=menu.rodResult();
        if (head==null||core==null||rod==null) return;
        AnvilAssemblyResult assembly=AnvilAssemblyResult.roll(head,core,rod,new Random());
        if (minecraft==null || minecraft.gameMode==null) return;
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0);
        minecraft.setScreen(new AnvilRhythmForgingScreen(head.metal(),assembly));
    }

    @Override protected void renderBg(GuiGraphics g,float p,int mx,int my) {
        int x=leftPos,y=topPos;
        g.fill(x,y,x+imageWidth,y+imageHeight,0xFFE0E0E0);
        g.fill(x+3,y+3,x+imageWidth-3,y+imageHeight-3,0xFFC8C8C8);
        drawSlot(g,x+49,y+41); drawSlot(g,x+87,y+41); drawSlot(g,x+125,y+41);
        g.drawCenteredString(font,"+",x+78,y+47,0xFF666666);
        g.drawCenteredString(font,"+",x+116,y+47,0xFF666666);
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)drawSlot(g,x+7+col*18,y+95+row*18);
        for(int col=0;col<9;col++)drawSlot(g,x+7+col*18,y+153);
    }
    private void drawSlot(GuiGraphics g,int x,int y){g.fill(x,y,x+20,y+20,0xFF666666);g.fill(x+1,y+1,x+19,y+19,0xFFEEEEEE);g.fill(x+3,y+3,x+17,y+17,0xFF999999);}

    @Override protected void renderLabels(GuiGraphics g,int mx,int my){g.drawString(font,"FORGING ANVIL",8,8,0xFF333333,false);g.drawCenteredString(font,status,imageWidth/2,28,menu.hasValidAssembly()?0xFF228822:0xFF555555);g.drawString(font,playerInventoryTitle,inventoryLabelX,inventoryLabelY,0xFF333333,false);}
    @Override public void render(GuiGraphics g,int mx,int my,float p){renderBackground(g,mx,my,p);super.render(g,mx,my,p);renderTooltip(g,mx,my);}
}
