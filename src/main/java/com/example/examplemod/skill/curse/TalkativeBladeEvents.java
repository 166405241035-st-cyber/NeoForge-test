package com.example.examplemod.skill.curse;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

/**
 * Context-aware chatter for Talkative Blade.
 * The curse works from anywhere in the player's inventory; it does not need to be held.
 */
@EventBusSubscriber(modid = "examplemod")
public final class TalkativeBladeEvents {
    private static final String NEXT_CHAT = "TalkativeBladeNextChat";
    private static final String NEXT_EMERGENCY = "TalkativeBladeNextEmergency";
    private static final String NEXT_TROLL = "TalkativeBladeNextTroll";
    private static final String TROLL_LEFT = "TalkativeBladeTrollLeft";
    private static final String TROLL_NEXT = "TalkativeBladeTrollNext";

    private static final List<String> IDLE = List.of(
            "นี่... จะยืนอีกนานไหม?", "ข้าเริ่มเบื่อแล้วนะ", "มีอะไรให้ฟันบ้างไหม?",
            "ฮัลโหล? เจ้าของยังมีชีวิตอยู่ไหม?", "เดินเล่นหน่อยสิ ข้าเมื่อยแทนแล้ว",
            "นี่คือการผจญภัย หรือการยืนดูหญ้า?", "ถ้าจะ AFK อย่างน้อยก็หันวิวสวย ๆ หน่อย",
            "ข้าคือดาบนะ ไม่ใช่ของแต่งกระเป๋า", "ไปหาเรื่อง... เอ้ย ไปหามอนสเตอร์กัน",
            "ข้ารู้สึกว่าเราควรทำอะไรสักอย่าง", "วันนี้เจ้าดูว่างมากเลยนะ", "ขอร้องล่ะ ขยับสักบล็อกก็ยังดี"
    );
    private static final List<String> LOW_HP = List.of(
            "เฮ้! เลือดเจ้าใกล้หมดแล้วนะ!", "ถอยก่อน! ข้ายังไม่อยากเปลี่ยนเจ้าของ!",
            "กินอะไรสักอย่างเถอะ!", "อย่ามาตายตอนที่ข้าอยู่ด้วยนะ!", "หัวใจเหลือนิดเดียวแล้วโว้ย!",
            "แผนของเจ้าคืออะไร? ตายเหรอ?", "ข้าว่าเราควรวิ่ง... เดี๋ยวนี้!", "เจ้าจะสู้ต่อจริงดิ?",
            "ถ้ารอดรอบนี้ ข้าจะไม่บ่น... ห้าวินาที", "เลือด! ข้าหมายถึงเลือดของเจ้าน่ะ!"
    );
    private static final List<String> HUNGRY = List.of(
            "หิวแล้วก็กินสิ! จะจ้องอาหารทำไม?", "ท้องเจ้าร้องดังจนข้าได้ยินแล้ว",
            "อาหารอยู่ในกระเป๋าใช่ไหม? กิน!", "ถ้าเจ้าหิวจนวิ่งไม่ได้ ข้าจะหัวเราะนะ",
            "ข้าเป็นดาบ ข้าทำอาหารให้เจ้าไม่ได้", "หาอะไรกินก่อนจะไปหาเรื่องต่อเถอะ"
    );
    private static final List<String> NIGHT = List.of(
            "กลางคืนแล้ว... ดีเลย เวลาหาเรื่อง!", "ได้เวลาที่ของน่ารัก ๆ จะออกมากินเจ้า",
            "มืดแบบนี้ Creeper ชอบนะ แค่บอกไว้", "คืนนี้อย่านอนเลย ไปฟันมอนกัน",
            "เจ้ามีเตียงใช่ไหม? ...ใช่ไหม?", "ข้าชอบกลางคืน เสียงกรีดร้องชัดดี"
    );
    private static final List<String> RAIN = List.of(
            "ฝนอีกแล้ว... ข้าจะขึ้นสนิมไหมเนี่ย?", "บรรยากาศดี เหมาะกับการฟันอะไรสักอย่าง",
            "อย่าบอกนะว่าเจ้าจะยืนตากฝน", "เปียกหมดแล้ว! รับผิดชอบข้าด้วย",
            "ฟ้าร้องทีอย่าทำข้าหลุดมือล่ะ"
    );
    private static final List<String> NETHER = List.of(
            "ร้อน ร้อน ร้อน! ใครคิดว่าที่นี่น่าเที่ยว?", "ยินดีต้อนรับสู่นรก กรุณาอย่าทำข้าตก Lava",
            "ถ้าเจ้าโยนข้าลงลาวา ข้าจะตามหลอกเจ้า", "Piglin มองเราแปลก ๆ นะ",
            "เตือนก่อนเลย ข้าไม่กันไฟให้เจ้านะ", "ที่นี่ไม่มีน้ำ... แผนดีมากเจ้าของ"
    );
    private static final List<String> END = List.of(
            "นี่คือ The End... ชื่อเป็นลางดีจริง ๆ", "อย่าจ้อง Enderman แล้วโทษข้านะ",
            "มังกรอยู่ไหน ข้าอยากคุยด้วยคมดาบ", "ถ้าตก Void เราจบทั้งคู่ เข้าใจนะ?",
            "พื้นน้อยเกินไป ข้าไม่ชอบเลย", "สถานที่สวยดี ถ้าไม่นับความตายทุกทิศทาง"
    );
    private static final List<String> CREEPER = List.of(
            "ข้างหลัง!!!", "CREEPER!!!", "หันหลังเดี๋ยวนี้!!!", "ตัวเขียวข้างหลังโว้ย!",
            "วิ่งงงงงง!!!", "มันจะระเบิดแล้ว!!!", "ข้าไม่อยากบินไปกับแรงระเบิดนะ!",
            "ข้างหลังเจ้า! ไม่ใช่ข้า อีกข้าง!", "ฟ่อออออ— เดี๋ยวนะ เสียงนั้นไม่ดี!", "ถ้ารอดครั้งนี้ฟังข้าบ้างนะ!"
    );
    private static final List<String> CREEPER_TITLES = List.of(
            "ข้างหลัง!!!", "CREEPER!!!", "วิ่งงงง!!!", "หันหลัง!!!", "มันมาแล้ว!!!"
    );
    private static final List<String> FIRE = List.of(
            "ร้อนๆๆๆๆๆๆๆ!", "ไฟไหม้โว้ยยย!", "น้ำ! น้ำอยู่ไหน?!", "เอาข้าออกจากไฟก่อนนน!",
            "เจ้ากำลังย่างตัวเองอยู่รู้ไหม?", "นี่ไม่ใช่วิธีตีดาบให้ร้อนนะ!"
    );
    private static final List<String> WATER = List.of(
            "อ๋อ ดีเลย พาข้ามาแช่น้ำ", "ข้าว่ายน้ำไม่เป็นนะ... เพราะข้าเป็นดาบ",
            "มี Drowned แถวนี้ไหม?", "อย่างน้อยก็ไม่ร้อนเหมือน Nether",
            "ถ้าจมน้ำ อย่าหวังว่าข้าจะทำ CPR"
    );
    private static final List<String> TROLL = List.of(
            "เฮ้", "เฮ้", "เฮ้", "ได้ยินไหม", "เจ้าของ", "นี่", "...", "ตอบหน่อย",
            "โอเค", "ช่างเถอะ", "ข้าเบื่อ", "หยิบข้าหน่อย", "ไม่หยิบจริงดิ?", "เฮ้!"
    );

