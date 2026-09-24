package com.example.examplemod;

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

    public static void sendAction(int action) {
        PacketDistributor.sendToServer(new ActiveSkillPayload(action));
    }
}
