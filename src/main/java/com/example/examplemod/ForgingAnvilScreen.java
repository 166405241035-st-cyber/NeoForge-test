package com.example.examplemod;

import com.example.examplemod.block.*;
import com.example.examplemod.entity.*;
import com.example.examplemod.item.*;
import com.example.examplemod.skill.*;
import com.example.examplemod.skill.client.*;

import java.util.Random;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Concept-layout pre-Miniganme 2 screen. */
public class ForgingAnvilScreen extends AbstractContainerScreen<AnvilMenu> {
    private String status="Insert forged components";
    private static final int BG=0xFF20252B,PANEL=0xFF171B20,PANEL2=0xFF292F36;
    private static final int FRAME=0xFF6F3D20,COPPER=0xFFD27A34,TEXT=0xFFF4E9D8;
    private static final int MUTED=0xFFA8B0B8,READY=0xFF77DC5B,WARN=0xFFFFB24D;

    public ForgingAnvilScreen(AnvilMenu menu,Inventory inv,Component title){
        super(menu,inv,title); imageWidth=304; imageHeight=264; inventoryLabelX=72; inventoryLabelY=167;
    }

    @Override protected void init(){
        super.init();
        addRenderableWidget(Button.builder(Component.literal("FORGE"),b->startForge())
                .bounds(leftPos+112,topPos+137,80,20).build());
    }

    private void startForge(){
        if(!menu.hasValidAssembly()){status=missing();return;}
        ForgedHeadResult head=menu.headResult(); ForgedCoreResult core=menu.coreResult(); ForgedRodResult rod=menu.rodResult();
        if(head==null||core==null||rod==null)return;
        AnvilAssemblyResult assembly=AnvilAssemblyResult.roll(head,core,rod,new Random());
        if(minecraft==null||minecraft.gameMode==null)return;
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0);
        minecraft.setScreen(new AnvilRhythmForgingScreen(head.metal(),assembly));
    }
    private String missing(){
        if(menu.stackAt(AnvilMenu.HEAD_SLOT).isEmpty())return "Insert Head";
        if(menu.stackAt(AnvilMenu.CORE_SLOT).isEmpty())return "Insert Core";
        if(menu.stackAt(AnvilMenu.ROD_SLOT).isEmpty())return "Insert Rod";
        return "Use Forged Head + Core + Rod";
    }

    @Override protected void renderBg(GuiGraphics g,float pt,int mx,int my){
        int x=leftPos,y=topPos;
        g.fill(x,y,x+304,y+264,0xFF101317); g.fill(x+2,y+2,x+302,y+262,FRAME); g.fill(x+6,y+6,x+298,y+258,BG);
        g.fill(x+12,y+10,x+292,y+42,PANEL); g.fill(x+12,y+42,x+292,y+45,COPPER);

        // component assembly chamber
        g.fill(x+22,y+51,x+282,y+119,PANEL);
        slot(g,x+75,y+66,0xFFE08A42); slot(g,x+150,y+66,0xFF64B5E8); slot(g,x+225,y+66,0xFFA56BCB);

        // connector lines toward final equipment
        g.fill(x+85,y+104,x+85+1,y+112,COPPER);
        g.fill(x+160,y+104,x+160+1,y+112,COPPER);
        g.fill(x+235,y+104,x+235+1,y+112,COPPER);
        g.fill(x+85,y+111,x+236,y+112,COPPER);
        g.fill(x+160,y+111,x+161,y+119,COPPER);

        g.fill(x+22,y+124,x+282,y+163,PANEL); g.fill(x+22,y+162,x+282,y+165,FRAME);

        g.fill(x+59,y+170,x+245,y+257,PANEL);
        for(int r=0;r<3;r++)for(int c=0;c<9;c++)invSlot(g,x+71+c*18,y+179+r*18);
        for(int c=0;c<9;c++)invSlot(g,x+71+c*18,y+237);
    }
    private void card(GuiGraphics g,int x,int y,int w,int h,int accent){
        g.fill(x,y,x+w,y+h,0xFF0F1216);g.fill(x+1,y+1,x+w-1,y+h-1,accent);g.fill(x+3,y+3,x+w-3,y+h-3,PANEL2);
    }
    private void slot(GuiGraphics g,int x,int y,int accent){
        g.fill(x,y,x+20,y+20,0xFF090B0E);g.fill(x+1,y+1,x+19,y+19,accent);g.fill(x+3,y+3,x+17,y+17,0xFF242A31);
    }
    private void invSlot(GuiGraphics g,int x,int y){
        g.fill(x,y,x+20,y+20,0xFF0D1014);g.fill(x+1,y+1,x+19,y+19,0xFF3B424B);g.fill(x+3,y+3,x+17,y+17,0xFF272D34);
    }

    @Override protected void renderLabels(GuiGraphics g,int mx,int my){
        g.drawCenteredString(font,"FINAL ASSEMBLY",152,14,TEXT);
        g.drawCenteredString(font,"Combine your forged components",152,28,MUTED);
        g.drawCenteredString(font,"HEAD",85,47,0xFFFFB56B);
        g.drawCenteredString(font,"CORE",160,47,0xFF8CCBFF);
        g.drawCenteredString(font,"ROD",235,47,0xFFD0A4FF);
        g.drawCenteredString(font,"+",122,69,0xFFFFC27A); g.drawCenteredString(font,"+",197,69,0xFFFFC27A);
        g.drawCenteredString(font,"FINAL EQUIPMENT",160,111,TEXT);
        boolean ready=menu.hasValidAssembly();
        g.drawCenteredString(font,ready?"READY TO FORGE":status,152,125,ready?READY:WARN);
        g.drawCenteredString(font,"INVENTORY",152,167,MUTED);
    }

    @Override public void render(GuiGraphics g,int mx,int my,float pt){
        renderBackground(g,mx,my,pt);super.render(g,mx,my,pt);renderTooltip(g,mx,my);
    }
}
