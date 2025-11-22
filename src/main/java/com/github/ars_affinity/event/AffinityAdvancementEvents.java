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
    private static final ResourceLocation INITIATION = ArsAffinity.prefix("progression/affinity_initiate");
    private static final Map<SpellSchool, ResourceLocation> COMPLETION_ADVANCEMENTS = Map.of(
        SpellSchools.ELEMENTAL_FIRE, ArsAffinity.prefix("progression/affinity_fire_mastery"),
        SpellSchools.ELEMENTAL_WATER, ArsAffinity.prefix("progression/affinity_water_mastery"),
        SpellSchools.ELEMENTAL_EARTH, ArsAffinity.prefix("progression/affinity_earth_mastery"),
        SpellSchools.ELEMENTAL_AIR, ArsAffinity.prefix("progression/affinity_air_mastery"),
        SpellSchools.ABJURATION, ArsAffinity.prefix("progression/affinity_abjuration_mastery"),
        SpellSchools.CONJURATION, ArsAffinity.prefix("progression/affinity_conjuration_mastery"),
        SpellSchools.NECROMANCY, ArsAffinity.prefix("progression/affinity_necromancy_mastery"),
        SpellSchools.MANIPULATION, ArsAffinity.prefix("progression/affinity_manipulation_mastery")
    );

    @SubscribeEvent
    public static void onPointAllocated(SchoolAffinityPointAllocatedEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        PlayerAffinityData data = PlayerAffinityDataHelper.getPlayerAffinityData(serverPlayer);
        if (data == null) {
            return;
        }
        if (data.getTotalPointsAcrossAllSchools() > 0) {
            award(serverPlayer, INITIATION);
        }
        SpellSchool school = event.getSchool();
        ResourceLocation advancementId = COMPLETION_ADVANCEMENTS.get(school);
        if (advancementId == null) {
            return;
        }
        int maxPoints = PerkTreeManager.getMaxPointsForSchool(school);
        if (maxPoints > 0 && event.getTotalPoints() >= maxPoints) {
            award(serverPlayer, advancementId);
        }
    }

    private static void award(ServerPlayer player, ResourceLocation advancementId) {
        var server = player.server;
        if (server == null) {
            return;
        }
        var advancement = server.getAdvancements().get(advancementId);
        if (advancement == null) {
            ArsAffinity.LOGGER.warn("Missing advancement {}", advancementId);
            return;
        }
        var progress = player.getAdvancements().getOrStartProgress(advancement);
        if (progress.isDone()) {
            return;
        }
        for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(advancement, criterion);
        }
    }
}
