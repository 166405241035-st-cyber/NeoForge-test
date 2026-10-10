package com.example.examplemod.debug.menu;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.forging.blueprint.HeadBlueprintType;
import com.example.examplemod.forging.material.ForgingMetal;
import com.example.examplemod.forging.result.AnvilAssemblyResult;
import com.example.examplemod.skill.EffectTier;
import com.example.examplemod.skill.ForgingEffect;
import com.example.examplemod.skill.blessing.ForgedBlessing;
import com.example.examplemod.skill.blessing.ForgedBlessingRuntime;
import com.example.examplemod.skill.curse.ForgedCurse;
import com.example.examplemod.skill.curse.ForgedCurseRuntime;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BlessingCurseTestMenu extends AbstractContainerMenu {
    public static final int NONE = 0, BLESSING = 1, CURSE = 2;
    private static final int EFFECT_COUNT = ForgingEffect.values().length;
    private static final int SPECIAL_SLOTS = Math.max(ForgedBlessing.values().length, ForgedCurse.values().length);

    public BlessingCurseTestMenu(int id, Inventory inventory) {
        super(ExampleMod.BLESSING_CURSE_TEST_MENU.get(), id);
        for (int row=0; row<3; row++) for (int col=0; col<9; col++)
            addSlot(new Slot(inventory, col + row*9 + 9, 35 + col*18, 142 + row*18));
        for (int col=0; col<9; col++) addSlot(new Slot(inventory, col, 35 + col*18, 200));
    }

    public static int encodeSelection(HeadBlueprintType blueprint, ForgingMetal metal, ForgingEffect effect,
            EffectTier tier, int specialType, int specialIndex) {
        int value = blueprint.ordinal();
        value = value * ForgingMetal.values().length + metal.ordinal();
        value = value * EFFECT_COUNT + effect.ordinal();
        value = value * EffectTier.values().length + tier.ordinal();
        value = value * 3 + specialType;
        return value * SPECIAL_SLOTS + specialIndex;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id < 0 || player.level().isClientSide()) return false;
        try {
            int value=id;
            int specialIndex=value % SPECIAL_SLOTS; value/=SPECIAL_SLOTS;
            int specialType=value % 3; value/=3;
            EffectTier tier=EffectTier.values()[value % EffectTier.values().length]; value/=EffectTier.values().length;
            ForgingEffect effect=ForgingEffect.values()[value % EFFECT_COUNT]; value/=EFFECT_COUNT;
            ForgingMetal metal=ForgingMetal.values()[value % ForgingMetal.values().length]; value/=ForgingMetal.values().length;
            HeadBlueprintType blueprint=HeadBlueprintType.values()[value];

            List<AnvilAssemblyResult.FinalEffect> effects=List.of(new AnvilAssemblyResult.FinalEffect(effect,tier));
            AnvilAssemblyResult assembly=new AnvilAssemblyResult(blueprint,metal,metal,metal,
                    effect.material(),effect.material(),effect.material(),effects);
            ItemStack result=ExampleMod.FORGED_EQUIPMENT_ITEM.get().createStack(assembly);

            if (specialType==BLESSING) {
                if (specialIndex >= ForgedBlessing.values().length) return false;
                ForgedBlessingRuntime.set(result,ForgedBlessing.values()[specialIndex]);
            } else if (specialType==CURSE) {
                if (specialIndex >= ForgedCurse.values().length) return false;
                ForgedCurseRuntime.set(result,ForgedCurse.values()[specialIndex]);
            }

            if (!player.getInventory().add(result)) player.drop(result,false);
            player.displayClientMessage(Component.literal("Created Blessing/Curse test item."),true);
            return true;
        } catch (RuntimeException ex) {
            ExampleMod.LOGGER.warn("Rejected Blessing/Curse tester selection {}",id,ex);
            return false;
        }
    }

    @Override public boolean stillValid(Player player){ return true; }
    @Override public ItemStack quickMoveStack(Player player,int index){ return ItemStack.EMPTY; }
}
