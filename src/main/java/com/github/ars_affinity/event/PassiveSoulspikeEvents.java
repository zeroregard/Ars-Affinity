package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import com.hollingsworth.arsnouveau.api.spell.Spell;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.api.spell.SpellResolver;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import alexthw.ars_elemental.common.glyphs.EffectCharm;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster;
import com.hollingsworth.arsnouveau.api.mana.IManaCap;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;

import java.util.Random;
import net.neoforged.bus.api.SubscribeEvent;

public class PassiveSoulspikeEvents {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity ? 
            (LivingEntity) event.getSource().getEntity() : null;
        
        if (attacker == null) return;

        // Check if this is a melee attack (not a projectile)
        DamageSource source = event.getSource();
        boolean isMeleeAttack = !source.is(DamageTypeTags.IS_PROJECTILE);

        if(!isMeleeAttack) {
            return;
        }

        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress == null) return;

        int animaTier = progress.getTier(SpellSchools.NECROMANCY);
        if (animaTier > 0) {
            AffinityPerkHelper.applyActivePerk(player, AffinityPerkType.PASSIVE_SOULSPIKE, AffinityPerk.AmountBasedPerk.class, amountPerk -> {
                if (RANDOM.nextFloat() < amountPerk.amount) {
                    applySoulspike(player, attacker, false);
                    
                    ArsAffinity.LOGGER.info("Soulspike (Melee) activated! Player {} reflected anima at attacker {} ({}% chance)", 
                        player.getName().getString(), attacker.getName().getString(), (int)(amountPerk.amount * 100));
                }
            });
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        var projectile = event.getProjectile();
        var level = projectile.level();

        if (level.isClientSide()) return;

        var rayTraceResult = event.getRayTraceResult();
        if (!(rayTraceResult instanceof EntityHitResult entityHitResult)) return;

        var hitEntity = entityHitResult.getEntity();
        if (!(hitEntity instanceof Player player)) return;

        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress == null) return;

        int animaTier = progress.getTier(SpellSchools.NECROMANCY);
        if (animaTier > 0) {
            AffinityPerkHelper.applyActivePerk(player, AffinityPerkType.PASSIVE_SOULSPIKE, AffinityPerk.AmountBasedPerk.class, amountPerk -> {
                float rangedChance = amountPerk.amount * 0.5f;
                if (RANDOM.nextFloat() < rangedChance) {
                    LivingEntity attacker = projectile.getOwner() instanceof LivingEntity ? 
                        (LivingEntity) projectile.getOwner() : null;
                    
                    if (attacker != null) {
                        applySoulspike(player, attacker, true);
                        
                        ArsAffinity.LOGGER.info("Soulspike (Ranged) activated! Player {} reflected anima at attacker {} ({}% chance)", 
                            player.getName().getString(), attacker.getName().getString(), (int)(rangedChance * 100));
                    }
                }
            });
        }
    }

    private static void applySoulspike(Player player, LivingEntity attacker, boolean isRanged) {
        // Calculate amplifier based on player's max mana (every 100 mana = +1 amplifier)
        IManaCap playerMana = CapabilityRegistry.getMana(player);
        int amplifier = 0;
        if (playerMana != null) {
            int maxMana = playerMana.getMaxMana();
            amplifier = maxMana / 100;
        }

        if (attacker instanceof Player) {
            attacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, amplifier, false, true, true));
        } else {
            try {
                // Create SpellStats with the proper amplifier
                SpellStats spellStats = new SpellStats.Builder()
                    .setAmplification(amplifier)
                    .build();

                SpellContext context = new SpellContext(player.level(), new Spell(MethodTouch.INSTANCE, EffectCharm.INSTANCE), player, new PlayerCaster(player));
                SpellResolver resolver = new SpellResolver(context);
                
                // Apply the charm effect with proper stats
                EffectCharm.INSTANCE.onResolveEntity(
                    new EntityHitResult(attacker), 
                    player.level(), 
                    player, 
                    spellStats, 
                    context, 
                    resolver
                );
                
                ArsAffinity.LOGGER.info("Applied EffectCharm to attacker {} with amplifier {} (player max mana: {})", 
                    attacker.getName().getString(), amplifier, playerMana != null ? playerMana.getMaxMana() : 0);
            } catch (Exception e) {
                ArsAffinity.LOGGER.error("Failed to apply EffectCharm to attacker", e);
            }
        }
        
        ArsAffinity.LOGGER.info("Applied soulspike to attacker {} with amplifier {} (player max mana: {})", 
            attacker.getName().getString(), amplifier, playerMana != null ? playerMana.getMaxMana() : 0);
    }
}
