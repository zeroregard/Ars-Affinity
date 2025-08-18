package com.github.ars_affinity.common.ability;

import com.github.ars_affinity.ArsAffinity;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToServer(
                ActiveAbilityPacket.TYPE,
                ActiveAbilityPacket.STREAM_CODEC,
                ActiveAbilityPacket::handleData
        );

        registrar.playToServer(
                com.github.ars_affinity.event.SanctuaryKeyReleasePacket.TYPE,
                com.github.ars_affinity.event.SanctuaryKeyReleasePacket.STREAM_CODEC,
                com.github.ars_affinity.event.SanctuaryKeyReleasePacket::handleData
        );
    }

    public static void sendToServer(CustomPacketPayload msg) {
        PacketDistributor.sendToServer(msg);
    }
} 