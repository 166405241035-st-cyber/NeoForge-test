package com.example.examplemod.menu;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.forging.result.ForgedCoreResult;
import com.example.examplemod.forging.result.ForgedHeadResult;
import com.example.examplemod.forging.result.ForgedRodResult;
import com.example.examplemod.forging.session.AnvilRewardSession;
import com.example.examplemod.item.ForgedCoreItem;
import com.example.examplemod.item.ForgedHeadItem;
import com.example.examplemod.item.ForgedRodItem;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Three-slot assembly menu: Head + Core + Rod. */
public class AnvilMenu extends AbstractContainerMenu {
    public static final int HEAD_SLOT = 0;
    public static final int CORE_SLOT = 1;
    public static final int ROD_SLOT = 2;
    public static final int ANVIL_SLOT_COUNT = 3;

    private final Container anvilInventory;
    private final ContainerLevelAccess access;

    public AnvilMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(ANVIL_SLOT_COUNT), ContainerLevelAccess.NULL);
    }

    public AnvilMenu(int containerId, Inventory playerInventory, Container anvilInventory) {
        this(containerId, playerInventory, anvilInventory, ContainerLevelAccess.NULL);
    }

    public AnvilMenu(int containerId, Inventory playerInventory, Container anvilInventory, ContainerLevelAccess access) {
        super(ExampleMod.ANVIL_MENU.get(), containerId);
        this.anvilInventory = anvilInventory;
        this.access = access;
        checkContainerSize(anvilInventory, ANVIL_SLOT_COUNT);
        anvilInventory.startOpen(playerInventory.player);

        addSlot(new Slot(anvilInventory, HEAD_SLOT, 68, 67) {
            @Override public boolean mayPlace(ItemStack stack) { return ForgedHeadItem.readResult(stack) != null; }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new Slot(anvilInventory, CORE_SLOT, 143, 67) {
            @Override public boolean mayPlace(ItemStack stack) { return ForgedCoreItem.readResult(stack) != null; }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new Slot(anvilInventory, ROD_SLOT, 218, 67) {
            @Override public boolean mayPlace(ItemStack stack) { return ForgedRodItem.readResult(stack) != null; }
            @Override public int getMaxStackSize() { return 1; }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 72 + col * 18, 180 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 72 + col * 18, 238));
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
        if (id == 0 && stillValid(player) && hasValidAssembly()) {
            ForgedHeadResult head = headResult();
            ForgedCoreResult core = coreResult();
            ForgedRodResult rod = rodResult();
            consumeAssembly();
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                AnvilRewardSession.begin(serverPlayer, head, core, rod);
            }
            return true;
        }
        return false;
    }

    @Override public boolean stillValid(Player player) {
        return stillValid(access, player, ExampleMod.FORGING_ANVIL.get());
    }

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
