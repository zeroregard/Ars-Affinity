package com.github.ars_affinity.command;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.config.ArsAffinityConfig;
import com.github.ars_affinity.school.SchoolRelationshipHelper;
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

import java.util.Arrays;
import java.util.List;

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
                .executes(ArsAffinityCommands::resetAllAffinities))
            .then(Commands.literal("list")
                .executes(ArsAffinityCommands::listAllAffinities))
            .then(Commands.literal("blacklist")
                .executes(ArsAffinityCommands::showGlyphBlacklist)));
    }

    private static int setAffinity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        String schoolName = StringArgumentType.getString(context, "school");
        float percentage = FloatArgumentType.getFloat(context, "percentage");

        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress == null) {
            source.sendFailure(Component.literal("Failed to get affinity progress for player"));
            return 0;
        }

        SpellSchool school = parseSpellSchool(schoolName);
        if (school == null) {
            source.sendFailure(Component.literal("Unknown school: " + schoolName));
            return 0;
        }

        // Convert percentage to affinity (0.0 to 1.0)
        float affinity = percentage / 100.0f;
        progress.setAffinity(school, affinity);

        source.sendSuccess(() -> Component.literal(String.format("Set %s affinity to %.1f%% (Tier %d)", 
            schoolName, percentage, SchoolRelationshipHelper.calculateTierFromAffinity(affinity))), true);

        ArsAffinity.LOGGER.info("Player {} set {} affinity to {}%", 
            player.getName().getString(), schoolName, percentage);

        return 1;
    }

    private static int getAffinity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        String schoolName = StringArgumentType.getString(context, "school");

        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress == null) {
            source.sendFailure(Component.literal("Failed to get affinity progress for player"));
            return 0;
        }

        SpellSchool school = parseSpellSchool(schoolName);
        if (school == null) {
            source.sendFailure(Component.literal("Unknown school: " + schoolName));
            return 0;
        }

        float affinity = progress.getAffinity(school);
        float percentage = affinity * 100.0f;
        int tier = SchoolRelationshipHelper.calculateTierFromAffinity(affinity);

        source.sendSuccess(() -> Component.literal(String.format("%s: %.1f%% (Tier %d)", 
            schoolName, percentage, tier)), false);

        return 1;
    }

    private static int resetAllAffinities(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress == null) {
            source.sendFailure(Component.literal("Failed to get affinity progress for player"));
            return 0;
        }

        // Reset all schools to 0%
        for (SpellSchool school : SchoolRelationshipHelper.ALL_SCHOOLS) {
            progress.setAffinity(school, 0.0f);
        }

        source.sendSuccess(() -> Component.literal("Reset all affinities to 0%"), true);
        ArsAffinity.LOGGER.info("Player {} reset all affinities", player.getName().getString());

        return 1;
    }

    private static int listAllAffinities(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress == null) {
            source.sendFailure(Component.literal("Failed to get affinity progress for player"));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Current Affinities:"), false);
        
        for (SpellSchool school : SchoolRelationshipHelper.ALL_SCHOOLS) {
            float affinity = progress.getAffinity(school);
            float percentage = affinity * 100.0f;
            int tier = SchoolRelationshipHelper.calculateTierFromAffinity(affinity);
            
            source.sendSuccess(() -> Component.literal(String.format("  %s: %.1f%% (Tier %d)", 
                school.getId(), percentage, tier)), false);
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

    private static SuggestionProvider<CommandSourceStack> getSchoolSuggestions() {
        return (context, builder) -> {
            String[] schoolNames = Arrays.stream(SchoolRelationshipHelper.ALL_SCHOOLS)
                .map(SpellSchool::getId)
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
            case "necromancy" -> com.hollingsworth.arsnouveau.api.spell.SpellSchools.NECROMANCY;
            case "manipulation" -> com.hollingsworth.arsnouveau.api.spell.SpellSchools.MANIPULATION;
            default -> null;
        };
    }
} 