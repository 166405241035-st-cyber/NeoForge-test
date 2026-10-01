package com.example.examplemod.skill;

import com.example.examplemod.*;
import com.example.examplemod.block.*;
import com.example.examplemod.entity.*;
import com.example.examplemod.item.*;
import com.example.examplemod.skill.*;
import com.example.examplemod.skill.client.*;
import com.example.examplemod.guide.ForgingGuideJournal;
import com.example.examplemod.guide.ForgingGuideClientState;
import com.example.examplemod.AnvilRewardSession;
import net.minecraft.server.level.ServerPlayer;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
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

    public record ForgingRewardPayload(ItemStack stack) implements CustomPacketPayload {
        public static final Type<ForgingRewardPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "forging_reward"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ForgingRewardPayload> STREAM_CODEC =
                ItemStack.STREAM_CODEC.map(ForgingRewardPayload::new, ForgingRewardPayload::stack);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record AnvilStartPayload(ItemStack assembly) implements CustomPacketPayload {
        public static final Type<AnvilStartPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "anvil_start"));
        public static final StreamCodec<RegistryFriendlyByteBuf, AnvilStartPayload> STREAM_CODEC =
                ItemStack.STREAM_CODEC.map(AnvilStartPayload::new, AnvilStartPayload::assembly);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record AnvilFinishPayload(int accuracyHundredths, int maxCombo, int perfectCount, int misses) implements CustomPacketPayload {
        public static final Type<AnvilFinishPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "anvil_finish"));
        public static final StreamCodec<ByteBuf, AnvilFinishPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, AnvilFinishPayload::accuracyHundredths,
                ByteBufCodecs.VAR_INT, AnvilFinishPayload::maxCombo,
                ByteBufCodecs.VAR_INT, AnvilFinishPayload::perfectCount,
                ByteBufCodecs.VAR_INT, AnvilFinishPayload::misses,
                AnvilFinishPayload::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record AnvilResultPayload(ItemStack equipment, String rank, int chancePermille) implements CustomPacketPayload {
        public static final Type<AnvilResultPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "anvil_result"));
        public static final StreamCodec<RegistryFriendlyByteBuf, AnvilResultPayload> STREAM_CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC, AnvilResultPayload::equipment,
                ByteBufCodecs.STRING_UTF8, AnvilResultPayload::rank,
                ByteBufCodecs.VAR_INT, AnvilResultPayload::chancePermille,
                AnvilResultPayload::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record GuideRequestPayload() implements CustomPacketPayload {
        public static final Type<GuideRequestPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "guide_request"));
        public static final StreamCodec<ByteBuf, GuideRequestPayload> STREAM_CODEC =
                StreamCodec.unit(new GuideRequestPayload());
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record GuideSnapshotPayload(String discoveries) implements CustomPacketPayload {
        public static final Type<GuideSnapshotPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "guide_snapshot"));
        public static final StreamCodec<ByteBuf, GuideSnapshotPayload> STREAM_CODEC =
                ByteBufCodecs.STRING_UTF8.map(GuideSnapshotPayload::new, GuideSnapshotPayload::discoveries);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
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
        registrar.playToClient(GuideSnapshotPayload.TYPE, GuideSnapshotPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ForgingGuideClientState.update(payload.discoveries())));
        registrar.playToClient(AnvilStartPayload.TYPE, AnvilStartPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    AnvilAssemblyResult assembly = AnvilAssemblyResult.fromStack(payload.assembly());
                    if (assembly != null) net.minecraft.client.Minecraft.getInstance().setScreen(
                            new AnvilRhythmForgingScreen(assembly));
                }));
        registrar.playToClient(AnvilResultPayload.TYPE, AnvilResultPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (net.minecraft.client.Minecraft.getInstance().screen instanceof AnvilRhythmForgingScreen screen)
                        screen.showResult(payload.equipment(), payload.rank(), payload.chancePermille() / 1000.0D);
                }));
        registrar.playToServer(AnvilFinishPayload.TYPE, AnvilFinishPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) AnvilRewardSession.finish(player,
                            payload.accuracyHundredths(), payload.maxCombo(), payload.perfectCount(), payload.misses());
                }));
        registrar.playToServer(GuideRequestPayload.TYPE, GuideRequestPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        PacketDistributor.sendToPlayer(player, new GuideSnapshotPayload(ForgingGuideJournal.snapshot(player)));
                    }
                }));
        registrar.playToServer(
                ForgingRewardPayload.TYPE,
                ForgingRewardPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() == null) return;
                    ItemStack stack = payload.stack();
                    if (stack.isEmpty() || stack.getCount() != 1) return;
                    // Anvil equipment is awarded separately by the server-owned anvil session.
                    if (!(stack.getItem() instanceof ForgedHeadItem)
                            && !(stack.getItem() instanceof ForgedCoreItem)
                            && !(stack.getItem() instanceof ForgedRodItem)) return;
                    if (!ForgeRewardSession.accept(context.player(), stack)) return;
                    ItemStack reward = stack.copy();
                    if (!context.player().getInventory().add(reward)) {
                        context.player().drop(reward, false);
                    }
                    ForgingGuideJournal.record(context.player(), stack);
                })
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

    public static void sendForgingReward(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            PacketDistributor.sendToServer(new ForgingRewardPayload(stack.copy()));
        }
    }

    public static void finishAnvil(ForgingResult result) {
        PacketDistributor.sendToServer(new AnvilFinishPayload(
                (int) Math.round(result.accuracy() * 100), result.maxCombo(),
                result.perfectCount(), result.missCount()));
    }

    public static void requestGuide() {
        PacketDistributor.sendToServer(new GuideRequestPayload());
    }

    public static void sendAction(int action) {
        PacketDistributor.sendToServer(new ActiveSkillPayload(action));
    }
}
