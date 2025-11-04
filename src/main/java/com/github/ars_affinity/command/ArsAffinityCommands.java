
package com.github.ars_affinity.command;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.PlayerAffinityDataHelper;
import com.github.ars_affinity.capability.PlayerAffinityDataProvider;
import com.github.ars_affinity.config.ArsAffinityConfig;
import com.github.ars_affinity.event.SchoolAffinityPointAllocatedEvent;
import com.github.ars_affinity.perk.AffinityPerkType;

import com.github.ars_affinity.perk.PerkAllocation;
import com.github.ars_affinity.school.SchoolRelationshipHelper;
import com.github.ars_affinity.util.ChatMessageHelper;
import com.github.ars_affinity.util.GlyphBlacklistHelper;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ArsAffinityCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ars-affinity")
            .requires(source -> source.hasPermission(2)) // OP level 2 or higher
            .then(Commands.literal("set")
                .then(Commands.argument("school", StringArgumentType.word())
                    .suggests(getSchoolSuggestions())
                    .then(Commands.argument("percentage", FloatArgumentType.floatArg(0.0f, 100.0f))
                        .executes(ArsAffinityCommands::setAffinity))))
            .then(Commands.literal("get")
                .then(Commands.argument("school", StringArgumentType.word())
                    .suggests(getSchoolSuggestions())
                    .executes(ArsAffinityCommands::getAffinity)))
            .then(Commands.literal("reset")
                .then(Commands.argument("target", StringArgumentType.word())
                    .suggests(getResetSuggestions())
                    .executes(ArsAffinityCommands::resetAffinity)))
            .then(Commands.literal("list")
                .executes(ArsAffinityCommands::listAllAffinities))
            .then(Commands.literal("list-perks")
                .executes(ArsAffinityCommands::listPerks))
            .then(Commands.literal("blacklist")
                .executes(ArsAffinityCommands::showGlyphBlacklist)));
    }

    private static int setAffinity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        String schoolName = StringArgumentType.getString(context, "school");
        float percentage = FloatArgumentType.getFloat(context, "percentage");

        var data = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        if (data == null) {
            source.sendFailure(Component.literal("Failed to get affinity data for player"));
            return 0;
        }

        SpellSchool school = parseSpellSchool(schoolName);
        if (school == null) {
            source.sendFailure(Component.literal("Unknown school: " + schoolName));
            return 0;
        }

        // Set the percentage and let the natural progression system handle point distribution
        float currentPercentage = data.getSchoolPercentage(school);
        float percentageIncrease = percentage - currentPercentage;
        
        if (percentageIncrease > 0) {
            // Use the natural progression system to add progress
            int pointsAwarded = data.addSchoolProgress(school, percentageIncrease);
            
            if (pointsAwarded > 0) {
                SchoolAffinityPointAllocatedEvent event = new SchoolAffinityPointAllocatedEvent(
                    player, school, pointsAwarded, data.getSchoolPoints(school)
                );
                NeoForge.EVENT_BUS.post(event);
            }
        } else if (percentageIncrease < 0) {
            // For decreasing percentage, we need to reset and set to target
            data.resetSchoolPercentage(school);
            data.setSchoolPoints(school, 0);
            
            // Then add progress up to the target percentage
            int pointsAwarded = data.addSchoolProgress(school, percentage);
            
            if (pointsAwarded > 0) {
                SchoolAffinityPointAllocatedEvent event = new SchoolAffinityPointAllocatedEvent(
                    player, school, pointsAwarded, data.getSchoolPoints(school)
                );
                NeoForge.EVENT_BUS.post(event);
            }
        }

        int maxPoints = com.github.ars_affinity.perk.PerkTreeManager.getMaxPointsForSchool(school);
        String displayName = getSchoolDisplayName(school);
        source.sendSuccess(() -> Component.literal(String.format("Set %s affinity to %.1f%% (%d/%d points)", 
            displayName, percentage, data.getSchoolPoints(school), maxPoints)), true);

        ArsAffinity.LOGGER.info("Player {} set {} affinity to {}% ({} points)", 
            player.getName().getString(), displayName, percentage, data.getSchoolPoints(school));
        
        PlayerAffinityDataHelper.savePlayerData(player);
        PlayerAffinityDataProvider.syncToClient(player);

        return 1;
    }

    private static int getAffinity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        String schoolName = StringArgumentType.getString(context, "school");

        var data = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        if (data == null) {
            source.sendFailure(Component.literal("Failed to get affinity data for player"));
            return 0;
        }

        SpellSchool school = parseSpellSchool(schoolName);
        if (school == null) {
            source.sendFailure(Component.literal("Unknown school: " + schoolName));
            return 0;
        }

        int currentPoints = data.getSchoolPoints(school);
        int maxPoints = com.github.ars_affinity.perk.PerkTreeManager.getMaxPointsForSchool(school);
        float percentage = data.getSchoolAffinityPercentage(school) * 100.0f;

        String displayName = getSchoolDisplayName(school);
        source.sendSuccess(() -> Component.literal(String.format("%s: %d/%d points (%.1f%%)", 
            displayName, currentPoints, maxPoints, percentage)), false);

        return 1;
    }

    private static int resetAffinity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        String target = StringArgumentType.getString(context, "target");

        var data = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        if (data == null) {
            ChatMessageHelper.sendAllSchoolsResetFailureMessage(player, "Failed to get affinity data for player");
            return 0;
        }

        if (target.equalsIgnoreCase("all")) {
            // Reset all schools - deallocate all perks and reset points
            int totalPointsReset = 0;
            for (SpellSchool school : SchoolRelationshipHelper.ALL_SCHOOLS) {
                int currentPoints = data.getSchoolPoints(school);
                totalPointsReset += currentPoints;
            }
            
            data.respecAll();
            
            for (SpellSchool school : SchoolRelationshipHelper.ALL_SCHOOLS) {
                data.setSchoolPoints(school, 0);
                data.resetSchoolPercentage(school);
            }
            
            ChatMessageHelper.sendAllSchoolsResetMessage(player, totalPointsReset);
            PlayerAffinityDataHelper.savePlayerData(player);
            PlayerAffinityDataProvider.syncToClient(player);
            return 1;
        } else {
            // Reset specific school - deallocate perks and reset points
            SpellSchool school = parseSpellSchool(target);
            if (school == null) {
                source.sendFailure(Component.literal("Unknown school: " + target + ". Use 'all' to reset all schools or a valid school name."));
                return 0;
            }
            
            int currentPoints = data.getSchoolPoints(school);
            
            data.respecSchool(school);
            
            data.setSchoolPoints(school, 0);
            data.resetSchoolPercentage(school);
            
            ChatMessageHelper.sendSchoolResetMessage(player, school, currentPoints);
            PlayerAffinityDataHelper.savePlayerData(player);
            PlayerAffinityDataProvider.syncToClient(player);
            return 1;
        }
    }

    private static int listAllAffinities(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        var data = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        if (data == null) {
            source.sendFailure(Component.literal("Failed to get affinity data for player"));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Current Affinities:"), false);
        
        for (SpellSchool school : SchoolRelationshipHelper.ALL_SCHOOLS) {
            int currentPoints = data.getSchoolPoints(school);
            int maxPoints = com.github.ars_affinity.perk.PerkTreeManager.getMaxPointsForSchool(school);
            float progressPercentage = data.getSchoolPercentage(school);
            
            source.sendSuccess(() -> Component.literal(String.format("  %s: %d/%d points (%.1f%% progress)", 
                school.getId(), currentPoints, maxPoints, progressPercentage)), false);
        }

        return 1;
    }

    private static int showGlyphBlacklist(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        
        try {
            List<? extends String> blacklist = ArsAffinityConfig.GLYPH_BLACKLIST.get();
            
            if (blacklist == null || blacklist.isEmpty()) {
                source.sendSuccess(() -> Component.literal("Glyph blacklist is empty - no glyphs are blacklisted"), false);
            } else {
                source.sendSuccess(() -> Component.literal("Glyph blacklist contains " + blacklist.size() + " entries:"), false);
                
                for (String glyphId : blacklist) {
                    source.sendSuccess(() -> Component.literal("  - " + glyphId), false);
                }
            }
            
            // Also log to console for debugging
            GlyphBlacklistHelper.logBlacklistConfiguration();
            
        } catch (Exception e) {
            source.sendFailure(Component.literal("Error reading glyph blacklist configuration: " + e.getMessage()));
            ArsAffinity.LOGGER.error("Error reading glyph blacklist configuration: {}", e.getMessage());
            return 0;
        }
        
        return 1;
    }

    private static int listPerks(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        var data = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        
        if (data == null) {
            context.getSource().sendFailure(Component.literal("Failed to get affinity data"));
            return 0;
        }
        
        Set<PerkAllocation> activePerkAllocations = data.getAllAllocatedPerks();
        
        if (activePerkAllocations.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.literal("No active perks"), false);
            return 1;
        }
        
        context.getSource().sendSuccess(() -> Component.literal("Active perks:"), false);
        
        for (PerkAllocation allocation : activePerkAllocations) {
            AffinityPerkType perkType = allocation.getPerkType();
            SpellSchool school = allocation.getSchool();
            int tier = allocation.getTier();
            
            String perkName = perkType.name().replace("_", " ").toLowerCase();
            String schoolName = school.getId().toString().replace("ars_nouveau:", "").replace("_", " ").toLowerCase();
            
            context.getSource().sendSuccess(() -> Component.literal("  - " + perkName + " (" + schoolName + " Tier " + tier + ")"), false);
        }
        
        return 1;
    }

    private static SuggestionProvider<CommandSourceStack> getSchoolSuggestions() {
        return (context, builder) -> {
            String[] schoolNames = Arrays.stream(SchoolRelationshipHelper.ALL_SCHOOLS)
                .map(school -> getSchoolDisplayName(school))
                .toArray(String[]::new);
            return SharedSuggestionProvider.suggest(schoolNames, builder);
        };
    }
    
    private static SuggestionProvider<CommandSourceStack> getResetSuggestions() {
        return (context, builder) -> {
            // Add "all" as the first suggestion
            builder.suggest("all");
            
            // Add all school names
            String[] schoolNames = Arrays.stream(SchoolRelationshipHelper.ALL_SCHOOLS)
                .map(school -> getSchoolDisplayName(school))
                .toArray(String[]::new);
            return SharedSuggestionProvider.suggest(schoolNames, builder);
        };
    }

    private static SpellSchool parseSpellSchool(String schoolName) {
        return switch (schoolName.toLowerCase()) {
            case "fire" -> com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_FIRE;
            case "water" -> com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_WATER;
            case "earth" -> com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_EARTH;
            case "air" -> com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_AIR;
            case "abjuration" -> com.hollingsworth.arsnouveau.api.spell.SpellSchools.ABJURATION;
            case "conjuration" -> com.hollingsworth.arsnouveau.api.spell.SpellSchools.CONJURATION;
            case "necromancy", "anima" -> com.hollingsworth.arsnouveau.api.spell.SpellSchools.NECROMANCY;
            case "manipulation" -> com.hollingsworth.arsnouveau.api.spell.SpellSchools.MANIPULATION;
            default -> null;
        };
    }
    
    /**
     * Gets the display name for a spell school for commands and chat messages.
     * Necromancy is displayed as "anima" instead of "necromancy".
     */
    private static String getSchoolDisplayName(SpellSchool school) {
        if (school == com.hollingsworth.arsnouveau.api.spell.SpellSchools.NECROMANCY) {
            return "anima";
        }
        return school.getId().toString().replaceAll("_", " ");
    }

} 