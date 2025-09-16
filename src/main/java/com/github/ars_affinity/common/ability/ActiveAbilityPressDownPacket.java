package com.github.ars_affinity.common.ability;

import com.github.ars_affinity.ArsAffinity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ActiveAbilityPressDownPacket() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ActiveAbilityPressDownPacket> TYPE = new CustomPacketPayload.Type<>(ArsAffinity.prefix("active_ability_press"));
    public static final StreamCodec<FriendlyByteBuf, ActiveAbilityPressDownPacket> STREAM_CODEC = StreamCodec.unit(new ActiveAbilityPressDownPacket());

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
}


