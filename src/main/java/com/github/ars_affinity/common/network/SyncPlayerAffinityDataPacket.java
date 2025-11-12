package com.github.ars_affinity.common.network;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.PlayerAffinityData;
import com.github.ars_affinity.capability.PlayerAffinityDataProvider;
import com.github.ars_affinity.client.screen.perk.PerkTreeScreen;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import com.hollingsworth.arsnouveau.common.network.AbstractPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public class SyncPlayerAffinityDataPacket extends AbstractPacket {
    
    public static final CustomPacketPayload.Type<SyncPlayerAffinityDataPacket> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ArsAffinity.MOD_ID, "sync_player_affinity_data"));
    
    public static final StreamCodec<FriendlyByteBuf, SyncPlayerAffinityDataPacket> CODEC = 
        StreamCodec.ofMember(SyncPlayerAffinityDataPacket::encode, SyncPlayerAffinityDataPacket::new);
    
    private final Map<SpellSchool, Integer> schoolPoints;
    private final Map<SpellSchool, Float> schoolPercentages;
    private final CompoundTag serializedData;
    
    public SyncPlayerAffinityDataPacket(PlayerAffinityData data, Player player) {
        this.schoolPoints = data.getAllSchoolPoints();
        this.schoolPercentages = data.getAllSchoolPercentages();
        this.serializedData = data.serializeNBT(player.level().registryAccess());
    }
    
    public SyncPlayerAffinityDataPacket(FriendlyByteBuf buffer) {
        this.schoolPoints = new HashMap<>();
        this.schoolPercentages = new HashMap<>();
        
        int pointsCount = buffer.readInt();
        for (int i = 0; i < pointsCount; i++) {
            String schoolId = buffer.readUtf();
            int points = buffer.readInt();
            SpellSchool school = getSchoolFromId(schoolId);
            if (school != null) {
                schoolPoints.put(school, points);
            }
        }
        
        int percentagesCount = buffer.readInt();
        for (int i = 0; i < percentagesCount; i++) {
            String schoolId = buffer.readUtf();
            float percentage = buffer.readFloat();
            SpellSchool school = getSchoolFromId(schoolId);
            if (school != null) {
                schoolPercentages.put(school, percentage);
            }
        }
        
        this.serializedData = buffer.readNbt();
    }
    
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(schoolPoints.size());
        for (Map.Entry<SpellSchool, Integer> entry : schoolPoints.entrySet()) {
            buffer.writeUtf(entry.getKey().getId().toString());
            buffer.writeInt(entry.getValue());
        }
        
        buffer.writeInt(schoolPercentages.size());
        for (Map.Entry<SpellSchool, Float> entry : schoolPercentages.entrySet()) {
            buffer.writeUtf(entry.getKey().getId().toString());
            buffer.writeFloat(entry.getValue());
        }
        
        buffer.writeNbt(serializedData);
    }
    
    @Override
    public void onClientReceived(Minecraft minecraft, Player player) {
        if (player == null) {
            ArsAffinity.LOGGER.warn("Cannot sync affinity data - player is null");
            return;
        }
        
        PlayerAffinityData clientData = PlayerAffinityDataProvider.getPlayerAffinityData(player);
        if (clientData != null && serializedData != null) {
            try {
                clientData.deserializeNBT(player.level().registryAccess(), serializedData);
                ArsAffinity.LOGGER.info("Successfully synced affinity data to client for player {}", 
                    player.getName().getString());
                minecraft.execute(() -> {
                    if (minecraft.screen instanceof PerkTreeScreen screen) {
                        screen.refreshData();
                    }
                });
            } catch (Exception e) {
                ArsAffinity.LOGGER.error("Failed to deserialize affinity data on client: {}", e.getMessage(), e);
            }
        }
    }
    
    @Override
    public void onServerReceived(MinecraftServer server, ServerPlayer player) {
    }
    
    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    private static SpellSchool getSchoolFromId(String id) {
        return switch (id) {
            case "ars_nouveau:elemental_fire" -> SpellSchools.ELEMENTAL_FIRE;
            case "ars_nouveau:elemental_water" -> SpellSchools.ELEMENTAL_WATER;
            case "ars_nouveau:elemental_earth" -> SpellSchools.ELEMENTAL_EARTH;
            case "ars_nouveau:elemental_air" -> SpellSchools.ELEMENTAL_AIR;
            case "ars_nouveau:abjuration" -> SpellSchools.ABJURATION;
            case "ars_nouveau:necromancy" -> SpellSchools.NECROMANCY;
            case "ars_nouveau:conjuration" -> SpellSchools.CONJURATION;
            case "ars_nouveau:manipulation" -> SpellSchools.MANIPULATION;
            default -> null;
        };
    }
}


