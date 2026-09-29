package com.example.examplemod.guide;

import com.example.examplemod.skill.ForgingEffect;
import com.example.examplemod.skill.blessing.ForgedBlessing;
import com.example.examplemod.skill.curse.ForgedCurse;

/** Short player-facing behavior summaries. Unlocked tiers are supplied by the journal. */
public final class ForgingGuideDescriptions {
    private ForgingGuideDescriptions() {}

    public static String effect(ForgingEffect effect, int tier) {
        if (effect == ForgingEffect.FRENZY_DIGGING) {
            int bonus = tier == 1 ? 15 : tier == 2 ? 30 : 50;
            return "ขุดต่อเนื่อง 5 บล็อกเพื่อเพิ่มความเร็วขุด " + bonus + "% หยุดขุดเกิน 5 วินาทีจะเริ่มนับใหม่";
        }
        return switch (effect) {
            case ZOMBIE_MINION_CALLING -> "โจมตีแล้วมีโอกาสเรียกซอมบี้มาช่วยต่อสู้";
            case CRIPPLING_STRIKE -> "โจมตีแล้วมีโอกาสทำให้เป้าหมายเคลื่อนที่ช้าลง";
            case SCAVENGER_DIG -> "ขุดแล้วมีโอกาสพบของเพิ่มเติมจากซากมอนสเตอร์";
            case ROTTEN_COMPOST -> "ทำฟาร์มแล้วมีโอกาสได้ปุ๋ยจากเนื้อเน่า";
            case UNREFINED_ORE_DISCOVERY -> "เก็บเกี่ยวแล้วมีโอกาสพบแร่ดิบ";
            case SPINE_SPIKE -> "โจมตีแล้วสร้างหนามกระดูกใส่เป้าหมาย";
            case GRAVE_GRASP -> "รั้งเป้าหมายให้ขยับได้ลำบากชั่วคราว";
            case ROUGH_CLEAVE_3X3 -> "ใช้สกิลขุดพื้นที่ 3×3 ด้านหน้า";
            case BONE_DUST_EXTRACT -> "ขุดแล้วมีโอกาสได้ผงกระดูก";
            case ORGANIC_CATALYST -> "เร่งการเติบโตของพืชบริเวณที่เลือก";
            case WEB_TRAP -> "โจมตีแล้วดักศัตรูด้วยใยแมงมุม";
            case HARPOON_PULL -> "ใช้สกิลดึงเป้าหมายเข้ามา";
            case STATIC_HOVER_DROP -> "หยุดการตกของตัวเองชั่วคราว";
            case BLOCK_LEVITATION -> "ใช้สกิลสร้างสิ่งกีดขวางชั่วคราว";
            case FLORA_AEGIS -> "ช่วยป้องกันพืชผลในแปลง";
            case COMBO_DETONATION -> "โจมตีต่อเนื่องเพื่อปล่อยแรงระเบิด";
            case CRITICAL_BLAST -> "โจมตีคริติคอลแล้วมีโอกาสเกิดระเบิด";
            case TUNNEL_CHARGE_3X1 -> "ใช้สกิลเจาะทาง 3×1 ด้านหน้า";
            case LINEAR_BLAST_1X5 -> "ใช้สกิลขุดเป็นเส้นตรง 1×5";
            case EXPLOSIVE_TILLING -> "พรวนดินเป็นแนวด้วยแรงระเบิด";
            case UNSTOPPABLE_KNOCKBACK -> "เพิ่มแรงผลักศัตรูเมื่อโจมตี";
            case SLIME_TRAIL_STRIKE -> "โจมตีแล้วทิ้งร่องรอยสไลม์";
            case MAGNETIC_CLUMPING -> "ดึงไอเท็มที่ตกอยู่เข้าหาตัว";
            case EARTHY_SHOCKWAVE -> "สร้างคลื่นกระแทกจากพื้น";
            case MOISTURE_RETAIN -> "ช่วยรักษาความชื้นของดินเพาะปลูก";
            case MOB_SWAP -> "ใช้สกิลสลับตำแหน่งกับเป้าหมาย";
            case RIFT_TELEPORT_ATTACK -> "โจมตีแล้วมีโอกาสย้ายตำแหน่งผ่านมิติ";
            case VOID_VACUUM_PICK -> "เก็บไอเท็มจากการขุดเข้าสู่ตัว";
            case LINE_BUILDER -> "ใช้สกิลวางบล็อกเป็นเส้น";
            case POCKET_DIMENSION -> "เปิดพื้นที่เก็บของส่วนตัวด้วยสกิล";
            case FIREBALL_SHOOT -> "ใช้สกิลยิงลูกไฟ";
            case LAVA_WAVE -> "ปล่อยคลื่นลาวาไปด้านหน้า";
            case THERMAL_CROP_BARRIER -> "ป้องกันแปลงปลูกด้วยความร้อน";
            case AUTO_SMELT_MINING -> "หลอมผลผลิตจากการขุดโดยอัตโนมัติ";
            case AEGIS_SHIELD -> "ใช้สกิลสร้างโล่ป้องกัน";
            case VAMPIRIC_VITALITY -> "โจมตีแล้วมีโอกาสฟื้นฟูพลังชีวิต";
            case AIR_SLASH_RUPTURE -> "ปล่อยคลื่นดาบจากอากาศ";
            case SELF_REPAIRING -> "ซ่อมความทนทานของอุปกรณ์";
            case HEALING_HARVEST -> "เก็บเกี่ยวแล้วมีโอกาสฟื้นฟูพลังชีวิต";
            case WITHER_DRAIN -> "ดูดพลังชีวิตจากศัตรูที่ติด Wither";
            case WITHER_CURSE_POWER -> "เพิ่มพลังโจมตีแลกกับผลเสียของ Wither";
            case OBSIDIAN_BREAKER -> "ใช้สกิลทำลายบล็อกแข็งด้านหน้า";
            case SOUL_SAND_EXTRACTION -> "ขุดแล้วมีโอกาสได้ทรายวิญญาณ";
            case NETHER_MUTATION -> "ทำให้ผลผลิตในฟาร์มเปลี่ยนสภาพ";
            case VELOCITY_STRIKE -> "ความเร็วเคลื่อนที่เพิ่มพลังโจมตี";
            case BOOMERANG_WEAPON -> "ขว้างอาวุธให้ย้อนกลับมาหาตัว";
            case AIRBORNE_MINING -> "ขุดได้เร็วขึ้นขณะลอยอยู่กลางอากาศ";
            case FRONT_DASH -> "ใช้สกิลพุ่งตัวไปด้านหน้า";
            case EXTENDED_REACH_TILLING -> "ใช้จอบพรวนดินได้ไกลขึ้น";
            case POISON_GAS_CLOUD -> "สร้างกลุ่มก๊าซพิษใส่ศัตรู";
            case STUN_TIME_STOP -> "ทำให้ศัตรูหยุดเคลื่อนไหวชั่วคราว";
            case WIDE_EXCAVATION_4X4 -> "ใช้สกิลขุดพื้นที่กว้าง 4×4";
            case LINEAR_PENETRATION_3X15 -> "ใช้สกิลเจาะแนวลึก 3×15";
            case HYPER_GROWTH_SOIL -> "เร่งการเติบโตบนดินเพาะปลูก";
            case IRON_FORTRESS_GUARD -> "เพิ่มการป้องกันขณะรับการโจมตี";
            case LEVITATION_BLOW -> "โจมตีแล้วทำให้เป้าหมายลอยขึ้น";
            case INTERNAL_STORAGE -> "เปิดที่เก็บของในอุปกรณ์";
            case EARTHY_WALL_RISE -> "สร้างกำแพงจากพื้นดิน";
            case AUTO_CHEST_TRANSPORT -> "ส่งผลผลิตไปยังหีบโดยอัตโนมัติ";
            case DIVINE_BEACON_LIGHT -> "แสงศักดิ์สิทธิ์ช่วยผู้ถืออุปกรณ์";
            case GRAVATIONAL_SLAM -> "กระแทกพื้นด้วยแรงโน้มถ่วง";
            case ULTIMATE_LASER_BREAKER -> "ยิงลำแสงขุดบล็อกด้านหน้า";
            case SKY_BRIDGE_WALK -> "สร้างทางเดินขณะเคลื่อนที่";
            case NATURE_GOD_BLESS -> "เสริมผลของการเพาะปลูก";
            case FRENZY_DIGGING -> throw new IllegalStateException("Handled above");
        };
    }

