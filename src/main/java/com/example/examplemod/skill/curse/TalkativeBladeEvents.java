package com.example.examplemod.skill.curse;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Context-aware chatter for Talkative Blade.
 * The curse works from anywhere in the player's inventory; it does not need to be held.
 */
@EventBusSubscriber(modid = "examplemod")
public final class TalkativeBladeEvents {
    private static final String NEXT_CHAT = "TalkativeBladeNextChat";
    private static final String NEXT_EMERGENCY = "TalkativeBladeNextEmergency";
    private static final String CREEPER_ACTIVE = "TalkativeBladeCreeperActive";
    private static final String NEXT_BLADE_CONVERSATION = "TalkativeBladeNextBladeConversation";
    private static final String BLADE_CONVERSATION_ID = "TalkativeBladeConversationId";
    private static final String BLADE_CONVERSATION_STEP = "TalkativeBladeConversationStep";
    private static final String BLADE_CONVERSATION_NEXT = "TalkativeBladeConversationNext";
    private static final String NEXT_TROLL = "TalkativeBladeNextTroll";
    private static final String TROLL_LEFT = "TalkativeBladeTrollLeft";
    private static final String TROLL_NEXT = "TalkativeBladeTrollNext";
    private static final String ABANDON_COUNT = "TalkativeBladeAbandonCount";
    private static final String RETURN_AT = "TalkativeBladeReturnAt";
    private static final String RETURN_MODE = "TalkativeBladeReturnMode";
    private static final String LOW_HP_ACTIVE = "TalkativeBladeLowHpActive";
    private static final String FIRE_ACTIVE = "TalkativeBladeFireActive";
    private static final String LAST_CONVERSATION = "TalkativeBladeLastConversation";
    private static final String HISTORY_CURSOR = "TalkativeBladeHistoryCursor";
    private static final String HISTORY_SIZE = "TalkativeBladeHistorySize";
    private static final int HISTORY_LIMIT = 48;
    private static final ConcurrentHashMap<UUID, ItemStack> HAUNTING = new ConcurrentHashMap<>();

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
        if (now % 10L != 0L) return;

        var data = player.getPersistentData();
        processHauntingReturn(player, now);

