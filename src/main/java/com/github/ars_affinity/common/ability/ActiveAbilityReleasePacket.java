package com.github.ars_affinity.common.ability;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.common.ability.field.ActiveFieldRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ActiveAbilityReleasePacket() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ActiveAbilityReleasePacket> TYPE = new CustomPacketPayload.Type<>(ArsAffinity.prefix("active_ability_release"));
    public static final StreamCodec<FriendlyByteBuf, ActiveAbilityReleasePacket> STREAM_CODEC = StreamCodec.unit(new ActiveAbilityReleasePacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ActiveFieldRegistry.stop(serverPlayer);
            }
        });
    }
}


