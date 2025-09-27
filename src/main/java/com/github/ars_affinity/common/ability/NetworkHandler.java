package com.github.ars_affinity.common.ability;

import com.github.ars_affinity.ArsAffinity;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
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
                ActiveAbilityPressDownPacket.TYPE,
                ActiveAbilityPressDownPacket.STREAM_CODEC,
                ActiveAbilityPressDownPacket::handleData
        );

        registrar.playToServer(
                ActiveAbilityReleasePacket.TYPE,
                ActiveAbilityReleasePacket.STREAM_CODEC,
                ActiveAbilityReleasePacket::handleData
        );
    }

    public static void sendToServer(CustomPacketPayload msg) {
        PacketDistributor.sendToServer(msg);
    }
<<<<<<< HEAD
    
    public static void sendToAllClients(CustomPacketPayload msg) {
        PacketDistributor.sendToAllPlayers(msg);
    }
=======
>>>>>>> 00bcb64105ef9f2f558979be9b6a22b8028fac00
} 