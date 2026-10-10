package com.example.examplemod.debug.client;

import com.example.examplemod.debug.menu.BlessingCurseTestMenu;
import com.example.examplemod.forging.blueprint.HeadBlueprintType;
import com.example.examplemod.forging.material.ForgingMetal;
import com.example.examplemod.skill.EffectTier;
import com.example.examplemod.skill.ForgingEffect;
import com.example.examplemod.skill.blessing.ForgedBlessing;
import com.example.examplemod.skill.curse.ForgedCurse;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BlessingCurseTestScreen extends AbstractContainerScreen<BlessingCurseTestMenu> {
    private HeadBlueprintType blueprint=HeadBlueprintType.SWORD;
    private ForgingMetal metal=ForgingMetal.IRON;
    private ForgingEffect effect=ForgingEffect.WITHER_CURSE_POWER;
    private EffectTier tier=EffectTier.III;
    private int specialType=BlessingCurseTestMenu.CURSE;
    private int specialIndex=ForgedCurse.POWER_ERASURE.ordinal();
    private Button typeButton,metalButton,effectButton,tierButton,kindButton,specialButton;
    private String status="Choose values, then create";

    public BlessingCurseTestScreen(BlessingCurseTestMenu menu, Inventory inventory, Component title) {
        super(menu,inventory,title); imageWidth=250; imageHeight=224; inventoryLabelY=130;
    }

    @Override protected void init() {
        super.init();
        typeButton=addRenderableWidget(Button.builder(typeText(),b->cycleType()).bounds(leftPos+10,topPos+25,110,20).build());
        metalButton=addRenderableWidget(Button.builder(metalText(),b->cycleMetal()).bounds(leftPos+130,topPos+25,110,20).build());
        effectButton=addRenderableWidget(Button.builder(effectText(),b->cycleEffect()).bounds(leftPos+10,topPos+51,180,20).build());
        tierButton=addRenderableWidget(Button.builder(tierText(),b->cycleTier()).bounds(leftPos+198,topPos+51,42,20).build());
        kindButton=addRenderableWidget(Button.builder(kindText(),b->cycleKind()).bounds(leftPos+10,topPos+77,80,20).build());
        specialButton=addRenderableWidget(Button.builder(specialText(),b->cycleSpecial()).bounds(leftPos+98,topPos+77,142,20).build());
        addRenderableWidget(Button.builder(Component.literal("CLEAR SPECIAL"),b->clearSpecial()).bounds(leftPos+10,topPos+103,105,20).build());
        addRenderableWidget(Button.builder(Component.literal("CREATE TEST ITEM"),b->createItem()).bounds(leftPos+125,topPos+103,115,20).build());
    }

    private void cycleType(){ blueprint=next(HeadBlueprintType.values(),blueprint.ordinal()); typeButton.setMessage(typeText()); }
    private void cycleMetal(){ metal=next(ForgingMetal.values(),metal.ordinal()); metalButton.setMessage(metalText()); }
    private void cycleEffect(){ effect=next(ForgingEffect.values(),effect.ordinal()); effectButton.setMessage(effectText()); }
    private void cycleTier(){ tier=next(EffectTier.values(),tier.ordinal()); tierButton.setMessage(tierText()); }
    private void cycleKind(){
        specialType=specialType==BlessingCurseTestMenu.BLESSING?BlessingCurseTestMenu.CURSE:BlessingCurseTestMenu.BLESSING;
        specialIndex=0; kindButton.setMessage(kindText()); specialButton.setMessage(specialText());
    }
    private void cycleSpecial(){
        if(specialType==BlessingCurseTestMenu.NONE) specialType=BlessingCurseTestMenu.BLESSING;
        int count=specialType==BlessingCurseTestMenu.BLESSING?ForgedBlessing.values().length:ForgedCurse.values().length;
        specialIndex=(specialIndex+1)%count; kindButton.setMessage(kindText()); specialButton.setMessage(specialText());
    }
    private void clearSpecial(){ specialType=BlessingCurseTestMenu.NONE; specialIndex=0; kindButton.setMessage(kindText()); specialButton.setMessage(specialText()); }
    private void createItem(){
        if(minecraft==null||minecraft.gameMode==null)return;
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                BlessingCurseTestMenu.encodeSelection(blueprint,metal,effect,tier,specialType,specialIndex));
        status="Test item created";
    }
    private Component typeText(){return Component.literal("Type: "+pretty(blueprint.name()));}
    private Component metalText(){return Component.literal("Metal: "+metal.displayName());}
    private Component effectText(){return Component.literal("Effect: "+effect.displayName());}
    private Component tierText(){return Component.literal(tier.name());}
    private Component kindText(){return Component.literal(specialType==0?"None":specialType==1?"Blessing":"Curse");}
    private Component specialText(){
        if(specialType==0)return Component.literal("-- No Special --");
        return Component.literal(specialType==1?ForgedBlessing.values()[specialIndex].displayName():ForgedCurse.values()[specialIndex].displayName());
    }
    private static <T>T next(T[]v,int i){return v[(i+1)%v.length];}
    private static String pretty(String s){String l=s.toLowerCase();return Character.toUpperCase(l.charAt(0))+l.substring(1);}

    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        int x=leftPos,y=topPos;
        g.fill(x,y,x+imageWidth,y+imageHeight,0xFF21182B);
        g.fill(x+3,y+3,x+imageWidth-3,y+imageHeight-3,0xFF352441);
        g.fill(x+8,y+20,x+imageWidth-8,y+22,0xFFD6B84A);
        g.fill(x+imageWidth/2,y+20,x+imageWidth-8,y+22,0xFF8E304B);
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)drawSlot(g,x+34+col*18,y+141+row*18);
        for(int col=0;col<9;col++)drawSlot(g,x+34+col*18,y+199);
    }
    private void drawSlot(GuiGraphics g,int x,int y){g.fill(x,y,x+20,y+20,0xFF17131C);g.fill(x+1,y+1,x+19,y+19,0xFF66536F);g.fill(x+3,y+3,x+17,y+17,0xFF29202F);}
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){
        g.drawCenteredString(font,"BLESSING & CURSE TEST FORGE",imageWidth/2,7,0xFFFFE69A);
        g.drawCenteredString(font,status,imageWidth/2,126,0xFFE0D5E8);
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial){renderBackground(g,mx,my,partial);super.render(g,mx,my,partial);renderTooltip(g,mx,my);}
}
