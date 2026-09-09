package com.example.examplemod;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Three-slot assembly menu: Head + Core + Rod. */
public class AnvilMenu extends AbstractContainerMenu {
    public static final int HEAD_SLOT = 0;
    public static final int CORE_SLOT = 1;
    public static final int ROD_SLOT = 2;
    public static final int ANVIL_SLOT_COUNT = 3;

    private final Container anvilInventory;

    public AnvilMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(ANVIL_SLOT_COUNT));
    }

    public AnvilMenu(int containerId, Inventory playerInventory, Container anvilInventory) {
        super(ExampleMod.ANVIL_MENU.get(), containerId);
        this.anvilInventory = anvilInventory;
        checkContainerSize(anvilInventory, ANVIL_SLOT_COUNT);
        anvilInventory.startOpen(playerInventory.player);

        addSlot(new Slot(anvilInventory, HEAD_SLOT, 50, 42) {
            @Override public boolean mayPlace(ItemStack stack) { return ForgedHeadItem.readResult(stack) != null; }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new Slot(anvilInventory, CORE_SLOT, 88, 42) {
            @Override public boolean mayPlace(ItemStack stack) { return ForgedCoreItem.readResult(stack) != null; }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new Slot(anvilInventory, ROD_SLOT, 126, 42) {
            @Override public boolean mayPlace(ItemStack stack) { return ForgedRodItem.readResult(stack) != null; }
            @Override public int getMaxStackSize() { return 1; }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 96 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 154));
        }
    }

    public ItemStack stackAt(int slot) { return anvilInventory.getItem(slot); }
    public ForgedHeadResult headResult() { return ForgedHeadItem.readResult(stackAt(HEAD_SLOT)); }
    public ForgedCoreResult coreResult() { return ForgedCoreItem.readResult(stackAt(CORE_SLOT)); }
    public ForgedRodResult rodResult() { return ForgedRodItem.readResult(stackAt(ROD_SLOT)); }

    public boolean hasValidAssembly() {
        return headResult() != null && coreResult() != null && rodResult() != null;
    }

    private void consumeAssembly() {
        stackAt(HEAD_SLOT).shrink(1);
        stackAt(CORE_SLOT).shrink(1);
        stackAt(ROD_SLOT).shrink(1);
        anvilInventory.setChanged();
        broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0 && hasValidAssembly()) {
            consumeAssembly();
            return true;
        }
        return false;
    }

    @Override public boolean stillValid(Player player) { return true; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();

        if (index < ANVIL_SLOT_COUNT) {
            if (!moveItemStackTo(source, ANVIL_SLOT_COUNT, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            int target = targetSlot(source);
            if (target < 0 || !moveItemStackTo(source, target, target + 1, false)) return ItemStack.EMPTY;
        }

        if (source.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        if (source.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, source);
        return copy;
    }

    private int targetSlot(ItemStack stack) {
        if (ForgedHeadItem.readResult(stack) != null) return HEAD_SLOT;
        if (ForgedCoreItem.readResult(stack) != null) return CORE_SLOT;
        if (ForgedRodItem.readResult(stack) != null) return ROD_SLOT;
        return -1;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        anvilInventory.stopOpen(player);
        if (!player.level().isClientSide()) clearContainer(player, anvilInventory);
    }
}
