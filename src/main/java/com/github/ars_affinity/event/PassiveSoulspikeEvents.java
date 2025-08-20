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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

import java.util.Random;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PassiveSoulspikeEvents {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity ? 
            (LivingEntity) event.getSource().getEntity() : null;
        
        if (attacker == null) return;

        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress == null) return;

        int animaTier = progress.getTier(SpellSchools.NECROMANCY);
        if (animaTier > 0) {
            AffinityPerkHelper.applyHighestTierPerk(progress, animaTier, SpellSchools.NECROMANCY, AffinityPerkType.PASSIVE_SOULSPIKE, perk -> {
                if (perk instanceof AffinityPerk.AmountBasedPerk amountPerk) {
                    if (RANDOM.nextFloat() < amountPerk.amount) {
                        applySoulspike(player, attacker, false);
                        
                        ArsAffinity.LOGGER.info("Soulspike activated! Player {} reflected anima at attacker {} ({}% chance)", 
                            player.getName().getString(), attacker.getName().getString(), (int)(amountPerk.amount * 100));
                    }
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
            AffinityPerkHelper.applyHighestTierPerk(progress, animaTier, SpellSchools.NECROMANCY, AffinityPerkType.PASSIVE_SOULSPIKE, perk -> {
                if (perk instanceof AffinityPerk.AmountBasedPerk amountPerk) {
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
                }
            });
        }
    }

    private static void applySoulspike(Player player, LivingEntity attacker, boolean isRanged) {
        if (attacker instanceof Player) {
            attacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0, false, true, true));
        } else {
            try {
                Spell charmSpell = new Spell(MethodTouch.INSTANCE, EffectCharm.INSTANCE);
                SpellContext context = new SpellContext(player.level(), charmSpell, player, new PlayerCaster(player));
                SpellResolver resolver = new SpellResolver(context);
                resolver.onCastOnEntity(player.getMainHandItem(), attacker, InteractionHand.MAIN_HAND);
            } catch (Exception e) {
                ArsAffinity.LOGGER.error("Failed to apply EffectCharm to attacker", e);
            }
        }
    }
}
