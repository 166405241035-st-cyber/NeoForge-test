package com.example.examplemod;

import com.example.examplemod.block.*;
import com.example.examplemod.entity.*;
import com.example.examplemod.item.*;
import com.example.examplemod.skill.*;
import com.example.examplemod.skill.client.*;
import com.example.examplemod.skill.blessing.*;
import com.example.examplemod.skill.curse.*;

import java.util.Random;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

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
    private float previousApproachRadius;
    private int feedbackTicks;
    private boolean lastHit;
    private int visualTicks;
    private final int[] roundGrades = new int[TOTAL_ROUNDS];
    private boolean finished;
    private ForgedBlessing rolledBlessing;
    private ForgedCurse rolledCurse;
    private double finalBlessingChance;
    private String performanceRank = "D";
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
    private void spawnTarget() { targetX=width/2;targetY=height/2+12;direction=Direction.values()[random.nextInt(Direction.values().length)];approachRadius=APPROACH_START_RADIUS;previousApproachRadius=approachRadius; }
    @Override public void tick(){visualTicks++;if(finished)return;if(feedbackTicks>0){if(--feedbackTicks==0){if(round>=TOTAL_ROUNDS){finished=true;MinigameFeedback.complete();finish();}else spawnTarget();}return;}previousApproachRadius=approachRadius;approachRadius-=speed();if(approachRadius<TARGET_RADIUS-goodWindow())miss("TOO LATE!");}
    private void attempt(int key){if(finished||feedbackTicks>0)return;Direction pressed=Direction.fromKey(key);if(pressed==null)return;if(pressed!=direction){miss("WRONG KEY!");return;}float d=Math.abs(approachRadius-TARGET_RADIUS);if(d<=perfectWindow())hit("PERFECT!",0xFF66FF66,100,100,0);else if(d<=greatWindow())hit("GREAT!",0xFF22CC55,75,75,1);else if(d<=goodWindow())hit("GOOD!",0xFFFFCC33,50,50,2);else miss("TOO EARLY!");}
    private void hit(String text,int color,int base,int accuracy,int grade){resultText=text+" +"+base;resultColor=color;if(grade==0)perfectCount++;else if(grade==1)greatCount++;else goodCount++;currentCombo++;maxCombo=Math.max(maxCombo,currentCombo);score+=base+Math.max(0,currentCombo-1)*5;weightedAccuracyPoints+=accuracy;roundGrades[round]=3-grade;lastHit=true;MinigameFeedback.hit(grade);next();}
    private void miss(String why){resultText=why+" MISS!";resultColor=0xFFFF5555;missCount++;currentCombo=0;roundGrades[round]=0;lastHit=false;MinigameFeedback.miss();next();}
    private void next(){round++;feedbackTicks=10;}

    private void finish(){
        if(minecraft==null)return;
        double accuracy=weightedAccuracyPoints/(double)TOTAL_ROUNDS;
        ForgingResult result=new ForgingResult(score,accuracy,maxCombo,perfectCount,greatCount,goodCount,missCount);
        if(minecraft.player!=null){
            ItemStack equipment=ExampleMod.FORGED_EQUIPMENT_ITEM.get().createStack(assembly);

            // Minigame 2 reward uses a visible performance rank as the base chance.
            // Misses penalize the chance, while strong Perfect play and a full combo
            // give small bonuses. Curse always keeps at least a 5% chance.
            double blessingChance;
            if (accuracy >= 90.0D && maxCombo >= 8) {
                performanceRank = "S";
                blessingChance = 0.90D;
            } else if (accuracy >= 80.0D) {
                performanceRank = "A";
                blessingChance = 0.75D;
            } else if (accuracy >= 65.0D) {
                performanceRank = "B";
                blessingChance = 0.60D;
            } else if (accuracy >= 50.0D) {
                performanceRank = "C";
                blessingChance = 0.45D;
            } else {
                performanceRank = "D";
                blessingChance = 0.10D;
            }

            blessingChance -= missCount * 0.05D;
            if (perfectCount >= 7) blessingChance += 0.05D;
            if (maxCombo >= TOTAL_ROUNDS) blessingChance += 0.10D;
            finalBlessingChance = Math.max(0.05D, Math.min(0.95D, blessingChance));

            if (random.nextDouble() < finalBlessingChance) {
                // Double Trigger is only useful when at least one main effect on this
                // forged item has a meaningful replay/result/effect rule. Do not award
                // a dead blessing to an item made entirely from passive, storage,
                // toggle-only, or Mob Swap effects.
                boolean supportsDoubleTrigger = assembly.effects().stream()
                        .anyMatch(effect -> DoubleTriggerRuntime.supports(effect.effect()));

                java.util.List<ForgedBlessing> pool = new java.util.ArrayList<>();
                for (ForgedBlessing blessing : ForgedBlessing.values()) {
                    if (blessing != ForgedBlessing.DOUBLE_TRIGGER || supportsDoubleTrigger) {
                        pool.add(blessing);
                    }
                }

                rolledBlessing = pool.get(random.nextInt(pool.size()));
                ForgedBlessingRuntime.set(equipment, rolledBlessing);
            } else {
                ForgedCurse[] pool = ForgedCurse.values();
                rolledCurse = pool[random.nextInt(pool.length)];
                ForgedCurseRuntime.set(equipment, rolledCurse);
            }

            ForgedEffectNetwork.sendForgingReward(equipment);
        }
        minecraft.setScreen(new AnvilForgingResultScreen(result,assembly,rolledBlessing,rolledCurse,performanceRank,finalBlessingChance));
    }

    @Override public boolean keyPressed(int keyCode,int scanCode,int modifiers){if(Direction.fromKey(keyCode)!=null){attempt(keyCode);return true;}return super.keyPressed(keyCode,scanCode,modifiers);}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float p){}
    @Override public void render(GuiGraphics g,int mx,int my,float p){
        g.fill(0,0,width,height,0x88000000);
        int left=width/2-155,right=width/2+155;
        g.fill(left,8,right,78,0xDD171B20);
        g.fill(left,8,right,10,0xFFD27A34);
        g.drawCenteredString(font,"FINAL FORGING - "+metal.displayName(),width/2,16,0xFFFFFF);
        g.drawCenteredString(font,"Round: "+Math.min(round+1,TOTAL_ROUNDS)+"/"+TOTAL_ROUNDS+"   Score: "+score+"   Combo: x"+currentCombo,width/2,35,0xDDDDDD);
        g.drawCenteredString(font,resultText,width/2,56,resultColor);
        ForgingMinigameArt.progress(g,width/2,84,roundGrades,round);
        drawTarget(g,p);
        g.drawCenteredString(font,"W=UP  A=LEFT  S=DOWN  D=RIGHT",width/2,height-24,0xFFFFFF);
        super.render(g,mx,my,p);
    }
    private void drawTarget(GuiGraphics g,float partialTick){
        int r=TARGET_RADIUS;
        // Anvil body and hot workpiece behind the timing target.
        g.fill(targetX-42,targetY+32,targetX+42,targetY+38,0xFF59636E);
        g.fill(targetX-26,targetY+38,targetX+26,targetY+47,0xFF3A424B);
        g.fill(targetX-32,targetY+47,targetX+32,targetY+51,0xFF20252B);
        ForgingMinigameArt.backdrop(g,targetX-r-6,targetY-r-6,2*r+12,2*r+12,visualTicks);
        g.fill(targetX-r,targetY-r,targetX+r,targetY+r,0xFF39424B);
        g.fill(targetX-r+3,targetY-r+3,targetX+r-3,targetY+r-3,0xFF171B20);
        g.fill(targetX-11,targetY-4,targetX+11,targetY+5,0xFFB85426);
        g.fill(targetX-7,targetY-3,targetX+7,targetY-1,0xFFFFAA4C);
        float displayedRadius=feedbackTicks>0?approachRadius:previousApproachRadius+(approachRadius-previousApproachRadius)*partialTick;
        int ar=Math.max(1,Math.round(displayedRadius));
        int color=feedbackTicks>0?(lastHit?resultColor:0xFFFF5555):0xFFFFD28A;
        int hammerX=targetX,hammerY=targetY;
        switch(direction){
            case UP -> hammerY-=ar;
            case DOWN -> hammerY+=ar;
            case LEFT -> hammerX-=ar;
            case RIGHT -> hammerX+=ar;
        }
        g.fill(hammerX-9,hammerY-5,hammerX+9,hammerY+5,0xFF0B0E12);
        g.fill(hammerX-7,hammerY-3,hammerX+7,hammerY+3,color);
        g.drawCenteredString(font,direction.symbol+" "+direction.key,targetX,targetY-17,0xFFFFFF);
        if(feedbackTicks>0&&lastHit)ForgingMinigameArt.sparks(g,targetX,targetY,feedbackTicks,0xFFFFC45E);
    }
    @Override public void onClose(){if(minecraft!=null)minecraft.setScreen(null);}
    @Override public boolean isPauseScreen(){return false;}
    private enum Direction{UP(GLFW.GLFW_KEY_W,"W","^"),LEFT(GLFW.GLFW_KEY_A,"A","<"),DOWN(GLFW.GLFW_KEY_S,"S","v"),RIGHT(GLFW.GLFW_KEY_D,"D",">");final int code;final String key,symbol;Direction(int c,String k,String s){code=c;key=k;symbol=s;}static Direction fromKey(int k){for(Direction d:values())if(d.code==k)return d;return null;}}
}
