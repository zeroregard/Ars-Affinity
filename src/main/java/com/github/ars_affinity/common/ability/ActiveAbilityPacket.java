package com.github.ars_affinity.common.ability;

import com.github.ars_affinity.ArsAffinity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ActiveAbilityPacket() implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<ActiveAbilityPacket> TYPE = new CustomPacketPayload.Type<>(ArsAffinity.prefix("active_ability"));
    
    public static final StreamCodec<FriendlyByteBuf, ActiveAbilityPacket> STREAM_CODEC = StreamCodec.unit(new ActiveAbilityPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ActiveAbilityManager.triggerActiveAbility(serverPlayer);
            }
        });
    }

    public static void sendToServer() {
        // This would be called from the client to trigger the ability
        NetworkHandler.sendToServer(new ActiveAbilityPacket());
    }
} 