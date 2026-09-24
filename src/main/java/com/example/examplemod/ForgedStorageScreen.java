package com.example.examplemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ForgedStorageScreen extends AbstractContainerScreen<ForgedStorageMenu> {
    public ForgedStorageScreen(ForgedStorageMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        int rows=(menu.slots.size()-36+8)/9;
        this.imageHeight=114+rows*18;
        this.inventoryLabelY=this.imageHeight-94;
    }
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        int x=leftPos,y=topPos;
        g.fill(x,y,x+imageWidth,y+imageHeight,0xFF303030);
        int storageSlots=menu.slots.size()-36;
        for(int i=0;i<storageSlots;i++){
            int sx=x+7+(i%9)*18, sy=y+17+(i/9)*18;
            g.fill(sx,sy,sx+18,sy+18,0xFF101010);
        }
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        renderBackground(g,mx,my,partial); super.render(g,mx,my,partial); renderTooltip(g,mx,my);
    }
}
