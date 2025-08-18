package com.github.ars_affinity.common.ability;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.common.ability.field.SanctuaryHelper;
import com.github.ars_affinity.common.ability.field.CurseFieldHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public class ActiveAbilityManager {
	private static final Map<SpellSchool, AffinityPerkType> SCHOOL_ABILITY_MAP = new HashMap<>();

	static {
		SCHOOL_ABILITY_MAP.put(SpellSchools.ELEMENTAL_WATER, AffinityPerkType.ACTIVE_ICE_BLAST);
		SCHOOL_ABILITY_MAP.put(SpellSchools.ELEMENTAL_EARTH, AffinityPerkType.ACTIVE_GROUND_SLAM);
		SCHOOL_ABILITY_MAP.put(SpellSchools.MANIPULATION, AffinityPerkType.ACTIVE_SWAP_ABILITY);
		SCHOOL_ABILITY_MAP.put(SpellSchools.ELEMENTAL_AIR, AffinityPerkType.ACTIVE_AIR_DASH);
		SCHOOL_ABILITY_MAP.put(SpellSchools.ABJURATION, AffinityPerkType.ACTIVE_SANCTUARY);
		SCHOOL_ABILITY_MAP.put(SpellSchools.NECROMANCY, AffinityPerkType.ACTIVE_CURSE_FIELD);
	}

	public static void triggerActiveAbility(ServerPlayer player) {
		ArsAffinity.LOGGER.info("ACTIVE ABILITY: Trigger requested by {}", player.getName().getString());
		var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
		if (progress == null) {
			ArsAffinity.LOGGER.info("ACTIVE ABILITY: No progress capability for {}", player.getName().getString());
			return;
		}

		AffinityPerk activePerk = null;

		for (Map.Entry<SpellSchool, AffinityPerkType> entry : SCHOOL_ABILITY_MAP.entrySet()) {
			SpellSchool school = entry.getKey();
			AffinityPerkType perkType = entry.getValue();

			int tier = progress.getTier(school);
			ArsAffinity.LOGGER.info("ACTIVE ABILITY: Tier for {} is {}", school.getId(), tier);
			if (tier > 0) {
				final AffinityPerk[] foundPerk = {null};
				AffinityPerkHelper.applyHighestTierPerk(progress, tier, school, perkType, perk -> {
					foundPerk[0] = perk;
				});

				if (foundPerk[0] != null) {
					activePerk = foundPerk[0];
					ArsAffinity.LOGGER.info("ACTIVE ABILITY: Selected perk {} for school {}", ((AffinityPerk.ActiveAbilityPerk) activePerk).perk, school.getId());
					break;
				}
			}
		}

		if (activePerk == null || !(activePerk instanceof AffinityPerk.ActiveAbilityPerk)) {
			ArsAffinity.LOGGER.info("ACTIVE ABILITY: No active ability perk available for {}", player.getName().getString());
			return;
		}

		AffinityPerk.ActiveAbilityPerk abilityPerk = (AffinityPerk.ActiveAbilityPerk) activePerk;
		ArsAffinity.LOGGER.info("ACTIVE ABILITY: Executing {} for {}", abilityPerk.perk, player.getName().getString());

		switch (abilityPerk.perk) {
			case ACTIVE_ICE_BLAST:
				ArsAffinity.LOGGER.info("ACTIVE ABILITY: Dispatch ICE BLAST");
				IceBlastHelper.executeAbility(player, abilityPerk);
				break;
			case ACTIVE_SWAP_ABILITY:
				ArsAffinity.LOGGER.info("ACTIVE ABILITY: Dispatch SWAP ABILITY");
				SwapAbilityHelper.executeAbility(player, abilityPerk);
				break;
			case ACTIVE_GROUND_SLAM:
				ArsAffinity.LOGGER.info("ACTIVE ABILITY: Dispatch GROUND SLAM");
				GroundSlamHelper.executeAbility(player, abilityPerk);
				break;
			case ACTIVE_AIR_DASH:
				ArsAffinity.LOGGER.info("ACTIVE ABILITY: Dispatch AIR DASH");
				AirDashHelper.executeAbility(player, abilityPerk);
				break;
			case ACTIVE_SANCTUARY:
				ArsAffinity.LOGGER.info("ACTIVE ABILITY: Dispatch SANCTUARY");
				SanctuaryHelper.toggleOrStart(player, abilityPerk);
				break;
			case ACTIVE_CURSE_FIELD:
				ArsAffinity.LOGGER.info("ACTIVE ABILITY: Dispatch CURSE FIELD");
				CurseFieldHelper.toggleOrStart(player, abilityPerk);
				break;
			default:
				ArsAffinity.LOGGER.warn("Unknown active ability perk type: {}", abilityPerk.perk);
				break;
		}
	}
}