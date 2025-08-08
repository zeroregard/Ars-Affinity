package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.mana.IManaHolder;
import com.hollingsworth.arsnouveau.api.mana.ManaHelper;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.minecraft.tags.DamageTypeTags;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PassiveReverberationEvents {
    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        if (!player.isCrouching()) return;

        // Only reflect melee attacks
        if (!event.getSource().is(DamageTypeTags.IS_MELEE)) return;

        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity ?
                (LivingEntity) event.getSource().getEntity() : null;
        if (attacker == null) return;

        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress == null) return;
        int earthTier = progress.getTier(SpellSchools.ELEMENTAL_EARTH);
        if (earthTier <= 0) return;

        AffinityPerkHelper.applyHighestTierPerk(progress, earthTier, SpellSchools.ELEMENTAL_EARTH, AffinityPerkType.PASSIVE_REVERBERATION, perk -> {
            if (perk instanceof AffinityPerk.AmountBasedPerk amountPerk) {
                IManaHolder manaHolder = ManaHelper.getManaHolder(player);
                if (manaHolder == null) return;
                double manaCost = manaHolder.getMaxMana() * amountPerk.amount;
                if (manaHolder.getCurrentMana() < manaCost) return;
                manaHolder.removeMana(manaCost);

                // Reflect knockback to attacker
                double strength = 0.5; // You may want to make this configurable or scale with perk
                double dx = attacker.getX() - player.getX();
                double dz = attacker.getZ() - player.getZ();
                double dist = Math.max(Math.sqrt(dx * dx + dz * dz), 0.01);
                attacker.push(dx / dist * strength, 0.1, dz / dist * strength);

                ArsAffinity.LOGGER.info("Reverberation activated! Player {} reflected knockback to attacker {} for {} mana ({}%)", 
                    player.getName().getString(), attacker.getName().getString(), (int)manaCost, (int)(amountPerk.amount * 100));
            }
        });
    }
}