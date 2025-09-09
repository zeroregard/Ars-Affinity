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
    public static final StreamCodec<RegistryFriendlyByteBuf, ParticleEffectPacket> CODEC = StreamCodec.ofMember(ParticleEffectPacket::toBytes, ParticleEffectPacket::new);
    
    private final int playerId;
    private final String schoolId;
    private final int particleCount;
    
    public ParticleEffectPacket(int playerId, String schoolId, int particleCount) {
        this.playerId = playerId;
        this.schoolId = schoolId;
        this.particleCount = particleCount;
    }
    
    public ParticleEffectPacket(RegistryFriendlyByteBuf buf) {
        this.playerId = buf.readVarInt();
        this.schoolId = buf.readUtf();
        this.particleCount = buf.readVarInt();
    }

    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(playerId);
        buf.writeUtf(schoolId);
        buf.writeVarInt(particleCount);
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
        public static void handle(ParticleEffectPacket packet, Minecraft minecraft, Player player) {
            ArsAffinity.LOGGER.info("=== PARTICLE EFFECT PACKET RECEIVED ===");
            ArsAffinity.LOGGER.info("ParticleEffectPacket.handle called with playerId={}, schoolId={}, particleCount={}", 
                packet.playerId, packet.schoolId, packet.particleCount);
            ArsAffinity.LOGGER.info("Minecraft level: {}, isClientSide: {}", 
                minecraft.level != null ? minecraft.level.dimension().location() : "null", 
                minecraft.level != null ? minecraft.level.isClientSide() : "null");
                
            if (minecraft.level != null && minecraft.level.isClientSide()) {
                ArsAffinity.LOGGER.info("ParticleEffectPacket: Client-side level confirmed");
                
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
