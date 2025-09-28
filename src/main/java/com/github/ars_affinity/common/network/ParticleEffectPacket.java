package com.github.ars_affinity.common.network;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.client.particles.SpiralParticleHelper;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.common.network.AbstractPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public class ParticleEffectPacket extends AbstractPacket {
    public static final Type<ParticleEffectPacket> TYPE = new Type<>(ArsAffinity.prefix("particle_effect"));
    public static final Type<ParticleEffectPacket> UPDATE_TYPE = new Type<>(ArsAffinity.prefix("particle_effect_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ParticleEffectPacket> CODEC = StreamCodec.ofMember(ParticleEffectPacket::toBytes, ParticleEffectPacket::new);
    
    private final int playerId;
    private final String schoolId;
    private final int particleCount;
    private final double posX, posY, posZ;
    private final boolean isUpdate;
    
    // Constructor for initial spawn
    public ParticleEffectPacket(int playerId, String schoolId, int particleCount) {
        this(playerId, schoolId, particleCount, 0, 0, 0, false);
    }
    
    // Constructor for position updates
    public ParticleEffectPacket(int playerId, String schoolId, double posX, double posY, double posZ) {
        this(playerId, schoolId, 0, posX, posY, posZ, true);
    }
    
    // Full constructor
    private ParticleEffectPacket(int playerId, String schoolId, int particleCount, double posX, double posY, double posZ, boolean isUpdate) {
        this.playerId = playerId;
        this.schoolId = schoolId;
        this.particleCount = particleCount;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.isUpdate = isUpdate;
    }
    
    public ParticleEffectPacket(RegistryFriendlyByteBuf buf) {
        this.playerId = buf.readVarInt();
        this.schoolId = buf.readUtf();
        this.isUpdate = buf.readBoolean();
        if (isUpdate) {
            this.particleCount = 0;
            this.posX = buf.readDouble();
            this.posY = buf.readDouble();
            this.posZ = buf.readDouble();
        } else {
            this.particleCount = buf.readVarInt();
            this.posX = 0;
            this.posY = 0;
            this.posZ = 0;
        }
    }

    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(playerId);
        buf.writeUtf(schoolId);
        buf.writeBoolean(isUpdate);
        if (isUpdate) {
            buf.writeDouble(posX);
            buf.writeDouble(posY);
            buf.writeDouble(posZ);
        } else {
            buf.writeVarInt(particleCount);
        }
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return isUpdate ? UPDATE_TYPE : TYPE;
    }
    
    @Override
    public void onClientReceived(Minecraft minecraft, Player player) {
        Handle.handle(this, minecraft, player);
    }
    
    private static class Handle {
        public static void handle(ParticleEffectPacket packet, Minecraft minecraft, Player player) {
            ArsAffinity.LOGGER.info("=== PARTICLE EFFECT PACKET RECEIVED ===");
            ArsAffinity.LOGGER.info("ParticleEffectPacket.handle called with playerId={}, schoolId={}, isUpdate={}", 
                packet.playerId, packet.schoolId, packet.isUpdate);
            ArsAffinity.LOGGER.info("Minecraft level: {}, isClientSide: {}", 
                minecraft.level != null ? minecraft.level.dimension().location() : "null", 
                minecraft.level != null ? minecraft.level.isClientSide() : "null");
                
            if (minecraft.level != null && minecraft.level.isClientSide()) {
                ArsAffinity.LOGGER.info("ParticleEffectPacket: Client-side level confirmed");
                
                if (packet.isUpdate) {
                    // Handle position update - update the center of existing particles
                    ArsAffinity.LOGGER.debug("ParticleEffectPacket: Position update received: ({}, {}, {})", 
                        packet.posX, packet.posY, packet.posZ);
                    
                    SpiralParticleHelper.updateParticleCenter(packet.playerId, packet.schoolId, packet.posX, packet.posY, packet.posZ);
                } else {
                    // Handle initial spawn
                    Player targetPlayer = minecraft.level.getEntity(packet.playerId) instanceof Player p ? p : null;
                    
                    if (targetPlayer != null) {
                        ArsAffinity.LOGGER.info("ParticleEffectPacket: Target player found: {} at position ({}, {}, {})", 
                            targetPlayer.getName().getString(), targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ());
                        
                        SpellSchool school = getSchoolFromId(packet.schoolId);
                        if (school != null) {
                            ArsAffinity.LOGGER.info("ParticleEffectPacket: School found: {}, spawning {} particles", 
                                school.getId(), packet.particleCount);
                            ArsAffinity.LOGGER.info("ParticleEffectPacket: Calling SpiralParticleHelper.spawnSpiralParticles");
                                
                            try {
                                SpiralParticleHelper.spawnSpiralParticles(
                                    minecraft.level,
                                    targetPlayer,
                                    school,
                                    packet.particleCount
                                );
                                
                                ArsAffinity.LOGGER.info("ParticleEffectPacket: Particles spawned successfully");
                            } catch (Exception e) {
                                ArsAffinity.LOGGER.error("ParticleEffectPacket: Error spawning particles: {}", e.getMessage(), e);
                            }
                        } else {
                            ArsAffinity.LOGGER.warn("ParticleEffectPacket: School not found for ID: {}", packet.schoolId);
                        }
                    } else {
                        ArsAffinity.LOGGER.warn("ParticleEffectPacket: Target player not found for ID: {}", packet.playerId);
                    }
                }
            } else {
                ArsAffinity.LOGGER.warn("ParticleEffectPacket: Not on client side or level is null");
            }
            ArsAffinity.LOGGER.info("=== PARTICLE EFFECT PACKET HANDLING COMPLETE ===");
        }
        
        private static SpellSchool getSchoolFromId(String schoolId) {
            return switch (schoolId) {
                // Short format (legacy/fallback)
                case "fire" -> com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_FIRE;
                case "water" -> com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_WATER;
                case "earth" -> com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_EARTH;
                case "air" -> com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_AIR;
                case "abjuration" -> com.hollingsworth.arsnouveau.api.spell.SpellSchools.ABJURATION;
                case "necromancy" -> com.hollingsworth.arsnouveau.api.spell.SpellSchools.NECROMANCY;
                case "conjuration" -> com.hollingsworth.arsnouveau.api.spell.SpellSchools.CONJURATION;
                case "manipulation" -> com.hollingsworth.arsnouveau.api.spell.SpellSchools.MANIPULATION;
                default -> {
                    ArsAffinity.LOGGER.warn("ParticleEffectPacket: Unknown school ID: '{}'", schoolId);
                    yield null;
                }
            };
        }
    }
}
