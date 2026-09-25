package com.example.examplemod;

import com.example.examplemod.block.*;
import com.example.examplemod.entity.*;
import com.example.examplemod.item.*;
import com.example.examplemod.skill.*;
import com.example.examplemod.skill.client.*;

/** Blueprint choices shown in the forge GUI. Head types map to the existing head blueprint rules. */
public enum ForgingBlueprintType {
    SWORD_HEAD("Sword Head", HeadBlueprintType.SWORD),
    AXE_HEAD("Axe Head", HeadBlueprintType.AXE),
    PICKAXE_HEAD("Pickaxe Head", HeadBlueprintType.PICKAXE),
    SHOVEL_HEAD("Shovel Head", HeadBlueprintType.SHOVEL),
    HOE_HEAD("Hoe Head", HeadBlueprintType.HOE),
    CORE("Core", null),
    ROD("Rod", null);

    private final String displayName;
    private final HeadBlueprintType headType;

    ForgingBlueprintType(String displayName, HeadBlueprintType headType) {
        this.displayName = displayName;
        this.headType = headType;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isHead() {
        return headType != null;
    }

    public HeadBlueprintType headType() {
        return headType;
    }
}
