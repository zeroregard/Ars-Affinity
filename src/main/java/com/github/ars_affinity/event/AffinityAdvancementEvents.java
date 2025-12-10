package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.PlayerAffinityData;
import com.github.ars_affinity.capability.PlayerAffinityDataHelper;
import com.github.ars_affinity.perk.PerkTreeManager;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Map;

public class AffinityAdvancementEvents {
    private static final ResourceLocation INITIATION = ArsAffinity.prefix("affinity_initiate");
    private static final Map<SpellSchool, ResourceLocation> COMPLETION_ADVANCEMENTS = Map.of(
        SpellSchools.ELEMENTAL_FIRE, ArsAffinity.prefix("affinity_fire_mastery"),
        SpellSchools.ELEMENTAL_WATER, ArsAffinity.prefix("affinity_water_mastery"),
        SpellSchools.ELEMENTAL_EARTH, ArsAffinity.prefix("affinity_earth_mastery"),
        SpellSchools.ELEMENTAL_AIR, ArsAffinity.prefix("affinity_air_mastery"),
        SpellSchools.ABJURATION, ArsAffinity.prefix("affinity_abjuration_mastery"),
        SpellSchools.CONJURATION, ArsAffinity.prefix("affinity_conjuration_mastery"),
        SpellSchools.NECROMANCY, ArsAffinity.prefix("affinity_necromancy_mastery"),
        SpellSchools.MANIPULATION, ArsAffinity.prefix("affinity_manipulation_mastery")
    );

    @SubscribeEvent
    public static void onPointAllocated(SchoolAffinityPointAllocatedEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer serverPlayer)) {
            ArsAffinity.LOGGER.debug("Advancement event skipped: player is not ServerPlayer");
            return;
        }
        PlayerAffinityData data = PlayerAffinityDataHelper.getPlayerAffinityData(serverPlayer);
        if (data == null) {
            ArsAffinity.LOGGER.warn("Advancement event skipped: PlayerAffinityData is null");
            return;
        }
        if (data.getTotalPointsAcrossAllSchools() > 0) {
            ArsAffinity.LOGGER.debug("Attempting to award initiation advancement to {}", serverPlayer.getName().getString());
            award(serverPlayer, INITIATION);
        }
        SpellSchool school = event.getSchool();
        ResourceLocation advancementId = COMPLETION_ADVANCEMENTS.get(school);
        if (advancementId == null) {
            ArsAffinity.LOGGER.warn("No advancement mapped for school {}", school);
            return;
        }
        int maxPoints = PerkTreeManager.getMaxPointsForSchool(school);
        if (maxPoints > 0 && event.getTotalPoints() >= maxPoints) {
            ArsAffinity.LOGGER.debug("Player {} reached max points ({}/{}) for school {}, awarding mastery advancement",
                serverPlayer.getName().getString(), event.getTotalPoints(), maxPoints, school);
            award(serverPlayer, advancementId);
        }
    }

    private static void award(ServerPlayer player, ResourceLocation advancementId) {
        var server = player.server;
        if (server == null) {
            ArsAffinity.LOGGER.warn("Cannot award advancement {}: server is null", advancementId);
            return;
        }
        ArsAffinity.LOGGER.debug("Looking up advancement {} for player {}", advancementId, player.getName().getString());
        var advancement = server.getAdvancements().get(advancementId);
        if (advancement == null) {
            ArsAffinity.LOGGER.error("Missing advancement {}! Expected file at: data/{}/advancement/{}.json",
                advancementId, advancementId.getNamespace(), advancementId.getPath());
            return;
        }
        var progress = player.getAdvancements().getOrStartProgress(advancement);
        if (progress.isDone()) {
            ArsAffinity.LOGGER.debug("Advancement {} already completed for player {}", advancementId, player.getName().getString());
            return;
        }
        ArsAffinity.LOGGER.debug("Awarding advancement {} to player {}", advancementId, player.getName().getString());
        for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(advancement, criterion);
        }
    }
}
