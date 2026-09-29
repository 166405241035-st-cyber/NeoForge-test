package com.example.examplemod;

import com.example.examplemod.block.*;
import com.example.examplemod.entity.*;
import com.example.examplemod.item.*;
import com.example.examplemod.skill.*;
import com.example.examplemod.skill.client.*;

import java.util.function.IntConsumer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Forge inventory: blueprint, monster material, five matching metals and a fuel input. */
public class ForgeMenu extends AbstractContainerMenu {
    public static final int BLUEPRINT_SLOT = 0;
    public static final int MONSTER_SLOT = 1;
    public static final int METAL_START = 2;
    public static final int METAL_END = 7;
    public static final int FUEL_SLOT = 7;
    public static final int FORGE_SLOT_COUNT = 8;

    private final Container forgeInventory;
    private final Player owner;
    private final IntConsumer fuelSaver;
    private int fuel;

    public ForgeMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(FORGE_SLOT_COUNT), 0, value -> {});
    }

    public ForgeMenu(int containerId, Inventory playerInventory, Container forgeInventory) {
        this(containerId, playerInventory, forgeInventory, 0, value -> {});
    }

    public ForgeMenu(int containerId, Inventory playerInventory, Container forgeInventory, int initialFuel, IntConsumer fuelSaver) {
        super(ExampleMod.FORGE_MENU.get(), containerId);
        this.forgeInventory = forgeInventory;
        this.owner = playerInventory.player;
        this.fuel = Math.max(0, Math.min(ForgeIngredientResolver.MAX_FUEL, initialFuel));
        this.fuelSaver = fuelSaver;
        checkContainerSize(forgeInventory, FORGE_SLOT_COUNT);
        forgeInventory.startOpen(playerInventory.player);
        addDataSlot(new DataSlot() {
            @Override public int get() { return fuel; }
            @Override public void set(int value) { fuel = Math.max(0, Math.min(ForgeIngredientResolver.MAX_FUEL, value)); }
        });
        addSlot(new Slot(forgeInventory, BLUEPRINT_SLOT, 42, 65) {
            @Override public boolean mayPlace(ItemStack stack) { return ForgeIngredientResolver.blueprint(stack) != null; }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new Slot(forgeInventory, MONSTER_SLOT, 103, 65) {
            @Override public boolean mayPlace(ItemStack stack) { return ForgeIngredientResolver.monsterMaterial(stack) != null; }
            @Override public int getMaxStackSize() { return 1; }
        });
        addMetalSlot(2, 157, 57); addMetalSlot(3, 181, 57); addMetalSlot(4, 205, 57); addMetalSlot(5, 169, 81); addMetalSlot(6, 193, 81);
        addSlot(new Slot(forgeInventory, FUEL_SLOT, 256, 65) {
            @Override public boolean mayPlace(ItemStack stack) { return ForgeIngredientResolver.isFuel(stack, owner.level()); }
            @Override public void setChanged() { super.setChanged(); absorbFuel(); }
        });
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addSlot(new Slot(playerInventory, col + row * 9 + 9, 75 + col * 18, 179 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(playerInventory, col, 75 + col * 18, 237));
    }

    private void addMetalSlot(int index, int x, int y) {
        addSlot(new Slot(forgeInventory, index, x, y) {
            @Override public boolean mayPlace(ItemStack stack) { return ForgeIngredientResolver.metal(stack) != null; }
            @Override public int getMaxStackSize() { return 1; }
        });
    }

    public ItemStack stackAt(int slot) { return forgeInventory.getItem(slot); }
    public int fuel() { return fuel; }
    public int maxFuel() { return ForgeIngredientResolver.MAX_FUEL; }

    private void absorbFuel() {
        ItemStack stack = stackAt(FUEL_SLOT);
        if (owner.level().isClientSide()) return;
        int value = ForgeIngredientResolver.fuelValue(stack, owner.level());
        if (value <= 0 || fuel >= ForgeIngredientResolver.MAX_FUEL) return;
        int room = ForgeIngredientResolver.MAX_FUEL - fuel;
        int consumed = Math.min(stack.getCount(), (room + value - 1) / value);
        if (consumed <= 0) return;
        fuel = Math.min(ForgeIngredientResolver.MAX_FUEL, fuel + consumed * value);
        boolean lavaBucket = stack.is(Items.LAVA_BUCKET);
        stack.shrink(consumed);
        if (lavaBucket && stack.isEmpty()) forgeInventory.setItem(FUEL_SLOT, new ItemStack(Items.BUCKET));
        forgeInventory.setChanged();
        fuelSaver.accept(fuel);
    }

    @Override public void broadcastChanges() { absorbFuel(); super.broadcastChanges(); }

    public boolean hasValidRecipe() {
        ForgingBlueprintType blueprint = ForgeIngredientResolver.blueprint(stackAt(BLUEPRINT_SLOT));
        MonsterMaterial monster = ForgeIngredientResolver.monsterMaterial(stackAt(MONSTER_SLOT));
        ForgingMetal metal = ForgeIngredientResolver.metal(stackAt(METAL_START));
        if (blueprint == null || monster == null || metal == null) return false;
        ItemStack firstMetal = stackAt(METAL_START);
        for (int slot = METAL_START; slot < METAL_END; slot++) {
            ItemStack stack = stackAt(slot);
            if (stack.isEmpty() || ForgeIngredientResolver.metal(stack) != metal || !ForgeIngredientResolver.sameItem(firstMetal, stack)) return false;
        }
        return true;
    }

    public ForgingBlueprintType selectedBlueprint() { return ForgeIngredientResolver.blueprint(stackAt(BLUEPRINT_SLOT)); }
    public MonsterMaterial selectedMonster() { return ForgeIngredientResolver.monsterMaterial(stackAt(MONSTER_SLOT)); }
    public ForgingMetal selectedMetal() { return ForgeIngredientResolver.metal(stackAt(METAL_START)); }

    private void consumeRecipe() {
        fuel -= ForgeIngredientResolver.forgeCost(selectedMetal());
        fuelSaver.accept(fuel);
        // The blueprint is a reusable pattern; only materials and energy are spent.
        stackAt(MONSTER_SLOT).shrink(1);
        for (int slot = METAL_START; slot < METAL_END; slot++) stackAt(slot).shrink(1);
        forgeInventory.setChanged(); broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0 && hasValidRecipe() && fuel >= ForgeIngredientResolver.forgeCost(selectedMetal())) {
            ForgingBlueprintType blueprint = selectedBlueprint();
            ForgingMetal metal = selectedMetal();
            MonsterMaterial material = selectedMonster();
            consumeRecipe();
            if (!player.level().isClientSide()) ForgeRewardSession.begin(player, blueprint, metal, material);
            return true;
        }
        return false;
    }

    @Override public boolean stillValid(Player player) { return true; }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index); if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem(), copy = source.copy();
        if (index < FORGE_SLOT_COUNT) { if (!moveItemStackTo(source, FORGE_SLOT_COUNT, slots.size(), true)) return ItemStack.EMPTY; }
        else {
            int target = targetSlot(source);
            if (target >= 0) { if (!moveItemStackTo(source, target, target + 1, false)) return ItemStack.EMPTY; }
            else if (!moveItemStackTo(source, FORGE_SLOT_COUNT, slots.size(), false)) return ItemStack.EMPTY;
        }
        if (source.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        if (source.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, source); broadcastChanges(); return copy;
    }

    private int targetSlot(ItemStack stack) {
        if (ForgeIngredientResolver.blueprint(stack) != null) return BLUEPRINT_SLOT;
        if (ForgeIngredientResolver.monsterMaterial(stack) != null) return MONSTER_SLOT;
        if (ForgeIngredientResolver.isFuel(stack, owner.level())) return FUEL_SLOT;
        if (ForgeIngredientResolver.metal(stack) != null) for (int i = METAL_START; i < METAL_END; i++) if (!slots.get(i).hasItem()) return i;
        return -1;
    }

    @Override public void removed(Player player) {
        fuelSaver.accept(fuel); super.removed(player); forgeInventory.stopOpen(player);
        if (!player.level().isClientSide()) clearContainer(player, forgeInventory);
    }
}
