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
    private String status="";
    private static final int BG=0xFF20252B,PANEL=0xFF171B20,PANEL2=0xFF292F36;
    private static final int STEEL_DARK=0xFF0B0E12,STEEL=0xFF3A424B,STEEL_LIGHT=0xFF59636E;
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
        forgedFrame(g,x,y,304,264);
        metalPlate(g,x+57,y+4,190,25);
        g.fill(x+18,y+16,x+57,y+18,STEEL); g.fill(x+247,y+16,x+286,y+18,STEEL);
        g.fill(x+18,y+19,x+57,y+20,COPPER); g.fill(x+247,y+19,x+286,y+20,COPPER);

        // Open anvil assembly surface.
        g.fill(x+22,y+51,x+282,y+116,PANEL);
        g.fill(x+24,y+53,x+280,y+114,0xFF1D2228);
        slot(g,x+67,y+66,0xFFE08A42); slot(g,x+142,y+66,0xFF64B5E8); slot(g,x+217,y+66,0xFFA56BCB);

        // Connector lines converge into one downward arrow.
        g.fill(x+77,y+91,x+78,y+101,COPPER);
        g.fill(x+152,y+91,x+153,y+101,COPPER);
        g.fill(x+227,y+91,x+228,y+101,COPPER);
        g.fill(x+77,y+100,x+228,y+101,COPPER);
        g.fill(x+152,y+100,x+153,y+113,COPPER);
        g.fill(x+149,y+113,x+156,y+115,COPPER);
        g.fill(x+150,y+115,x+155,y+117,COPPER);
        g.fill(x+151,y+117,x+154,y+119,COPPER);
        g.fill(x+152,y+119,x+153,y+121,COPPER);

        g.fill(x+34,y+126,x+270,y+127,STEEL_LIGHT);
        g.fill(x+48,y+127,x+256,y+128,COPPER);
        g.fill(x+28,y+130,x+276,y+161,PANEL);

        g.fill(x+55,y+167,x+249,y+258,STEEL_DARK);
        g.fill(x+57,y+169,x+247,y+256,STEEL);
        g.fill(x+60,y+172,x+244,y+253,PANEL);
        for(int r=0;r<3;r++)for(int c=0;c<9;c++)invSlot(g,x+71+c*18,y+179+r*18);
        for(int c=0;c<9;c++)invSlot(g,x+71+c*18,y+237);
    }
    private void forgedFrame(GuiGraphics g,int x,int y,int w,int h){
        g.fill(x,y,x+w,y+h,STEEL_DARK);
        g.fill(x+3,y+3,x+w-3,y+h-3,STEEL_LIGHT);
        g.fill(x+6,y+6,x+w-6,y+h-6,STEEL);
        g.fill(x+8,y+8,x+w-8,y+h-8,COPPER);
        g.fill(x+10,y+10,x+w-10,y+h-10,BG);
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
        g.fill(x,y,x+w,y+h,0xFF0F1216);g.fill(x+1,y+1,x+w-1,y+h-1,accent);g.fill(x+3,y+3,x+w-3,y+h-3,PANEL2);
    }
    private void slot(GuiGraphics g,int x,int y,int accent){
        g.fill(x,y,x+20,y+20,0xFF090B0E);g.fill(x+1,y+1,x+19,y+19,accent);g.fill(x+3,y+3,x+17,y+17,0xFF242A31);
    }
    private void invSlot(GuiGraphics g,int x,int y){
        g.fill(x,y,x+20,y+20,0xFF0D1014);g.fill(x+1,y+1,x+19,y+19,0xFF3B424B);g.fill(x+3,y+3,x+17,y+17,0xFF272D34);
    }

    @Override protected void renderLabels(GuiGraphics g,int mx,int my){
        g.drawCenteredString(font,"FINAL ASSEMBLY",152,11,TEXT);
        g.drawCenteredString(font,"HEAD",77,47,0xFFFFB56B);
        g.drawCenteredString(font,"CORE",152,47,0xFF8CCBFF);
        g.drawCenteredString(font,"ROD",227,47,0xFFD0A4FF);
        g.drawCenteredString(font,"+",114,69,0xFFFFC27A); g.drawCenteredString(font,"+",189,69,0xFFFFC27A);
        boolean ready=menu.hasValidAssembly();
        if (ready) g.drawCenteredString(font,"READY TO FORGE",152,125,READY);
        else if (!status.isEmpty()) g.drawCenteredString(font,status,152,125,WARN);
        g.drawCenteredString(font,"INVENTORY",152,167,MUTED);
    }

    @Override public void render(GuiGraphics g,int mx,int my,float pt){
        renderBackground(g,mx,my,pt);super.render(g,mx,my,pt);renderTooltip(g,mx,my);
    }
}
