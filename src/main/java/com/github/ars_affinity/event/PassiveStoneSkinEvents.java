package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.github.ars_affinity.registry.ModPotions;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import com.hollingsworth.arsnouveau.api.mana.IManaCap;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.minecraft.tags.DamageTypeTags;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PassiveStoneSkinEvents {
	
	@SubscribeEvent
	public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
		if (!(event.getEntity() instanceof Player player)) return;
		if (player.level().isClientSide()) return;

		// Only handle melee attacks (player attacks and mob attacks)
		if (!event.getSource().is(DamageTypeTags.IS_PLAYER_ATTACK) && !event.getSource().is(DamageTypes.MOB_ATTACK)) return;

		// Cooldown check
		if (player.hasEffect(ModPotions.STONE_SKIN_COOLDOWN_EFFECT)) return;

		// Check if player has the stone skin perk using O(1) lookup
		var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
		if (progress == null) return;
		
		// O(1) perk lookup using the new perk index
		AffinityPerkHelper.applyActivePerk(progress, AffinityPerkType.PASSIVE_STONE_SKIN, perk -> {
			if (perk instanceof AffinityPerk.DurationBasedPerk durationPerk) {
				event.setCanceled(true);

				player.addEffect(new MobEffectInstance(ModPotions.STONE_SKIN_COOLDOWN_EFFECT, durationPerk.time, 0, false, true, true));

				Vec3 pos = player.position();
				player.level().playSound(null, pos.x, pos.y, pos.z, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0f, 0.9f);

				// Reverse knockback to attacker with mana-based scaling
				var damageSource = event.getSource();
				net.minecraft.world.entity.Entity attackerEntity = damageSource.getEntity();
				if (attackerEntity == null) attackerEntity = damageSource.getDirectEntity();
				if (attackerEntity instanceof LivingEntity attacker) {
					IManaCap manaCap = CapabilityRegistry.getMana(player);
					double currentMana = manaCap != null ? manaCap.getCurrentMana() : 0.0;
					double strength = Math.sqrt(Math.max(currentMana, 0.0) / 100.0);
					double dx = attacker.getX() - player.getX();
					double dz = attacker.getZ() - player.getZ();
					double dist = Math.max(Math.sqrt(dx * dx + dz * dz), 0.01);
					attacker.knockback(strength, -dx / dist, -dz / dist);
				}

				// Emit stone particles around the player
				if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
					BlockParticleOption stoneParticles = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState());
					for (int i = 0; i < 12; i++) {
						double ox = (Math.random() - 0.5) * 3;
						double oy = Math.random() * 1.0;
						double oz = (Math.random() - 0.5) * 3;
						serverLevel.sendParticles(stoneParticles, player.getX() + ox, player.getY() + oy, player.getZ() + oz, 1, 0, 0, 0, 0.0);
					}
				}
			}
		});
	}
}