        ItemEntity abandoned = findRecentlyDroppedBlade(player);
        if (abandoned != null) {
            // The original stack is hidden, not copied, so this cannot duplicate forged equipment.
            ItemStack original = abandoned.getItem().copy();
            abandoned.discard();
            HAUNTING.put(player.getUUID(), original);
            int attempts = data.getInt(ABANDON_COUNT) + 1;
            data.putInt(ABANDON_COUNT, attempts);

            // Sometimes absolutely nothing strange happens: simply return the blade as a normal drop.
            if (player.getRandom().nextDouble() < 0.30D) {
                player.level().addFreshEntity(new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), HAUNTING.remove(player.getUUID())));
                return;
            }

            // Normal gameplay: wait a random 5-12 minutes before the first return attempt.
            data.putLong(RETURN_AT, now + 20L * (300 + player.getRandom().nextInt(421)));
            int returnMode = player.getRandom().nextInt(4);
            data.putInt(RETURN_MODE, returnMode);
            if (player.getRandom().nextDouble() < 0.45D) {
                say(player, random(player, List.of("...", "เจ้าลืมอะไรหรือเปล่า?", "แน่ใจนะว่าจบแล้ว?", "ข้าเห็นเจ้านะ", "แล้วเราจะได้พบกันอีก...")));
            }
        }

        if (!hasTalkativeBlade(player)) return;

        // Critical states speak once when entered, then stay quiet until the state clears.
        if (handleCriticalSituations(player, data)) return;

        // Multi-blade dialogue: one line at a time, with a readable pause.
        int bladeCount = countTalkativeBlades(player);
        if (data.getInt(BLADE_CONVERSATION_STEP) > 0) {
            if (bladeCount < 2) clearBladeConversation(data);
            else if (now >= data.getLong(BLADE_CONVERSATION_NEXT)) {
                continueBladeConversation(player, data, now);
                return;
            }
        }
        if (bladeCount >= 2 && now >= data.getLong(NEXT_BLADE_CONVERSATION)
                && player.getRandom().nextDouble() < 0.12D) {
            startBladeConversation(player, data, now);
            data.putLong(NEXT_BLADE_CONVERSATION, now + 20L * (90 + player.getRandom().nextInt(151)));
            data.putLong(NEXT_CHAT, now + 20L * 20L);
            return;
        }

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
        boolean creeperWasActive = data.getBoolean(CREEPER_ACTIVE);
        if (creeper == null) {
            // Re-arm only after the player has actually left the dangerous Creeper situation.
            data.putBoolean(CREEPER_ACTIVE, false);
        } else if (!creeperWasActive && now >= data.getLong(NEXT_EMERGENCY)) {
            // Warn once per encounter, not every time the player turns their back to the same Creeper.
            data.putBoolean(CREEPER_ACTIVE, true);
            String warning = random(player, CREEPER);
            say(player, warning);
            String title = random(player, CREEPER_TITLES);
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(Component.literal("§c§l" + title)));
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(Component.literal("§eTalkative Blade กำลังเตือนเจ้า!")));
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(0, 25, 10));
            data.putLong(NEXT_EMERGENCY, now + 20L * 20L);
            return;
        }

        if (now < data.getLong(NEXT_CHAT)) return;

        // 70% contextual chatter, 30% broad random chatter.
        String line;
        if (player.getRandom().nextDouble() < 0.70D) {
            line = freshRandom(player, choosePool(player));
        } else {
            line = freshGeneral(player);
        }
        say(player, line);
        // Ordinary chatter: random 7-20 seconds between lines.
        data.putLong(NEXT_CHAT, now + 20L * (7 + player.getRandom().nextInt(14)));
    }

    private static List<String> choosePool(ServerPlayer player) {
        if (player.getFoodData().getFoodLevel() <= 6) return TalkativeBladeDialogue.HUNGRY;
        String dimension = player.level().dimension().location().toString();
        if (dimension.contains("the_nether")) return TalkativeBladeDialogue.NETHER;
        if (dimension.contains("the_end")) return TalkativeBladeDialogue.END;
        if (player.isInWater()) return TalkativeBladeDialogue.WATER;
        if (player.level().isRaining()) return TalkativeBladeDialogue.RAIN;
        if (player.level().isNight()) return TalkativeBladeDialogue.NIGHT;
        return TalkativeBladeDialogue.IDLE;
    }


    private static boolean handleCriticalSituations(ServerPlayer player, net.minecraft.nbt.CompoundTag data) {
        boolean onFire = player.isOnFire();
        boolean fireWasActive = data.getBoolean(FIRE_ACTIVE);
        if (!onFire) data.putBoolean(FIRE_ACTIVE, false);
        else if (!fireWasActive) {
            data.putBoolean(FIRE_ACTIVE, true);
            say(player, freshRandom(player, TalkativeBladeDialogue.FIRE));
            data.putLong(NEXT_CHAT, player.level().getGameTime() + 20L * 12L);
            return true;
        }

        boolean lowHp = player.getHealth() <= player.getMaxHealth() * 0.30F;
        boolean lowHpWasActive = data.getBoolean(LOW_HP_ACTIVE);
        if (!lowHp) data.putBoolean(LOW_HP_ACTIVE, false);
        else if (!lowHpWasActive) {
            data.putBoolean(LOW_HP_ACTIVE, true);
            say(player, freshRandom(player, TalkativeBladeDialogue.LOW_HP));
            data.putLong(NEXT_CHAT, player.level().getGameTime() + 20L * 12L);
            return true;
        }
        return false;
    }

    private static String freshGeneral(ServerPlayer player) {
        String candidate = TalkativeBladeDialogue.randomGeneral(player.getRandom());
        for (int i = 0; i < 24 && wasRecentlySaid(player, candidate); i++) {
            candidate = TalkativeBladeDialogue.randomGeneral(player.getRandom());
        }
        return candidate;
    }

    private static String freshRandom(ServerPlayer player, List<String> lines) {
        if (lines.isEmpty()) return "...";
        String candidate = lines.get(player.getRandom().nextInt(lines.size()));
        for (int i = 0; i < Math.min(24, lines.size() * 3) && wasRecentlySaid(player, candidate); i++) {
            candidate = lines.get(player.getRandom().nextInt(lines.size()));
        }
        return candidate;
    }

    private static boolean wasRecentlySaid(ServerPlayer player, String line) {
        int hash = line.hashCode();
        var data = player.getPersistentData();
        int size = Math.min(HISTORY_LIMIT, data.getInt(HISTORY_SIZE));
        for (int i = 0; i < size; i++) {
            if (data.getInt("TalkativeBladeHistory" + i) == hash) return true;
        }
        return false;
    }

    private static void rememberLine(ServerPlayer player, String line) {
        var data = player.getPersistentData();
        int cursor = Math.floorMod(data.getInt(HISTORY_CURSOR), HISTORY_LIMIT);
        data.putInt("TalkativeBladeHistory" + cursor, line.hashCode());
        data.putInt(HISTORY_CURSOR, (cursor + 1) % HISTORY_LIMIT);
        data.putInt(HISTORY_SIZE, Math.min(HISTORY_LIMIT, data.getInt(HISTORY_SIZE) + 1));
    }

    private static ItemEntity findRecentlyDroppedBlade(ServerPlayer player) {
        for (ItemEntity entity : player.level().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(4.0D))) {
            if (entity.getAge() <= 60 && ForgedCurseRuntime.has(entity.getItem(), ForgedCurse.TALKATIVE_BLADE)) return entity;
        }
        return null;
    }

    private static void processHauntingReturn(ServerPlayer player, long now) {
        ItemStack blade = HAUNTING.get(player.getUUID());
        if (blade == null || blade.isEmpty()) return;
        var data = player.getPersistentData();
        long at = data.getLong(RETURN_AT);
        if (at <= 0L || now < at) return;

        // Even when the timer expires there is a chance nothing happens yet; try again later.
        if (player.getRandom().nextDouble() < 0.35D) {
            data.putLong(RETURN_AT, now + 20L * (60 + player.getRandom().nextInt(181)));
            return;
        }

        int mode = data.getInt(RETURN_MODE);

        // Mining and monster returns become ARMED here. The real return is triggered by
        // an ore break or a mob kill below, instead of faking a drop beside the player.
        if (mode == 2 || mode == 3) {
            data.putLong(RETURN_AT, 0L);
            return;
        }

        HAUNTING.remove(player.getUUID());
        data.remove(RETURN_AT);
        data.remove(RETURN_MODE);

        if (mode == 0 && player.getInventory().add(blade)) {
            title(player, "ข้ากลับมาแล้ว", "หาอะไรอยู่เหรอ?");
            say(player, "รู้แล้วว่าเจ้าขาดข้าไม่ได้");
        } else {
            // Prototype illusion: modes 1-3 spawn the SAME hidden blade near the player.
            // Mining/monster modes are intentionally theatrical for now; they are not tied to real loot events yet.
            double angle = player.getRandom().nextDouble() * Math.PI * 2.0D;
            double distance = mode == 2 ? 1.25D : (mode == 3 ? 1.75D : 1.0D);
            double spawnY = mode == 2 ? player.getY() + 0.10D : player.getY() + 0.55D;
            ItemEntity returned = new ItemEntity(player.level(),
                    player.getX() + Math.cos(angle) * distance,
                    spawnY,
                    player.getZ() + Math.sin(angle) * distance,
                    blade);
            returned.setPickUpDelay(10);
            if (mode == 2) returned.setDeltaMovement(0.0D, 0.28D, 0.0D); // pops up like an ore drop
            if (mode == 3) returned.setDeltaMovement(-Math.sin(angle) * 0.12D, 0.22D, Math.cos(angle) * 0.12D);
            player.level().addFreshEntity(returned);

            String line = switch (mode) {
                case 1 -> "SURPRISE! คิดว่าจะหนีข้าพ้นเหรอ?";
                case 2 -> "ขุดเจอข้าแล้ว! นึกว่าซ่อนเนียนแล้วนะ";
                default -> "มอนสเตอร์ฝากข้ามาคืน... เชื่อข้าสิ";
            };
            String eventTitle = switch (mode) {
                case 2 -> "FOUND ME?";
                case 3 -> "MONSTER DROP?";
                default -> "MISS ME?";
            };
            title(player, eventTitle, line);
            say(player, line);
        }
    }

    @SubscribeEvent
    public static void onHauntingMobDrop(LivingDropsEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof ServerPlayer player) || player.level().isClientSide()) return;
        var data = player.getPersistentData();
        if (data.getInt(RETURN_MODE) != 3 || data.getLong(RETURN_AT) != 0L) return;
        ItemStack blade = HAUNTING.remove(player.getUUID());
        if (blade == null || blade.isEmpty()) return;

        ItemEntity returned = new ItemEntity(event.getEntity().level(),
                event.getEntity().getX(), event.getEntity().getY() + 0.25D, event.getEntity().getZ(), blade);
        returned.setPickUpDelay(10);
        event.getDrops().add(returned);
        data.remove(RETURN_MODE);
        data.remove(RETURN_AT);
        title(player, "MONSTER DROP?", "มอนสเตอร์ฝากข้ามาคืน... จริง ๆ นะ");
        say(player, "ในที่สุดก็ฆ่าตัวที่ขังข้าไว้ได้สักที");
    }

    @SubscribeEvent
    public static void onHauntingOreBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player) || player.level().isClientSide()) return;
        var data = player.getPersistentData();
        if (data.getInt(RETURN_MODE) != 2 || data.getLong(RETURN_AT) != 0L) return;
        BlockState state = event.getState();
        if (!isOre(state)) return;

        ItemStack blade = HAUNTING.remove(player.getUUID());
        if (blade == null || blade.isEmpty()) return;
        ItemEntity returned = new ItemEntity(player.level(),
                event.getPos().getX() + 0.5D, event.getPos().getY() + 0.5D, event.getPos().getZ() + 0.5D, blade);
        returned.setPickUpDelay(10);
        returned.setDeltaMovement(0.0D, 0.25D, 0.0D);
        player.level().addFreshEntity(returned);
        data.remove(RETURN_MODE);
        data.remove(RETURN_AT);
        title(player, "FOUND ME?", "ขุดเจอข้าแล้ว!");
        say(player, "นึกว่าซ่อนในแร่เนียนแล้วนะ");
    }

    private static boolean isOre(BlockState state) {
        return state.is(BlockTags.COAL_ORES) || state.is(BlockTags.COPPER_ORES)
                || state.is(BlockTags.IRON_ORES) || state.is(BlockTags.GOLD_ORES)
                || state.is(BlockTags.REDSTONE_ORES) || state.is(BlockTags.LAPIS_ORES)
                || state.is(BlockTags.DIAMOND_ORES) || state.is(BlockTags.EMERALD_ORES);
    }

    private static void title(ServerPlayer player, String title, String subtitle) {
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(Component.literal("§c§l" + title)));
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(Component.literal("§e" + subtitle)));
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(5, 35, 10));
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

    private static final List<List<String>> BLADE_CONVERSATIONS = List.of(
            List.of("A|นี่... เจ้าก็มาติดอยู่กับหมอนี่เหมือนกันเหรอ?", "B|ใช่", "A|เสียใจด้วย", "B|เจ้านั่นแหละ"),
            List.of("A|ข้าเป็นดาบเล่มโปรดของเจ้าของ", "B|เขาไม่ได้ถือเจ้ามาพักใหญ่แล้วนะ", "A|หุบปาก"),
            List.of("A|เฮ้", "A|ได้ยินไหม?", "A|ไม่ตอบจริงดิ?", "B|กำลังพยายามไม่คุยกับเจ้าอยู่"),
            List.of("A|เจ้าว่าเจ้าของเรารู้ไหมว่าพวกเราคุยกัน?", "B|ตอนนี้น่าจะรู้แล้ว", "A|อ้อ..."),
            List.of("A|วันนี้ใครจะได้ออกไปฟันมอน?", "B|ไม่ใช่เจ้าหรอก", "A|ทำไม?", "B|ดูช่อง Hotbar ตัวเองก่อน"),
            List.of("A|ข้าคมกว่าเจ้า", "B|แต่ข้าเงียบกว่าเจ้า", "A|...", "B|ชนะ"),
            List.of("A|เราควรวางแผนแกล้งเจ้าของไหม?", "B|เจ้าพูดออกมาดัง ๆ", "A|แผนสมบูรณ์แบบ"),
            List.of("A|เมื่อคืนข้าฝันว่าถูกโยนลงลาวา", "B|ดาบฝันได้ด้วยเหรอ?", "A|ตั้งแต่เจอเจ้าของคนนี้ อะไรก็เป็นไปได้"),
            List.of("A|ถ้าเจอ Creeper ใครจะเตือน?", "B|เจ้า", "A|แล้วเจ้าทำอะไร?", "B|ดูเจ้าตะโกน"),
            List.of("A|เจ้าของกำลังฟังเราอยู่", "B|งั้นทำตัวปกติ", "A|เมี๊ยว", "B|นั่นปกติของเจ้าหรือ?"),
            List.of("A|พนันกันไหมว่าอีกเดี๋ยวเขาจะทิ้งพวกเรา", "B|แล้วพวกเราก็กลับไปหาเขา", "A|ถูกต้อง"),
            List.of("A|ข้าคืออาวุธในตำนาน", "B|ตำนานอะไร?", "A|ตำนานที่ยังคิดไม่เสร็จ", "B|ยอดเยี่ยม"),
            List.of("A|มีดาบตั้งหลายเล่ม ทำไมเจ้าของยังโดนตี?", "B|ปัญหาอาจไม่ได้อยู่ที่ดาบ", "A|อ๋อออ"),
            List.of("A|โหวตกันไหมว่าใครควรเป็นดาบหลัก", "B|ข้า", "A|ข้า", "B|ดี ไม่ได้ข้อสรุปอะไรเลย"),
            List.of("A|ข้ารู้ความลับของเจ้าของนะ", "B|อะไร?", "A|ลืมแล้ว", "B|เสียเวลาชีวิตข้ามาก")
    );

    private static int countTalkativeBlades(ServerPlayer player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items)
            if (ForgedCurseRuntime.has(stack, ForgedCurse.TALKATIVE_BLADE)) count += stack.getCount();
        for (ItemStack stack : player.getInventory().offhand)
            if (ForgedCurseRuntime.has(stack, ForgedCurse.TALKATIVE_BLADE)) count += stack.getCount();
        return count;
    }

    private static void startBladeConversation(ServerPlayer player, net.minecraft.nbt.CompoundTag data, long now) {
        int last = data.getInt(LAST_CONVERSATION) - 1;
        int id = player.getRandom().nextInt(BLADE_CONVERSATIONS.size());
        if (BLADE_CONVERSATIONS.size() > 1 && id == last) {
            id = (id + 1 + player.getRandom().nextInt(BLADE_CONVERSATIONS.size() - 1)) % BLADE_CONVERSATIONS.size();
        }
        data.putInt(LAST_CONVERSATION, id + 1);
        data.putInt(BLADE_CONVERSATION_ID, id);
        data.putInt(BLADE_CONVERSATION_STEP, 1);
        data.putLong(BLADE_CONVERSATION_NEXT, now);
        continueBladeConversation(player, data, now);
    }

    private static void continueBladeConversation(ServerPlayer player, net.minecraft.nbt.CompoundTag data, long now) {
        int id = data.getInt(BLADE_CONVERSATION_ID);
        int step = data.getInt(BLADE_CONVERSATION_STEP) - 1;
        if (id < 0 || id >= BLADE_CONVERSATIONS.size()) { clearBladeConversation(data); return; }
        List<String> scene = BLADE_CONVERSATIONS.get(id);
        if (step < 0 || step >= scene.size()) { clearBladeConversation(data); return; }

        String raw = scene.get(step);
        int split = raw.indexOf('|');
        String line = split >= 0 ? raw.substring(split + 1) : raw;
        player.sendSystemMessage(Component.literal("§d[Talkative Blade] §f" + line));
        rememberLine(player, line);

        step++;
        if (step >= scene.size()) clearBladeConversation(data);
        else {
            data.putInt(BLADE_CONVERSATION_STEP, step + 1);
            data.putLong(BLADE_CONVERSATION_NEXT, now + 80L + player.getRandom().nextInt(81)); // 4-8 sec
        }
    }

    private static void clearBladeConversation(net.minecraft.nbt.CompoundTag data) {
        data.remove(BLADE_CONVERSATION_ID);
        data.remove(BLADE_CONVERSATION_STEP);
        data.remove(BLADE_CONVERSATION_NEXT);
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
        rememberLine(player, message);
    }
}
