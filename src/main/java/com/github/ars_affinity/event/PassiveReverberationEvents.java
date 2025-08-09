package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.minecraft.tags.DamageTypeTags;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PassiveReverberationEvents {
    
    // Store recent melee attackers to correlate with knockback events
    private static final Map<UUID, LivingEntity> recentAttackers = new HashMap<>();
    
    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        // Only track melee attacks (player attacks and mob attacks)
        if (!event.getSource().is(DamageTypeTags.IS_PLAYER_ATTACK) && !event.getSource().is(DamageTypes.MOB_ATTACK)) return;

        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity ?
                (LivingEntity) event.getSource().getEntity() : null;
        if (attacker == null) return;

        // Store the attacker for potential knockback handling
        recentAttackers.put(player.getUUID(), attacker);
    }
    
    @SubscribeEvent
    public static void onLivingKnockBack(LivingKnockBackEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        if (!player.isCrouching()) return;

        // Check if this player was recently attacked by someone
        LivingEntity attacker = recentAttackers.get(player.getUUID());
        if (attacker == null) return;

        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress == null) return;
        int earthTier = progress.getTier(SpellSchools.ELEMENTAL_EARTH);
        if (earthTier <= 0) return;

        AffinityPerkHelper.applyHighestTierPerk(progress, earthTier, SpellSchools.ELEMENTAL_EARTH, AffinityPerkType.PASSIVE_REVERBERATION, perk -> {
            if (perk instanceof AffinityPerk.AmountBasedPerk amountPerk) {
                var manaCap = CapabilityRegistry.getMana(player);
                if (manaCap == null) return;
                double manaCost = manaCap.getMaxMana() * amountPerk.amount;
                if (manaCap.getCurrentMana() < manaCost) return;
                manaCap.removeMana(manaCost);

                // Cancel the knockback for the player
                event.setCanceled(true);

                // Apply reverse knockback to the attacker
                double strength = event.getStrength() * 1.5; // Amplify the knockback
                double dx = attacker.getX() - player.getX();
                double dz = attacker.getZ() - player.getZ();
                double dist = Math.max(Math.sqrt(dx * dx + dz * dz), 0.01);
                attacker.knockback(strength, -dx / dist, -dz / dist);

                ArsAffinity.LOGGER.info("Reverberation activated! Player {} cancelled knockback and reflected it to attacker {} for {} mana ({}%)", 
                    player.getName().getString(), attacker.getName().getString(), (int)manaCost, (int)(amountPerk.amount * 100));
                
                // Clear the attacker since we've processed this knockback
                recentAttackers.remove(player.getUUID());
            }
        });
    }
}