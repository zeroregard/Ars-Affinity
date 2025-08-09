package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.github.ars_affinity.registry.ModPotions;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.minecraft.tags.DamageTypeTags;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PassiveReverberationEvents {
    
    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        if (!player.isCrouching()) return;

        // Only handle melee attacks (player attacks and mob attacks)
        if (!event.getSource().is(DamageTypeTags.IS_PLAYER_ATTACK) && !event.getSource().is(DamageTypes.MOB_ATTACK)) return;

        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity ?
                (LivingEntity) event.getSource().getEntity() : null;
        if (attacker == null) return;

        // Check if player has earth affinity and the perk
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

                // Apply knockback immunity effect for a very short duration (5 ticks)
                // This will prevent the knockback that follows this damage
                player.addEffect(new MobEffectInstance(ModPotions.KNOCKBACK_IMMUNITY_EFFECT, 5, 0, false, false));

                // Apply reverse knockback to the attacker with mana-based scaling
                // Square root scaling: strength = sqrt(mana / 20)
                // At 20 mana -> strength = 1.0, at 200 mana -> strength ≈ 3.16
                double strength = Math.sqrt(manaCost / 20.0);
                double dx = attacker.getX() - player.getX();
                double dz = attacker.getZ() - player.getZ();
                double dist = Math.max(Math.sqrt(dx * dx + dz * dz), 0.01);
                attacker.knockback(strength, -dx / dist, -dz / dist);

                ArsAffinity.LOGGER.info("Reverberation activated! Player {} reflected knockback to attacker {} for {} mana ({}%) with strength {}", 
                    player.getName().getString(), attacker.getName().getString(), (int)manaCost, (int)(amountPerk.amount * 100), String.format("%.2f", strength));
            }
        });
    }
    
    @SubscribeEvent
    public static void onLivingKnockBack(LivingKnockBackEvent event) {
        // Cancel all knockback for entities with knockback immunity effect
        if (event.getEntity().hasEffect(ModPotions.KNOCKBACK_IMMUNITY_EFFECT)) {
            event.setCanceled(true);
        }
    }
}