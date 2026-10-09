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
    private static final int TARGET_RADIUS = 22;
    private static final int APPROACH_START_RADIUS = 70;
    private static final float APPROACH_SPEED = 2.2F;
    private static final float SPEED_PER_DIFFICULTY_STEP = 0.45F;
    private static final float PERFECT_WINDOW = 5.0F, GREAT_WINDOW = 12.0F, GOOD_WINDOW = 22.0F;
    private final Random random = new Random();
    private final float difficulty;
    private final AnvilAssemblyResult assembly;
    private int targetX, targetY, round, score, currentCombo, maxCombo, perfectCount, greatCount, goodCount, missCount, weightedAccuracyPoints;
    private Direction direction;
    private float approachRadius;
    private float previousApproachRadius;
    private int feedbackTicks;
    private boolean lastHit;
    private final int totalRounds;
    private final int[] roundGrades;
    private boolean finished;
    private int tutorialStep; // 0: explanation, 1: approach, 2: wait for the first correct key, 3: normal play
    private ForgingResult finalResult;
    private String resultText = "Press the shown W/A/S/D key at the right time";
    private int resultColor = 0xFFFFFF;

    public AnvilRhythmForgingScreen(AnvilAssemblyResult assembly) {
        super(Component.literal("Final Rhythm Forging"));
        this.difficulty = assembly.rhythmDifficulty();
        this.totalRounds = ForgingRounds.forDifficulty(difficulty);
        this.roundGrades = new int[totalRounds];
        this.assembly = assembly;
    }

    private float speed() { return APPROACH_SPEED + (difficulty - 1) * SPEED_PER_DIFFICULTY_STEP; }
    private float perfectWindow() { return Math.max(2F, PERFECT_WINDOW - (difficulty - 1) * .75F); }
    private float greatWindow() { return Math.max(perfectWindow()+1F, GREAT_WINDOW - (difficulty-1)*2F); }
    private float goodWindow() { return Math.max(greatWindow()+1F, GOOD_WINDOW - (difficulty-1)*2F); }

    @Override protected void init() {
        if (direction == null) spawnTarget();
    }
    private void spawnTarget() {
        targetX=width>210?105+random.nextInt(width-210):width/2;
        targetY=height>320?170+random.nextInt(height-270):height/2+25;
        if (tutorialStep < 3) { targetX = width / 2; targetY = height / 2; }
        direction=Direction.values()[random.nextInt(Direction.values().length)];
        approachRadius=APPROACH_START_RADIUS;
        previousApproachRadius=approachRadius;
    }
    @Override public void tick(){if(finished || tutorialStep == 0 || tutorialStep == 2)return;if(feedbackTicks>0){if(--feedbackTicks==0){if(round>=totalRounds){finished=true;MinigameFeedback.complete();finish();}else spawnTarget();}return;}previousApproachRadius=approachRadius;approachRadius-=speed();if(tutorialStep == 1 && approachRadius <= TARGET_RADIUS){approachRadius=TARGET_RADIUS;previousApproachRadius=TARGET_RADIUS;tutorialStep=2;return;}if(approachRadius<TARGET_RADIUS-goodWindow())miss("TOO LATE!");}
    private void attempt(int key){if(finished||feedbackTicks>0||tutorialStep<2)return;Direction pressed=Direction.fromKey(key);if(pressed==null)return;if(tutorialStep==2){if(pressed!=direction)return;tutorialStep=3;}if(pressed!=direction){miss("WRONG KEY!");return;}float d=Math.abs(approachRadius-TARGET_RADIUS);if(d<=perfectWindow())hit("PERFECT!",0xFF66FF66,100,100,0);else if(d<=greatWindow())hit("GREAT!",0xFF22CC55,75,75,1);else if(d<=goodWindow())hit("GOOD!",0xFFFFCC33,50,50,2);else miss("TOO EARLY!");}
    private void hit(String text,int color,int base,int accuracy,int grade){resultText=text+" +"+base;resultColor=color;if(grade==0)perfectCount++;else if(grade==1)greatCount++;else goodCount++;currentCombo++;maxCombo=Math.max(maxCombo,currentCombo);score+=base+Math.max(0,currentCombo-1)*5;weightedAccuracyPoints+=accuracy;roundGrades[round]=3-grade;lastHit=true;MinigameFeedback.hit(grade);next();}
    private void miss(String why){resultText=why+" MISS!";resultColor=0xFFFF5555;missCount++;currentCombo=0;roundGrades[round]=0;lastHit=false;MinigameFeedback.miss();next();}
    private void next(){round++;feedbackTicks=10;}

    private void finish() {
        if (minecraft == null) return;
        double accuracy = weightedAccuracyPoints / (double) totalRounds;
        finalResult = new ForgingResult(score, accuracy, maxCombo, perfectCount, greatCount, goodCount, missCount);
        resultText = "Finishing on server...";
        ForgedEffectNetwork.finishAnvil(finalResult);
    }

    public void showResult(ItemStack equipment, String rank, double chance) {
        if (minecraft == null || finalResult == null) return;
        minecraft.setScreen(new AnvilForgingResultScreen(finalResult, assembly,
                ForgedBlessingRuntime.get(equipment), ForgedCurseRuntime.get(equipment), rank, chance));
    }

    @Override public boolean keyPressed(int keyCode,int scanCode,int modifiers){if(tutorialStep==0 && (keyCode==GLFW.GLFW_KEY_ENTER || keyCode==GLFW.GLFW_KEY_SPACE)){tutorialStep=1;return true;}if(Direction.fromKey(keyCode)!=null){attempt(keyCode);return true;}return super.keyPressed(keyCode,scanCode,modifiers);}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float p){}
    @Override public void render(GuiGraphics g,int mx,int my,float p){
        g.fill(0,0,width,height,0x88000000);
        g.fill(0,78,width,height-36,0x9920252B);
        int left=width/2-155,right=width/2+155;
        g.fill(left,8,right,78,0xDD171B20);
        g.fill(left,8,right,10,0xFFD27A34);
        g.drawCenteredString(font,"FINAL FORGING - Difficulty "+String.format(java.util.Locale.ROOT,"%.2f",difficulty)+"/3",width/2,16,0xFFFFFF);
        g.drawCenteredString(font,"Round: "+Math.min(round+1,totalRounds)+"/"+totalRounds+"   Score: "+score+"   Combo: x"+currentCombo,width/2,35,0xDDDDDD);
        g.drawCenteredString(font,resultText,width/2,56,resultColor);
        ForgingMinigameArt.progress(g,width/2,85,roundGrades,round);
        drawTarget(g,p);
        g.drawCenteredString(font,"W=UP  A=LEFT  S=DOWN  D=RIGHT",width/2,height-24,0xFFFFFF);
        if (tutorialStep == 0 || tutorialStep == 2) drawTutorial(g);
        super.render(g,mx,my,p);
    }
    private void drawTutorial(GuiGraphics g) {
        int boxWidth = Math.min(300, width - 16);
        int left = (width - boxWidth) / 2;
        int top = tutorialStep == 0 ? Math.max(90, height / 2 - 42) : Math.max(8, height - 86);
        g.fill(left - 2, top - 2, left + boxWidth + 2, top + 66, 0xFFFFC45E);
        g.fill(left, top, left + boxWidth, top + 64, 0xF0171B20);
        g.drawCenteredString(font, "GUIDED TUTORIAL", width / 2, top + 8, 0xFFFFC45E);
        if (tutorialStep == 0) {
            g.drawCenteredString(font, "Match the shown W / A / S / D key.", width / 2, top + 23, 0xFFFFFFFF);
            g.drawCenteredString(font, "Press when the gold ring meets the target.", width / 2, top + 35, 0xFFFFFFFF);
            g.drawCenteredString(font, "Press ENTER or SPACE to begin.", width / 2, top + 49, 0xFF9BE178);
        } else {
            g.drawCenteredString(font, "The rings match now. Press " + direction.key + "!", width / 2, top + 23, 0xFFFFFFFF);
            g.drawCenteredString(font, "This first target waits for the correct key.", width / 2, top + 35, 0xFFFFFFFF);
            g.drawCenteredString(font, "The remaining targets move normally.", width / 2, top + 49, 0xFF9BE178);
        }
    }

    private void drawTarget(GuiGraphics g,float partialTick){
        // Rhythm note: a shrinking approach circle around a randomly placed key target.
        int tint=switch(direction){case UP->0xFF76C6F7;case LEFT->0xFFFFB66B;case DOWN->0xFF9BE178;case RIGHT->0xFFC8A1F5;};
        ForgingMinigameArt.ring(g,targetX,targetY,TARGET_RADIUS+5,3,0xFF0B0E12);
        ForgingMinigameArt.ring(g,targetX,targetY,TARGET_RADIUS+2,3,tint);
        ForgingMinigameArt.ring(g,targetX,targetY,TARGET_RADIUS-2,2,0xFF59636E);
        g.fill(targetX-14,targetY-8,targetX+14,targetY+9,0xFF171B20);
        g.drawCenteredString(font,direction.key,targetX,targetY-4,0xFFFFFFFF);
        float displayedRadius=feedbackTicks>0?approachRadius:previousApproachRadius+(approachRadius-previousApproachRadius)*partialTick;
        int ar=Math.max(1,Math.round(displayedRadius));
        int color=feedbackTicks>0?(lastHit?resultColor:0xFFFF5555):0xFFFFE4A0;
        ForgingMinigameArt.ring(g,targetX,targetY,ar,2,color);
        if(feedbackTicks>0)ForgingMinigameArt.ring(g,targetX,targetY,TARGET_RADIUS+feedbackTicks,2,color);
        if(feedbackTicks>0&&lastHit)ForgingMinigameArt.sparks(g,targetX,targetY,feedbackTicks,0xFFFFC45E);
    }
    @Override public void onClose(){if(minecraft!=null)minecraft.setScreen(null);}
    @Override public boolean isPauseScreen(){return false;}
    private enum Direction{UP(GLFW.GLFW_KEY_W,"W","^"),LEFT(GLFW.GLFW_KEY_A,"A","<"),DOWN(GLFW.GLFW_KEY_S,"S","v"),RIGHT(GLFW.GLFW_KEY_D,"D",">");final int code;final String key,symbol;Direction(int c,String k,String s){code=c;key=k;symbol=s;}static Direction fromKey(int k){for(Direction d:values())if(d.code==k)return d;return null;}}
}
