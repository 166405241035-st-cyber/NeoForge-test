package com.example.examplemod;

/**
 * Named effects currently defined by the project design.
 * Effect behavior is implemented separately; this enum is the data identity used
 * by filtering/randomization.
 */
public enum ForgingEffect {
    ZOMBIE_MINION_CALLING("Zombie Minion Calling", MonsterMaterial.ROTTEN_FLESH, EffectCategory.ATTACK),
    CRIPPLING_STRIKE("Crippling Strike", MonsterMaterial.ROTTEN_FLESH, EffectCategory.ATTACK),
    SCAVENGER_DIG("Scavenger Dig", MonsterMaterial.ROTTEN_FLESH, EffectCategory.MINING),
    ROTTEN_COMPOST("Rotten Compost", MonsterMaterial.ROTTEN_FLESH, EffectCategory.MINING),
    UNREFINED_ORE_DISCOVERY("Unrefined Ore Discovery", MonsterMaterial.ROTTEN_FLESH, EffectCategory.FARMING),

    SPINE_SPIKE("Spine Spike", MonsterMaterial.BONE, EffectCategory.ATTACK),
    GRAVE_GRASP("Grave Grasp", MonsterMaterial.BONE, EffectCategory.ATTACK),
    ROUGH_CLEAVE_3X3("Rough Cleave 3x3", MonsterMaterial.BONE, EffectCategory.MINING),
    BONE_DUST_EXTRACT("Bone Dust Extract", MonsterMaterial.BONE, EffectCategory.MINING),
    ORGANIC_CATALYST("Organic Catalyst", MonsterMaterial.BONE, EffectCategory.FARMING),

    WEB_TRAP("Web Trap", MonsterMaterial.STRING, EffectCategory.ATTACK),
    HARPOON_PULL("Harpoon Pull", MonsterMaterial.STRING, EffectCategory.ATTACK),
    STATIC_HOVER_DROP("Static Hover Drop", MonsterMaterial.STRING, EffectCategory.MINING),
    BLOCK_LEVITATION("Block Levitation", MonsterMaterial.STRING, EffectCategory.MINING),
    FLORA_AEGIS("Flora Aegis", MonsterMaterial.STRING, EffectCategory.FARMING),

    COMBO_DETONATION("Combo Detonation", MonsterMaterial.GUNPOWDER, EffectCategory.ATTACK),
    CRITICAL_BLAST("Critical Blast", MonsterMaterial.GUNPOWDER, EffectCategory.ATTACK),
    TUNNEL_CHARGE_3X1("Tunnel Charge 3x1", MonsterMaterial.GUNPOWDER, EffectCategory.MINING),
    LINEAR_BLAST_1X5("Linear Blast 1x5", MonsterMaterial.GUNPOWDER, EffectCategory.MINING),
    EXPLOSIVE_TILLING("Explosive Tilling", MonsterMaterial.GUNPOWDER, EffectCategory.FARMING),

    UNSTOPPABLE_KNOCKBACK("Unstoppable Knockback", MonsterMaterial.SLIME, EffectCategory.ATTACK),
    SLIME_TRAIL_STRIKE("Slime Trail Strike", MonsterMaterial.SLIME, EffectCategory.ATTACK),
    MAGNETIC_CLUMPING("Magnetic Clumping", MonsterMaterial.SLIME, EffectCategory.MINING),
    EARTHY_SHOCKWAVE("Earthy Shockwave", MonsterMaterial.SLIME, EffectCategory.MINING),
    MOISTURE_RETAIN("Moisture Retain", MonsterMaterial.SLIME, EffectCategory.FARMING),

    MOB_SWAP("Mob Swap", MonsterMaterial.ENDER, EffectCategory.ATTACK),
    RIFT_TELEPORT_ATTACK("Rift Teleport Attack", MonsterMaterial.ENDER, EffectCategory.ATTACK),
    VOID_VACUUM_PICK("Void Vacuum Pick", MonsterMaterial.ENDER, EffectCategory.MINING),
    LINE_BUILDER("Line Builder", MonsterMaterial.ENDER, EffectCategory.MINING),
    POCKET_DIMENSION("Pocket Dimension", MonsterMaterial.ENDER, EffectCategory.FARMING),

    FIREBALL_SHOOT("Fireball Shoot", MonsterMaterial.BLAZE_ROD, EffectCategory.ATTACK),
    LAVA_WAVE("Lava Wave", MonsterMaterial.BLAZE_ROD, EffectCategory.ATTACK),
    FRENZY_DIGGING("Frenzy Digging", MonsterMaterial.BLAZE_ROD, EffectCategory.MINING),
    THERMAL_CROP_BARRIER("Thermal Crop Barrier", MonsterMaterial.BLAZE_ROD, EffectCategory.MINING),
    AUTO_SMELT_MINING("Auto-Smelt Mining", MonsterMaterial.BLAZE_ROD, EffectCategory.FARMING),

