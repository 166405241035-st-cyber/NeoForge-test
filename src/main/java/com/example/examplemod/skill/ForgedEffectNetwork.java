package com.example.examplemod.skill;

import com.example.examplemod.*;
import com.example.examplemod.block.*;
import com.example.examplemod.entity.*;
import com.example.examplemod.item.*;
import com.example.examplemod.skill.*;
import com.example.examplemod.skill.client.*;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = ExampleMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ForgedEffectNetwork {
    private ForgedEffectNetwork() {}

    public record TimeStopShakePayload(int ticks) implements CustomPacketPayload {
        public static final Type<TimeStopShakePayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "time_stop_shake"));
        public static final StreamCodec<ByteBuf, TimeStopShakePayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, TimeStopShakePayload::ticks, TimeStopShakePayload::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record SkillHudStatePayload(int selectedIndex, String cooldownStates, long readyAt, long slamUntil, boolean aegisActive)
            implements CustomPacketPayload {
        public static final Type<SkillHudStatePayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "skill_hud_state"));
        public static final StreamCodec<ByteBuf, SkillHudStatePayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT, SkillHudStatePayload::selectedIndex,
                        ByteBufCodecs.STRING_UTF8, SkillHudStatePayload::cooldownStates,
                        ByteBufCodecs.VAR_LONG, SkillHudStatePayload::readyAt,
                        ByteBufCodecs.VAR_LONG, SkillHudStatePayload::slamUntil,
                        ByteBufCodecs.BOOL, SkillHudStatePayload::aegisActive,
                        SkillHudStatePayload::new
                );
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record ActiveSkillPayload(int action) implements CustomPacketPayload {
        public static final Type<ActiveSkillPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "active_skill"));

        public static final StreamCodec<ByteBuf, ActiveSkillPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        ActiveSkillPayload::action,
                        ActiveSkillPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                TimeStopShakePayload.TYPE,
                TimeStopShakePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ForgedEffectKeybinds.startTimeStopShake(payload.ticks()))
        );
        registrar.playToClient(
                SkillHudStatePayload.TYPE,
                SkillHudStatePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ForgedSkillHud.updateServerState(
                        payload.selectedIndex(), payload.cooldownStates(), payload.readyAt(),
                        payload.slamUntil(), payload.aegisActive()))
        );
        registrar.playToServer(
                ActiveSkillPayload.TYPE,
                ActiveSkillPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() != null) {
                        ForgedActiveSkills.handle(context.player(), payload.action());
                    }
                })
        );
    }

    public static void sendTimeStopShake(net.minecraft.server.level.ServerPlayer player, int ticks) {
        PacketDistributor.sendToPlayer(player, new TimeStopShakePayload(ticks));
    }

    public static void sendHudState(net.minecraft.server.level.ServerPlayer player,
                                    int selectedIndex, String cooldownStates, long readyAt,
                                    long slamUntil, boolean aegisActive) {
        PacketDistributor.sendToPlayer(player,
                new SkillHudStatePayload(selectedIndex, cooldownStates == null ? "" : cooldownStates,
                        Math.max(0L, readyAt), Math.max(0L, slamUntil), aegisActive));
    }

    public static void sendAction(int action) {
        PacketDistributor.sendToServer(new ActiveSkillPayload(action));
    }
}
