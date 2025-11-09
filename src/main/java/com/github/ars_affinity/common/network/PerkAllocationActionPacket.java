package com.github.ars_affinity.common.network;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.PerkAllocationManager;
import com.hollingsworth.arsnouveau.common.network.AbstractPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class PerkAllocationActionPacket extends AbstractPacket {

    public static final CustomPacketPayload.Type<PerkAllocationActionPacket> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ArsAffinity.MOD_ID, "perk_allocation_action"));

    public static final StreamCodec<FriendlyByteBuf, PerkAllocationActionPacket> CODEC =
        StreamCodec.ofMember(PerkAllocationActionPacket::encode, PerkAllocationActionPacket::new);

    private final String perkId;
    private final boolean allocate;

    public PerkAllocationActionPacket(String perkId, boolean allocate) {
        this.perkId = perkId;
        this.allocate = allocate;
    }

    public PerkAllocationActionPacket(FriendlyByteBuf buffer) {
        this.perkId = buffer.readUtf();
        this.allocate = buffer.readBoolean();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(perkId);
        buffer.writeBoolean(allocate);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, Player player) {
    }

    @Override
    public void onServerReceived(MinecraftServer server, ServerPlayer player) {
        server.execute(() -> {
            if (allocate) {
                PerkAllocationManager.allocatePoints(player, perkId);
            } else {
                PerkAllocationManager.deallocatePerk(player, perkId);
            }
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

