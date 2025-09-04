package com.github.ars_affinity.event;

import com.alexthw.sauce.registry.ModRegistry;
import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.config.ArsAffinityConfig;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.event.EventQueue;
import com.hollingsworth.arsnouveau.api.event.ITimedEvent;
import com.hollingsworth.arsnouveau.api.event.SummonEvent;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import com.hollingsworth.arsnouveau.common.entity.goal.FollowSummonerGoal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.player.Player;

import net.neoforged.bus.api.SubscribeEvent;

import java.lang.ref.WeakReference;

public class PassiveSummoningPowerEvents {



    @SubscribeEvent
    public static void onSummonEvent(SummonEvent event) {
        if (!(event.shooter instanceof Player player)) return;
        if (event.world.isClientSide()) return;
        if (event.summon.getLivingEntity() == null) {
            return;
        }

        AffinityPerkHelper.applyActivePerk(player, AffinityPerkType.PASSIVE_SUMMONING_POWER, AffinityPerk.DurationBasedPerk.class, durationPerk -> {
            // Apply SUMMON_POWER attribute boost to the PLAYER
            int summonPowerBonus = (int) durationPerk.amount;
            applySummonPowerBoostToPlayer(player, summonPowerBonus, durationPerk.time);

            // Apply extended distance override
            applyExtendedDistanceOverride(event.summon.getLivingEntity(), player, durationPerk.time, event.world);

            ArsAffinity.LOGGER.info("Player {} summoned entity with PASSIVE_SUMMONING_POWER perk (+{} power to player) for {} seconds, with extended distance control",
                player.getName().getString(), summonPowerBonus, durationPerk.time / 20);

        });
    }

    private static void applySummonPowerBoostToPlayer(Player player, int powerBonus, int durationTicks) {
        if (player == null) return;

        AttributeInstance attributeInstance = player.getAttribute(ModRegistry.SUMMON_POWER);
        
        if (attributeInstance != null) {
            // Create unique modifier ID for this player
            String modifierId = "ars_affinity_summoning_power_boost_" + player.getUUID();
            
            // Remove any existing modifier first to avoid stacking
            attributeInstance.removeModifier(ArsAffinity.prefix(modifierId));
            
            // Create modifier with unique ID
            AttributeModifier modifier = new AttributeModifier(
                ArsAffinity.prefix(modifierId),
                powerBonus,
                AttributeModifier.Operation.ADD_VALUE
            );
            
            // Add the modifier
            attributeInstance.addPermanentModifier(modifier);
            
            // Schedule removal - no need to track anything
            if (player.level() instanceof ServerLevel) {
                PowerBoostTimer timer = new PowerBoostTimer(player, durationTicks);
                EventQueue.getServerInstance().addEvent(timer);
            }
            
            ArsAffinity.LOGGER.info("Applied +{} SUMMON_POWER to player {} for {} ticks", powerBonus, player.getName().getString(), durationTicks);
        }
    }

    private static void removePlayerPowerBoost(Player player) {
        if (player == null) return;
        
        AttributeInstance attributeInstance = player.getAttribute(ModRegistry.SUMMON_POWER);
        if (attributeInstance != null) {
            // Create unique modifier ID for this player
            String modifierId = "ars_affinity_summoning_power_boost_" + player.getUUID();
            attributeInstance.removeModifier(ArsAffinity.prefix(modifierId));
            ArsAffinity.LOGGER.info("Removed SUMMON_POWER boost for player {}", player.getName().getString());
        }
    }



    private static void applyExtendedDistanceOverride(net.minecraft.world.entity.LivingEntity summon, Player player, int durationTicks, net.minecraft.world.level.Level level) {
        if (!(summon instanceof com.hollingsworth.arsnouveau.common.entity.IFollowingSummon followingSummon)) return;

        GoalSelector goalSelector = summon instanceof net.minecraft.world.entity.Mob mob ? mob.goalSelector : null;
        if (goalSelector == null) return;

        goalSelector.getAvailableGoals().removeIf(goal -> goal.getGoal() instanceof FollowSummonerGoal);

        CustomExtendedDistanceGoal extendedGoal = new CustomExtendedDistanceGoal(followingSummon, player, 1.0,
            ArsAffinityConfig.SWARM_SUMMON_DISTANCE_OVERRIDE_MIN_DISTANCE.get().floatValue(),
            ArsAffinityConfig.SWARM_SUMMON_DISTANCE_OVERRIDE_MAX_DISTANCE.get().floatValue());
        goalSelector.addGoal(2, extendedGoal);

        if (level instanceof ServerLevel) {
            DistanceOverrideTimer timer = new DistanceOverrideTimer(goalSelector, extendedGoal, durationTicks);
            EventQueue.getServerInstance().addEvent(timer);
        }
    }



