package com.example.examplemod;

import com.example.examplemod.block.*;
import com.example.examplemod.entity.*;
import com.example.examplemod.item.*;
import com.example.examplemod.skill.*;
import com.example.examplemod.skill.client.*;
import com.example.examplemod.skill.blessing.ForgedBlessing;
import com.example.examplemod.skill.curse.ForgedCurse;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Displays the final randomized/merged effects after Rhythm Forging. */
public class AnvilForgingResultScreen extends Screen {
    private final ForgingResult result;
    private final AnvilAssemblyResult assembly;
    private final ForgedBlessing blessing;
    private final ForgedCurse curse;
    private final String performanceRank;
    private final double blessingChance;

    public AnvilForgingResultScreen(ForgingResult result, AnvilAssemblyResult assembly) {
        this(result, assembly, null, null, "-", 0.0D);
    }

    public AnvilForgingResultScreen(ForgingResult result, AnvilAssemblyResult assembly,
            ForgedBlessing blessing, ForgedCurse curse, String performanceRank, double blessingChance) {
        super(Component.literal("Final Forging Result"));
        this.result = result;
        this.assembly = assembly;
        this.blessing = blessing;
        this.curse = curse;
        this.performanceRank = performanceRank;
        this.blessingChance = blessingChance;
    }

    @Override protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Continue"), b -> { if (minecraft != null) minecraft.setScreen(null); })
                .bounds(width/2-50,(height-Math.min(248,height-10))/2+Math.min(248,height-10)-27,100,20).build());
    }

    @Override public void renderBackground(GuiGraphics g,int x,int y,float p) {}
    @Override public void render(GuiGraphics g,int mx,int my,float p) {
        ForgingResultArt.Frame frame=ForgingResultArt.draw(g,font,width,height,"FINAL EQUIPMENT COMPLETE");
        int cx=frame.center(), top=frame.top();
        int durability=ForgedEquipmentItem.calculateDurability(assembly.blueprint(),assembly.headMetal(),assembly.coreMetal(),assembly.rodMetal());
        g.drawCenteredString(font,format(assembly.blueprint().name())+" EQUIPMENT",cx,top+43,0xFF80D5E5);
        g.drawCenteredString(font,"DURABILITY  "+durability+" / "+durability,cx,top+61,ForgingResultArt.MUTED);
        g.drawCenteredString(font,"FORGED EFFECTS",cx,top+75,ForgingResultArt.COPPER);
        int y=top+96;
        for(AnvilAssemblyResult.FinalEffect effect:assembly.effects()){
            g.drawCenteredString(font,effect.effect().displayName()+" "+effect.tier().name(),cx,y,ForgingResultArt.GREAT);y+=14;
        }
        g.drawCenteredString(font,"RANK  "+performanceRank,cx,top+141,0xFF80D5E5);
        if (blessing != null) {
            g.drawCenteredString(font,"BLESSING  "+blessing.displayName(),cx,top+155,ForgingResultArt.GOOD);
        } else if (curse != null) {
            g.drawCenteredString(font,"CURSE  "+curse.displayName(),cx,top+155,ForgingResultArt.MISS);
        }
        g.fill(frame.left()+20,top+173,frame.left()+frame.width()-20,top+174,ForgingResultArt.COPPER);
        int cell=(frame.width()-24)/3,start=frame.left()+12;
        String[] labels={"SCORE","ACCURACY","MAX COMBO"};
        String[] values={Integer.toString(result.score()),String.format("%.1f%%",result.accuracy()),"x"+result.maxCombo()};
        for(int i=0;i<3;i++){
            int x=start+i*cell+cell/2;
            g.drawCenteredString(font,labels[i],x,top+181,ForgingResultArt.MUTED);
            g.drawCenteredString(font,values[i],x,top+197,i==0?ForgingResultArt.GOOD:ForgingResultArt.TEXT);
        }
        super.render(g,mx,my,p);
    }
    private static String format(String value){String[] words=value.toLowerCase().split("_");StringBuilder b=new StringBuilder();for(String w:words){if(!b.isEmpty())b.append(' ');b.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));}return b.toString();}
    @Override public boolean isPauseScreen(){return false;}
}