    private TalkativeBladeEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        long now = player.level().getGameTime();
        if (now % 10L != 0L || !hasTalkativeBlade(player)) return;

        var data = player.getPersistentData();

        // Rare obnoxious chat burst: intentionally part of the curse.
        int left = data.getInt(TROLL_LEFT);
        if (left > 0 && now >= data.getLong(TROLL_NEXT)) {
            say(player, random(player, TROLL));
            data.putInt(TROLL_LEFT, left - 1);
            data.putLong(TROLL_NEXT, now + 8L + player.getRandom().nextInt(13));
            return;
        }
        if (now >= data.getLong(NEXT_TROLL) && player.getRandom().nextDouble() < 0.04D) {
            data.putInt(TROLL_LEFT, 3 + player.getRandom().nextInt(4));
            data.putLong(TROLL_NEXT, now);
            data.putLong(NEXT_TROLL, now + 20L * (120 + player.getRandom().nextInt(181)));
        }

        // Emergency Creeper warning ignores normal chatter cooldown, but has its own cooldown.
        Creeper creeper = nearestDangerousCreeper(player);
        if (creeper != null && now >= data.getLong(NEXT_EMERGENCY)) {
            String warning = random(player, CREEPER);
            say(player, warning);
            String title = random(player, CREEPER_TITLES);
            player.sendSystemMessage(Component.literal("§c§l" + title));
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(Component.literal("§c§l" + title)));
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(Component.literal("§eTalkative Blade กำลังเตือนเจ้า!")));
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(0, 25, 10));
            data.putLong(NEXT_EMERGENCY, now + 20L * 8L);
            return;
        }

        if (now < data.getLong(NEXT_CHAT)) return;

        List<String> pool = choosePool(player);
        say(player, random(player, pool));
        // 15-45 seconds between ordinary lines.
        data.putLong(NEXT_CHAT, now + 20L * (15 + player.getRandom().nextInt(31)));
    }

    private static List<String> choosePool(ServerPlayer player) {
        if (player.isOnFire()) return FIRE;
        if (player.getHealth() <= player.getMaxHealth() * 0.30F) return LOW_HP;
        if (player.getFoodData().getFoodLevel() <= 6) return HUNGRY;
        String dimension = player.level().dimension().location().toString();
        if (dimension.contains("the_nether")) return NETHER;
        if (dimension.contains("the_end")) return END;
        if (player.isInWater()) return WATER;
        if (player.level().isRaining()) return RAIN;
        if (player.level().isNight()) return NIGHT;
        return IDLE;
    }

    private static Creeper nearestDangerousCreeper(ServerPlayer player) {
        Creeper best = null;
        double bestDistance = 36.0D;
        for (Creeper creeper : player.level().getEntitiesOfClass(Creeper.class, player.getBoundingBox().inflate(6.0D))) {
            double d = player.distanceToSqr(creeper);
            if (d < bestDistance && isBehind(player, creeper)) {
                best = creeper;
                bestDistance = d;
            }
        }
        return best;
    }

    private static boolean isBehind(ServerPlayer player, Creeper creeper) {
        var look = player.getLookAngle();
        var toCreeper = creeper.position().subtract(player.position()).normalize();
        return look.dot(toCreeper) < 0.25D;
    }

    private static boolean hasTalkativeBlade(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            if (ForgedCurseRuntime.has(stack, ForgedCurse.TALKATIVE_BLADE)) return true;
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (ForgedCurseRuntime.has(stack, ForgedCurse.TALKATIVE_BLADE)) return true;
        }
        return false;
    }

    private static String random(ServerPlayer player, List<String> lines) {
        return lines.get(player.getRandom().nextInt(lines.size()));
    }

    private static void say(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal("§d[Talkative Blade] §f" + message));
    }
}
