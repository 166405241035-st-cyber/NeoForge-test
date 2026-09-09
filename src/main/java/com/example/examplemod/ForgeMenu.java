package com.example.examplemod;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Forge inventory layout:
 * 0 = Blueprint
 * 1 = Monster material
 * 2..6 = five metal slots
 * 7 = Fuel
 */
public class ForgeMenu extends AbstractContainerMenu {
    public static final int BLUEPRINT_SLOT = 0;
    public static final int MONSTER_SLOT = 1;
    public static final int METAL_START = 2;
    public static final int METAL_END = 7;
    public static final int FUEL_SLOT = 7;
    public static final int FORGE_SLOT_COUNT = 8;

    private final Container forgeInventory;

    public ForgeMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(FORGE_SLOT_COUNT));
    }

    public ForgeMenu(int containerId, Inventory playerInventory, Container forgeInventory) {
        super(ExampleMod.FORGE_MENU.get(), containerId);
        this.forgeInventory = forgeInventory;
        checkContainerSize(forgeInventory, FORGE_SLOT_COUNT);
        forgeInventory.startOpen(playerInventory.player);

        // Coordinates are relative to the 176x190 GUI texture-less panel.
        addSlot(new Slot(forgeInventory, BLUEPRINT_SLOT, 27, 46));
        addSlot(new Slot(forgeInventory, MONSTER_SLOT, 86, 58));

        addSlot(new Slot(forgeInventory, 2, 86, 31));
        addSlot(new Slot(forgeInventory, 3, 59, 47));
        addSlot(new Slot(forgeInventory, 4, 113, 47));
        addSlot(new Slot(forgeInventory, 5, 69, 77));
        addSlot(new Slot(forgeInventory, 6, 103, 77));

        addSlot(new Slot(forgeInventory, FUEL_SLOT, 145, 67));

        // Player inventory.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 108 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 166));
        }
    }

    public ItemStack stackAt(int forgeSlot) {
        return forgeInventory.getItem(forgeSlot);
    }

    public int fuelCount() {
        return stackAt(FUEL_SLOT).getCount();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack empty = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return empty;

        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();

        if (index < FORGE_SLOT_COUNT) {
            if (!moveItemStackTo(source, FORGE_SLOT_COUNT, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(source, 0, FORGE_SLOT_COUNT, false)) return ItemStack.EMPTY;
        }

        if (source.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();

        if (source.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, source);
        return copy;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        forgeInventory.stopOpen(player);
        if (!player.level().isClientSide()) {
            clearContainer(player, forgeInventory);
        }
    }
}
