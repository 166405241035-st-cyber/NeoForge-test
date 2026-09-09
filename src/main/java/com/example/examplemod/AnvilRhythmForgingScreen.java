package com.example.examplemod;

import java.util.Random;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Rhythm assembly minigame for Head + Core + Rod. */
public class AnvilRhythmForgingScreen extends Screen {
    private static final int TOTAL_ROUNDS = 10;
    private static final int TARGET_RADIUS = 22;
    private static final int APPROACH_START_RADIUS = 70;
    private static final float APPROACH_SPEED = 2.2F;
    private static final float SPEED_PER_DIFFICULTY_STEP = 0.45F;
    private static final float PERFECT_WINDOW = 5.0F, GREAT_WINDOW = 12.0F, GOOD_WINDOW = 22.0F;
    private final Random random = new Random();
    private final ForgingMetal metal;
    private final AnvilAssemblyResult assembly;
    private int targetX, targetY, round, score, currentCombo, maxCombo, perfectCount, greatCount, goodCount, missCount, weightedAccuracyPoints;
    private Direction direction;
    private float approachRadius;
    private boolean finished;
    private String resultText = "Press the shown W/A/S/D key at the right time";
    private int resultColor = 0xFFFFFF;

    public AnvilRhythmForgingScreen(ForgingMetal metal, AnvilAssemblyResult assembly) {
        super(Component.literal("Final Rhythm Forging"));
        this.metal = metal;
        this.assembly = assembly;
    }

    private float speed() { return APPROACH_SPEED + (metal.difficulty() - 1) * SPEED_PER_DIFFICULTY_STEP; }
    private float perfectWindow() { return Math.max(2F, PERFECT_WINDOW - (metal.difficulty() - 1) * .75F); }
    private float greatWindow() { return Math.max(perfectWindow()+1F, GREAT_WINDOW - (metal.difficulty()-1)*2F); }
    private float goodWindow() { return Math.max(greatWindow()+1F, GOOD_WINDOW - (metal.difficulty()-1)*2F); }

    @Override protected void init() { spawnTarget(); }
    private void spawnTarget() {
        int mx=90, tm=80, bm=80;
        targetX=mx+random.nextInt(Math.max(1,width-mx*2));
        targetY=tm+random.nextInt(Math.max(1,height-tm-bm));
        direction=Direction.values()[random.nextInt(Direction.values().length)];
        approachRadius=APPROACH_START_RADIUS;
    }
    @Override public void tick(){ if(!finished){ approachRadius-=speed(); if(approachRadius<TARGET_RADIUS-goodWindow()) miss("TOO LATE!"); } }
    private void attempt(int key){
        if(finished)return; Direction pressed=Direction.fromKey(key); if(pressed==null)return;
        if(pressed!=direction){miss("WRONG KEY!");return;}
        float d=Math.abs(approachRadius-TARGET_RADIUS);
        if(d<=perfectWindow())hit("PERFECT!",0xFF66FF66,100,100,0);
        else if(d<=greatWindow())hit("GREAT!",0xFF22CC55,75,75,1);
        else if(d<=goodWindow())hit("GOOD!",0xFFFFCC33,50,50,2);
        else miss("TOO EARLY!");
    }
    private void hit(String text,int color,int base,int accuracy,int grade){ resultText=text+" +"+base;resultColor=color;if(grade==0)perfectCount++;else if(grade==1)greatCount++;else goodCount++;currentCombo++;maxCombo=Math.max(maxCombo,currentCombo);score+=base+Math.max(0,currentCombo-1)*5;weightedAccuracyPoints+=accuracy;next(); }
    private void miss(String why){resultText=why+" MISS!";resultColor=0xFFFF5555;missCount++;currentCombo=0;next();}
    private void next(){round++;if(round>=TOTAL_ROUNDS){finished=true;finish();}else spawnTarget();}
    private void finish(){if(minecraft==null)return;double accuracy=weightedAccuracyPoints/(double)TOTAL_ROUNDS;ForgingResult result=new ForgingResult(score,accuracy,maxCombo,perfectCount,greatCount,goodCount,missCount);minecraft.setScreen(new AnvilForgingResultScreen(result,assembly));}
    @Override public boolean keyPressed(int keyCode,int scanCode,int modifiers){if(Direction.fromKey(keyCode)!=null){attempt(keyCode);return true;}return super.keyPressed(keyCode,scanCode,modifiers);}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float p){}
    @Override public void render(GuiGraphics g,int mx,int my,float p){g.fill(0,0,width,height,0x88000000);g.fill(12,12,270,80,0xB0000000);g.drawString(font,"FINAL FORGING - "+metal.displayName(),22,22,0xFFFFFF);g.drawString(font,"Round: "+Math.min(round+1,TOTAL_ROUNDS)+"/"+TOTAL_ROUNDS,22,40,0xDDDDDD);g.drawString(font,"Score: "+score+" Combo: x"+currentCombo,22,58,0xDDDDDD);g.drawCenteredString(font,resultText,width/2,22,resultColor);drawTarget(g);g.drawCenteredString(font,"W=UP  A=LEFT  S=DOWN  D=RIGHT",width/2,height-24,0xFFFFFF);super.render(g,mx,my,p);}
    private void drawTarget(GuiGraphics g){int r=TARGET_RADIUS;g.fill(targetX-r,targetY-r,targetX+r,targetY+r,0xCC222222);g.fill(targetX-r+3,targetY-r+3,targetX+r-3,targetY+r-3,0xCCEEEEEE);g.fill(targetX-r+6,targetY-r+6,targetX+r-6,targetY+r-6,0xCC333333);int ar=Math.max(1,Math.round(approachRadius));g.fill(targetX-ar,targetY-ar,targetX+ar,targetY-ar+2,0xFFFFFFFF);g.fill(targetX-ar,targetY+ar-2,targetX+ar,targetY+ar,0xFFFFFFFF);g.fill(targetX-ar,targetY-ar,targetX-ar+2,targetY+ar,0xFFFFFFFF);g.fill(targetX+ar-2,targetY-ar,targetX+ar,targetY+ar,0xFFFFFFFF);g.drawCenteredString(font,direction.symbol+" "+direction.key,targetX,targetY-4,0xFFFFFF);}
    @Override public void onClose(){if(minecraft!=null)minecraft.setScreen(null);}
    @Override public boolean isPauseScreen(){return false;}
    private enum Direction{UP(GLFW.GLFW_KEY_W,"W","^"),LEFT(GLFW.GLFW_KEY_A,"A","<"),DOWN(GLFW.GLFW_KEY_S,"S","v"),RIGHT(GLFW.GLFW_KEY_D,"D",">");final int code;final String key,symbol;Direction(int c,String k,String s){code=c;key=k;symbol=s;}static Direction fromKey(int k){for(Direction d:values())if(d.code==k)return d;return null;}}
}
