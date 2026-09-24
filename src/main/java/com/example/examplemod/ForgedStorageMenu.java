package com.example.examplemod;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Item-backed storage used by Pocket Dimension and Internal Storage. */
public class ForgedStorageMenu extends AbstractContainerMenu {
    private final ItemStack tool;
    private final SimpleContainer storage;
    private final int slots;

    public ForgedStorageMenu(int id, Inventory inv) {
        this(id, inv, inv.player.getMainHandItem());
    }

    public ForgedStorageMenu(int id, Inventory inv, ItemStack tool) {
        super(ExampleMod.FORGED_STORAGE_MENU.get(), id);
        this.tool = tool;
        this.slots = storageSize(tool);
        this.storage = new SimpleContainer(Math.max(1, slots));
        load();

        int rows = (slots + 8) / 9;
        for (int i = 0; i < slots; i++)
            addSlot(new Slot(storage, i, 8 + (i % 9) * 18, 18 + (i / 9) * 18));

        int invY = 31 + rows * 18;
        for (int row=0; row<3; row++) for (int col=0; col<9; col++)
            addSlot(new Slot(inv, col + row*9 + 9, 8 + col*18, invY + row*18));
        for (int col=0; col<9; col++) addSlot(new Slot(inv, col, 8 + col*18, invY + 58));
    }

    public static int storageSize(ItemStack tool) {
        EffectTier pocket = ForgedEffectRuntime.tier(tool, ForgingEffect.POCKET_DIMENSION);
        EffectTier internal = ForgedEffectRuntime.tier(tool, ForgingEffect.INTERNAL_STORAGE);
        int size = 0;
        if (pocket != null) size = Math.max(size, switch(pocket){case I->9;case II->18;case III->27;});
        if (internal != null) size = Math.max(size, switch(internal){case I->18;case II->27;case III->36;});
        return size;
    }

    private void load() {
        CustomData data=tool.get(DataComponents.CUSTOM_DATA); if(data==null)return;
        CompoundTag root=data.copyTag();
        if(!root.contains("forgedStorage", Tag.TAG_LIST))return;
        ListTag list=root.getList("forgedStorage", Tag.TAG_COMPOUND);
        for(int i=0;i<list.size();i++){
            CompoundTag e=list.getCompound(i); int slot=e.getInt("slot");
            if(slot>=0&&slot<storage.getContainerSize())
                ItemStack.parse(tool.getItemHolder().registryLookup(), e.getCompound("item")).ifPresent(s->storage.setItem(slot,s));
        }
    }

    private void save() {
        CustomData.update(DataComponents.CUSTOM_DATA, tool, root->{
            ListTag list=new ListTag();
            for(int i=0;i<storage.getContainerSize();i++){
                ItemStack s=storage.getItem(i); if(s.isEmpty())continue;
                CompoundTag e=new CompoundTag(); e.putInt("slot",i);
                e.put("item",s.save(tool.getItemHolder().registryLookup()));
                list.add(e);
            }
            root.put("forgedStorage",list);
        });
    }

    @Override public void removed(Player player){ save(); super.removed(player); }
    @Override public boolean stillValid(Player player){ return !tool.isEmpty() && storageSize(tool)>0; }

    @Override public ItemStack quickMoveStack(Player player,int index){
        Slot slot=slots.get(index); if(!slot.hasItem())return ItemStack.EMPTY;
        ItemStack original=slot.getItem(), copy=original.copy();
        if(index<this.slots){ if(!moveItemStackTo(original,this.slots,slots.size(),true))return ItemStack.EMPTY; }
        else if(!moveItemStackTo(original,0,this.slots,false))return ItemStack.EMPTY;
        if(original.isEmpty())slot.set(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
}
