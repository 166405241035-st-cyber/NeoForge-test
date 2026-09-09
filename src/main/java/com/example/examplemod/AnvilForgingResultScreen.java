package com.example.examplemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Displays the final randomized/merged effects after Rhythm Forging. */
public class AnvilForgingResultScreen extends Screen {
    private final ForgingResult result;
    private final AnvilAssemblyResult assembly;

    public AnvilForgingResultScreen(ForgingResult result, AnvilAssemblyResult assembly) {
        super(Component.literal("Final Forging Result"));
        this.result = result;
        this.assembly = assembly;
    }

    @Override protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Continue"), b -> { if (minecraft != null) minecraft.setScreen(null); })
                .bounds(width/2-50,height/2+100,100,20).build());
    }

    @Override public void renderBackground(GuiGraphics g,int x,int y,float p) {}
    @Override public void render(GuiGraphics g,int mx,int my,float p) {
        int cx=width/2, top=height/2-125;
        g.fill(cx-165,top,cx+165,height/2+135,0xD0000000);
        g.drawCenteredString(font,"FINAL EQUIPMENT COMPLETE",cx,top+14,0xFFFFFF);
        g.drawCenteredString(font,assembly.metal().displayName()+" "+format(assembly.blueprint().name()),cx,top+32,0x55FFFF);
        int y=top+54;
        for(AnvilAssemblyResult.FinalEffect effect:assembly.effects()){
            g.drawCenteredString(font,effect.effect().displayName()+" "+effect.tier().name(),cx,y,0x55FF55);y+=16;
        }
        y=Math.max(y+8,top+110);
        g.drawCenteredString(font,"SCORE: "+result.score(),cx,y,0xFFFF55);
        g.drawCenteredString(font,String.format("ACCURACY: %.1f%%",result.accuracy()),cx,y+18,0xFFFFFF);
        g.drawCenteredString(font,"MAX COMBO: x"+result.maxCombo(),cx,y+36,0xFFFFFF);
        super.render(g,mx,my,p);
    }
    private static String format(String value){String[] words=value.toLowerCase().split("_");StringBuilder b=new StringBuilder();for(String w:words){if(!b.isEmpty())b.append(' ');b.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));}return b.toString();}
    @Override public boolean isPauseScreen(){return false;}
}
