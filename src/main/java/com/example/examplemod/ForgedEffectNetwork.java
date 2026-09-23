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

    public static void sendAction(int action) {
        PacketDistributor.sendToServer(new ActiveSkillPayload(action));
    }
}
