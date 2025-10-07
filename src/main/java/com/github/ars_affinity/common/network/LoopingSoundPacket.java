package com.github.ars_affinity.common.network;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.client.sound.LoopingSoundManager;
import com.hollingsworth.arsnouveau.common.network.AbstractPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public class LoopingSoundPacket extends AbstractPacket {
    public static final Type<LoopingSoundPacket> TYPE = new Type<>(ArsAffinity.prefix("looping_sound"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LoopingSoundPacket> CODEC = StreamCodec.ofMember(LoopingSoundPacket::toBytes, LoopingSoundPacket::new);
    
    private final int playerId;
    private final String soundId;
    private final boolean start;
    
    public LoopingSoundPacket(int playerId, String soundId, boolean start) {
        this.playerId = playerId;
        this.soundId = soundId;
        this.start = start;
    }
    
    public LoopingSoundPacket(RegistryFriendlyByteBuf buf) {
        this.playerId = buf.readVarInt();
        this.soundId = buf.readUtf();
        this.start = buf.readBoolean();
    }

    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(playerId);
        buf.writeUtf(soundId);
        buf.writeBoolean(start);
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    @Override
    public void onClientReceived(Minecraft minecraft, Player player) {
        Handle.handle(this, minecraft, player);
    }
    
    private static class Handle {
        public static void handle(LoopingSoundPacket packet, Minecraft minecraft, Player player) {
            if (minecraft.level != null && minecraft.level.isClientSide()) {
                Player targetPlayer = minecraft.level.getEntity(packet.playerId) instanceof Player p ? p : null;
                
                if (targetPlayer != null) {
                    if (packet.start) {
                        LoopingSoundManager.startLoopingSound(targetPlayer, packet.soundId);
                    } else {
                        LoopingSoundManager.stopLoopingSound(targetPlayer, packet.soundId);
                    }
                }
            }
        }
    }
}
