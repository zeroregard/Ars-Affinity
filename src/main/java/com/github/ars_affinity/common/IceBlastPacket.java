package com.github.ars_affinity.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class IceBlastPacket {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation("ars_affinity", "ice_blast"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    public static void register() {
        INSTANCE.registerMessage(0, IceBlastPacket.class, IceBlastPacket::encode, IceBlastPacket::decode, IceBlastPacket::handle);
    }

    public void encode(FriendlyByteBuf buf) {
        // No data to encode for this simple packet
    }

    public static IceBlastPacket decode(FriendlyByteBuf buf) {
        return new IceBlastPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                // Trigger ICE BLAST ability on the server
                IceBlastEvents.triggerIceBlast(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}