    public static String blessing(ForgedBlessing blessing) {
        return switch (blessing) {
            case DOUBLE_TRIGGER -> "สกิลที่รองรับมีโอกาส 20% เกิดผลซ้ำหนึ่งครั้ง โดยไม่หักคูลดาวน์หรือความทนทานซ้ำ การโจมตีต้องชาร์จเต็ม";
            case POWER_STRIKE -> "การโจมตีด้วยอุปกรณ์ชิ้นนี้แรงขึ้น 20%";
            case HUNTERS_FORTUNE -> "ดรอปของจากมอนสเตอร์เพิ่มอีกหนึ่งชุด";
            case LIFE_STEAL -> "โจมตีแล้วมีโอกาส 20% ฟื้นฟู 2 หน่วยพลังชีวิต";
            case DURABILITY_GUARD -> "มีโอกาส 25% ที่การใช้หนึ่งครั้งจะไม่เสียความทนทาน";
            case VEIN_BREAKER -> "ขุดแร่ชนิดเดียวกันที่ติดกันได้เพิ่มสูงสุด 8 บล็อก";
            case MINING_HASTE -> "เพิ่มความเร็วขุด 50% ขณะถืออุปกรณ์";
            case EXPERIENCE_BOOST -> "ค่าประสบการณ์จากมอนสเตอร์เพิ่ม 60%";
            case DIVINE_EXECUTION -> "โจมตีชาร์จเต็มมีโอกาส 5% กำจัดมอนสเตอร์ทันที";
        };
    }

