package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.common.ability.NetworkHandler;
import com.github.ars_affinity.common.ability.field.ActiveFieldRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SanctuaryKeyReleasePacket() implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<SanctuaryKeyReleasePacket> TYPE = new CustomPacketPayload.Type<>(ArsAffinity.prefix("sanctuary_release"));
	public static final StreamCodec<FriendlyByteBuf, SanctuaryKeyReleasePacket> STREAM_CODEC = StreamCodec.unit(new SanctuaryKeyReleasePacket());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public void handleData(IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof net.minecraft.server.level.ServerPlayer sp) {
				ActiveFieldRegistry.stop(sp);
			}
		});
	}

	public static void sendToServer() {
		NetworkHandler.sendToServer(new SanctuaryKeyReleasePacket());
	}
}

