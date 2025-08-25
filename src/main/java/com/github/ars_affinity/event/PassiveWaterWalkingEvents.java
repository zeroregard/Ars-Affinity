package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PassiveWaterWalkingEvents {
    
    private static final double WATER_WALKING_HEIGHT_OFFSET = 0.1; // Slight offset above water surface
    private static final double LOOK_DOWN_THRESHOLD = 87.0; // 90 degrees minus 3 degrees tolerance
    private static final int WATER_CHECK_RADIUS = 1; // Check surrounding blocks for water stability
    private static final double POSITION_SMOOTHING_FACTOR = 0.8; // Smoothing factor for position changes
    private static final int MIN_TICKS_BETWEEN_CHECKS = 2; // Minimum ticks between water checks
    private static final double MAX_SPEED_MULTIPLIER = 3.0; // Maximum speed multiplier to prevent excessive speed
    
    // Track player states to prevent jittery movement
    private static final Map<UUID, PlayerWaterWalkingState> playerStates = new HashMap<>();
    
    private static class PlayerWaterWalkingState {
        boolean wasWaterWalking = false;
        Vec3 lastWaterPosition = null;
        int ticksSinceLastCheck = 0;
        double smoothedY = 0.0;
        Vec3 lastMovement = null;
        
        void reset() {
            wasWaterWalking = false;
            lastWaterPosition = null;
            ticksSinceLastCheck = 0;
            smoothedY = 0.0;
            lastMovement = null;
        }
    }
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        
        Player player = event.player;
        if (player.level().isClientSide()) return;
        
        // Skip if player is in creative mode or spectator mode
        if (player.isCreative() || player.isSpectator()) return;
        
        // Get player's affinity progress
        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress == null) return;
        
        int waterTier = progress.getTier(SpellSchools.ELEMENTAL_WATER);
        if (waterTier <= 0) return;
        
        // Check if player should have water walking ability
        AffinityPerkHelper.applyHighestTierPerk(progress, waterTier, SpellSchools.ELEMENTAL_WATER, AffinityPerkType.PASSIVE_WATER_WALKING, perk -> {
            if (perk instanceof AffinityPerk.AmountBasedPerk amountPerk) {
                handleWaterWalking(player, amountPerk.amount);
            }
        });
    }
    
    private static void handleWaterWalking(Player player, float speedBonus) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();
        UUID playerId = player.getUUID();
        
        // Get or create player state
        PlayerWaterWalkingState state = playerStates.computeIfAbsent(playerId, k -> new PlayerWaterWalkingState());
        
        // Increment tick counter
        state.ticksSinceLastCheck++;
        
        // Only check water every few ticks to improve performance
        if (state.ticksSinceLastCheck < MIN_TICKS_BETWEEN_CHECKS) {
            // Still apply effects if we were water walking
            if (state.wasWaterWalking) {
                applyWaterWalkingEffect(player, level, playerPos, speedBonus, state);
            }
            return;
        }
        
        state.ticksSinceLastCheck = 0;
        
        // Check if player is above stable water
        boolean isAboveWater = isPlayerAboveStableWater(player, level, playerPos);
        
        // Check if player is crouching
        boolean isCrouching = player.isCrouching();
        
        // Check if player is looking down (within 3 degrees of 90 degrees)
        boolean isLookingDown = isPlayerLookingDown(player);
        
        // Check if player is flying (creative mode or elytra)
        boolean isFlying = player.isFallFlying() || player.abilities.flying;
        
        // Determine if water walking should be active
        boolean shouldWaterWalk = isAboveWater && !isCrouching && !isLookingDown && !isFlying;
        
        if (shouldWaterWalk) {
            if (!state.wasWaterWalking) {
                // Just started water walking
                state.lastWaterPosition = player.position();
                state.smoothedY = player.position().y;
                ArsAffinity.LOGGER.debug("Player {} started water walking", player.getName().getString());
            }
            
            // Apply water walking effect
            applyWaterWalkingEffect(player, level, playerPos, speedBonus, state);
            state.wasWaterWalking = true;
        } else {
            if (state.wasWaterWalking) {
                // Just stopped water walking
                ArsAffinity.LOGGER.debug("Player {} stopped water walking", player.getName().getString());
            }
            state.wasWaterWalking = false;
        }
    }
    
    private static boolean isPlayerAboveStableWater(Player player, Level level, BlockPos playerPos) {
        // Check the block below the player
        BlockPos belowPos = playerPos.below();
        BlockState belowState = level.getBlockState(belowPos);
        
        // Check if it's a water block
        if (belowState.is(Blocks.WATER)) {
            // Check if it's a full water block (not flowing)
            FluidState fluidState = belowState.getFluidState();
            if (fluidState.is(Fluids.WATER) && fluidState.isSource()) {
                // Additional check: ensure there's stable water around (not just a single water block)
                return hasStableWaterSurrounding(level, belowPos);
            }
        }
        
        return false;
    }
    
    private static boolean hasStableWaterSurrounding(Level level, BlockPos centerPos) {
        // Check if there are multiple water blocks around to ensure stability
        int stableWaterCount = 0;
        
        for (int x = -WATER_CHECK_RADIUS; x <= WATER_CHECK_RADIUS; x++) {
            for (int z = -WATER_CHECK_RADIUS; z <= WATER_CHECK_RADIUS; z++) {
                BlockPos checkPos = centerPos.offset(x, 0, z);
                BlockState checkState = level.getBlockState(checkPos);
                
                if (checkState.is(Blocks.WATER)) {
                    FluidState fluidState = checkState.getFluidState();
                    if (fluidState.is(Fluids.WATER) && fluidState.isSource()) {
                        stableWaterCount++;
                        // If we have at least 3 stable water blocks, consider it stable enough
                        if (stableWaterCount >= 3) {
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    private static boolean isPlayerLookingDown(Player player) {
        // Get player's pitch (looking up/down angle)
        float pitch = player.getXRot();
        
        // Minecraft pitch: -90 (looking up) to 90 (looking down)
        // We want to check if player is looking down within 3 degrees of straight down (90)
        return pitch >= LOOK_DOWN_THRESHOLD;
    }
    
    private static void applyWaterWalkingEffect(Player player, Level level, BlockPos playerPos, float speedBonus, PlayerWaterWalkingState state) {
        BlockPos waterPos = playerPos.below();
        BlockState waterState = level.getBlockState(waterPos);
        
        if (waterState.is(Blocks.WATER)) {
            // Calculate the water surface Y coordinate
            double waterSurfaceY = waterPos.getY() + 1.0; // Water blocks are 1 block tall
            double targetY = waterSurfaceY + WATER_WALKING_HEIGHT_OFFSET;
            
            // Smooth the Y position to prevent jittery movement
            state.smoothedY = state.smoothedY * POSITION_SMOOTHING_FACTOR + targetY * (1.0 - POSITION_SMOOTHING_FACTOR);
            
            // Set player position to hover above water surface
            Vec3 currentPos = player.position();
            Vec3 newPos = new Vec3(currentPos.x, state.smoothedY, currentPos.z);
            
            // Only update Y position if player is below or at water surface
            if (currentPos.y <= targetY + 0.5) { // Add some tolerance
                player.setPos(newPos);
                
                // Prevent falling
                if (player.getDeltaMovement().y < 0) {
                    player.setDeltaMovement(player.getDeltaMovement().multiply(1.0, 0.0, 1.0));
                }
            }
            
            // Apply movement speed bonus
            if (speedBonus > 0) {
                // Store the last movement for comparison
                Vec3 currentMovement = player.getDeltaMovement();
                
                // Only apply speed bonus if player is actively moving
                if (currentMovement.horizontalDistanceSqr() > 0.01) {
                    // Apply speed bonus to horizontal movement only
                    double speedMultiplier = Math.min(1.0 + speedBonus, MAX_SPEED_MULTIPLIER);
                    Vec3 newMovement = new Vec3(
                        currentMovement.x * speedMultiplier,
                        currentMovement.y,
                        currentMovement.z * speedMultiplier
                    );
                    
                    // Store the modified movement
                    state.lastMovement = newMovement;
                    player.setDeltaMovement(newMovement);
                }
            }
            
            // Log water walking activation (for debugging)
            if (ArsAffinity.LOGGER.isDebugEnabled()) {
                ArsAffinity.LOGGER.debug("Player {} is water walking with {}% speed bonus", 
                    player.getName().getString(), (int)(speedBonus * 100));
            }
        }
    }
    
    // Clean up player states when they log out
    public static void cleanupPlayerState(UUID playerId) {
        playerStates.remove(playerId);
    }
    
    // Clean up all player states when server stops
    public static void cleanupAllPlayerStates() {
        playerStates.clear();
    }
}