    public static String curse(ForgedCurse curse) {
        return switch (curse) {
            case TALKATIVE_BLADE -> "อาวุธพูดกับผู้ถือจากช่องเก็บของ และอาจแกล้งหายไปชั่วคราว";
            case BERSERKER -> "เมื่อเลือดต่ำกว่าครึ่ง: โจมตีแรงขึ้น 50% เร็วขึ้น 30% แต่รับความเสียหายเพิ่ม 30%";
            case GAMBLERS_STRIKE -> "โจมตีมีโอกาสเท่ากันระหว่างแรงขึ้นสองเท่ากับไม่สร้างความเสียหาย";
            case POWER_ERASURE -> "ปิดการทำงานของสกิลหลักบนอุปกรณ์ชิ้นนี้โดยทั่วไป";
            case LIFE_EXCHANGE -> "โจมตีมอนสเตอร์มีโอกาส 5% สลับพลังชีวิตปัจจุบันกับเป้าหมาย";
            case VAMPIRE_BLADE -> "โจมตีฟื้นฟู 20% ของความเสียหาย แต่การฟื้นฟูตามธรรมชาติลดครึ่งหนึ่ง";
            case LAST_STAND -> "เมื่อเลือดเหลือไม่เกิน 2 หน่วย การโจมตีครั้งถัดไปแรงขึ้น 3 เท่า คูลดาวน์ 120 วินาที";
            case CRITICAL_FAILURE -> "โจมตีปกติแรงเหลือครึ่งเดียว แต่คริติคอลแรงขึ้น 75%";
        };
    }
}