    private static class PowerBoostTimer implements ITimedEvent {
        private final WeakReference<Player> playerRef;
        private int ticksRemaining;

        public PowerBoostTimer(Player player, int durationTicks) {
            this.playerRef = new WeakReference<>(player);
            this.ticksRemaining = durationTicks;
        }

        @Override
        public void tick(boolean serverSide) {
            if (serverSide) {
                ticksRemaining--;
                if (ticksRemaining <= 0) {
                    Player player = playerRef.get();
                    if (player != null) {
                        removePlayerPowerBoost(player);
                    }
                }
            }
        }

        @Override
        public boolean isExpired() {
            return ticksRemaining <= 0 || playerRef.get() == null;
        }
    }

    private static class DistanceOverrideTimer implements ITimedEvent {
        private final GoalSelector goalSelector;
        private final Goal customGoal;
        private int ticksRemaining;

        public DistanceOverrideTimer(GoalSelector goalSelector, Goal customGoal, int durationTicks) {
            this.goalSelector = goalSelector;
            this.customGoal = customGoal;
            this.ticksRemaining = durationTicks;
        }

        @Override
        public void tick(boolean serverSide) {
            if (serverSide) {
                ticksRemaining--;
                if (ticksRemaining <= 0) {
                    revertDistanceOverride();
                }
            }
        }

        @Override
        public boolean isExpired() {
            return ticksRemaining <= 0;
        }
        
        private void revertDistanceOverride() {
            if (goalSelector != null && customGoal != null) {
                goalSelector.removeGoal(customGoal);
                ArsAffinity.LOGGER.info("Reverted distance override for summon");
            }
        }
    }





    private static class CustomExtendedDistanceGoal extends Goal {
        private final com.hollingsworth.arsnouveau.common.entity.IFollowingSummon summon;
        private final Player owner;
        private final double followSpeed;
        private final float minDist;
        private final float maxDist;
        private int timeToRecalcPath;

        public CustomExtendedDistanceGoal(com.hollingsworth.arsnouveau.common.entity.IFollowingSummon summon, Player owner, double followSpeed, float minDist, float maxDist) {
            this.summon = summon;
            this.owner = owner;
            this.followSpeed = followSpeed;
            this.minDist = minDist;
            this.maxDist = maxDist;
            this.setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (owner == null || owner.isSpectator()) return false;
            return summon.getSelfEntity().distanceToSqr(owner) > (minDist * minDist);
        }

        @Override
        public boolean canContinueToUse() {
            if (owner == null) return false;
            return summon.getSelfEntity().distanceToSqr(owner) > (maxDist * maxDist);
        }

        @Override
        public void tick() {
            if (owner == null) return;

            summon.getSelfEntity().getLookControl().setLookAt(owner, 10.0F, summon.getSelfEntity().getMaxHeadXRot());

            if (--timeToRecalcPath <= 0) {
                timeToRecalcPath = 10;

                float teleportDistance = ArsAffinityConfig.SWARM_SUMMON_DISTANCE_OVERRIDE_TELEPORT_DISTANCE.get().floatValue();
                if (summon.getSelfEntity().distanceToSqr(owner) > (teleportDistance * teleportDistance)) {
                    summon.getSelfEntity().moveTo(owner.getX(), owner.getY(), owner.getZ(), summon.getSelfEntity().getYRot(), summon.getSelfEntity().getXRot());
                    summon.getSelfEntity().getNavigation().stop();
                } else {
                    summon.getSelfEntity().getNavigation().moveTo(owner, followSpeed);
                }
            }
        }
    }
}