    AEGIS_SHIELD("Aegis Shield", MonsterMaterial.GHAST_TEAR, EffectCategory.ATTACK),
    VAMPIRIC_VITALITY("Vampiric Vitality", MonsterMaterial.GHAST_TEAR, EffectCategory.ATTACK),
    AIR_SLASH_RUPTURE("Air Slash Rupture", MonsterMaterial.GHAST_TEAR, EffectCategory.MINING),
    SELF_REPAIRING("Self-Repairing", MonsterMaterial.GHAST_TEAR, EffectCategory.MINING),
    HEALING_HARVEST("Healing Harvest", MonsterMaterial.GHAST_TEAR, EffectCategory.FARMING),

    WITHER_DRAIN("Wither Drain", MonsterMaterial.WITHER, EffectCategory.ATTACK),
    WITHER_CURSE_POWER("Wither Curse Power", MonsterMaterial.WITHER, EffectCategory.ATTACK),
    OBSIDIAN_BREAKER("Obsidian Breaker", MonsterMaterial.WITHER, EffectCategory.MINING),
    SOUL_SAND_EXTRACTION("Soul Sand Extraction", MonsterMaterial.WITHER, EffectCategory.MINING),
    NETHER_MUTATION("Nether Mutation", MonsterMaterial.WITHER, EffectCategory.FARMING),

    VELOCITY_STRIKE("Velocity Strike", MonsterMaterial.PHANTOM, EffectCategory.ATTACK),
    BOOMERANG_WEAPON("Boomerang Weapon", MonsterMaterial.PHANTOM, EffectCategory.ATTACK),
    AIRBORNE_MINING("Airborne Mining", MonsterMaterial.PHANTOM, EffectCategory.MINING),
    FRONT_DASH("Front Dash", MonsterMaterial.PHANTOM, EffectCategory.MINING),
    EXTENDED_REACH_TILLING("Extended Reach Tilling", MonsterMaterial.PHANTOM, EffectCategory.FARMING),

    POISON_GAS_CLOUD("Poison Gas Cloud", MonsterMaterial.DRAGON_BREATH, EffectCategory.ATTACK),
    STUN_TIME_STOP("Stun Time Stop", MonsterMaterial.DRAGON_BREATH, EffectCategory.ATTACK),
    WIDE_EXCAVATION_4X4("Wide Excavation 4x4", MonsterMaterial.DRAGON_BREATH, EffectCategory.MINING),
    LINEAR_PENETRATION_3X15("Linear Penetration 3x15", MonsterMaterial.DRAGON_BREATH, EffectCategory.MINING),
    HYPER_GROWTH_SOIL("Hyper Growth Soil", MonsterMaterial.DRAGON_BREATH, EffectCategory.FARMING),

    IRON_FORTRESS_GUARD("Iron Fortress Guard", MonsterMaterial.SHULKER, EffectCategory.ATTACK),
    LEVITATION_BLOW("Levitation Blow", MonsterMaterial.SHULKER, EffectCategory.ATTACK),
    INTERNAL_STORAGE("Internal Storage", MonsterMaterial.SHULKER, EffectCategory.MINING),
    EARTHY_WALL_RISE("Earthy Wall Rise", MonsterMaterial.SHULKER, EffectCategory.MINING),
    AUTO_CHEST_TRANSPORT("Auto-Chest Transport", MonsterMaterial.SHULKER, EffectCategory.FARMING),

    DIVINE_BEACON_LIGHT("Divine Beacon Light", MonsterMaterial.NETHER_STAR, EffectCategory.ATTACK),
    GRAVATIONAL_SLAM("Gravational Slam", MonsterMaterial.NETHER_STAR, EffectCategory.ATTACK),
    ULTIMATE_LASER_BREAKER("Ultimate Laser Breaker", MonsterMaterial.NETHER_STAR, EffectCategory.MINING),
    SKY_BRIDGE_WALK("Sky Bridge Walk", MonsterMaterial.NETHER_STAR, EffectCategory.MINING),
    NATURE_GOD_BLESS("Nature God Bless", MonsterMaterial.NETHER_STAR, EffectCategory.FARMING);

    private final String displayName;
    private final MonsterMaterial material;
    private final EffectCategory category;

    ForgingEffect(String displayName, MonsterMaterial material, EffectCategory category) {
        this.displayName = displayName;
        this.material = material;
        this.category = category;
    }

    public String displayName() {
        return displayName;
    }

    public MonsterMaterial material() {
        return material;
    }

    public EffectCategory category() {
        return category;
    }
}
