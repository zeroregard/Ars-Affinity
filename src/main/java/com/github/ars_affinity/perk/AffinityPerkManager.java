package com.github.ars_affinity.perk;

import com.github.ars_affinity.ArsAffinity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AffinityPerkManager {
    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(AffinityPerk.class, new AffinityPerkDeserializer())
        .create();
    private static final Map<SpellSchool, Map<Integer, List<AffinityPerk>>> schoolPerks = new HashMap<>();
    private static boolean isLoaded = false;

    private static class AffinityPerkDeserializer implements JsonDeserializer<AffinityPerk> {
        @Override
        public AffinityPerk deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return AffinityPerk.fromJson(json, context);
        }
    }

    public static void loadConfig() {
        schoolPerks.clear();

        Path configDir = Path.of("config", "ars_affinity", "perks");

        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
                return;
            }

            try (var paths = Files.walk(configDir)) {
                List<Path> jsonFiles = paths
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".json"))
                        .collect(Collectors.toList());

                for (Path path : jsonFiles) {
                    try {
                        loadPerkFile(configDir, path);
                    } catch (Exception e) {
                        String errorMsg = "Failed to load perk file: " + path + " - " + e.getMessage();
                        ArsAffinity.LOGGER.error(errorMsg, e);
                        throw new RuntimeException("Ars Affinity: " + errorMsg, e);
                    }
                }
            }

            isLoaded = true;
        } catch (Exception e) {
            String errorMsg = "Error loading affinity perks config: " + e.getMessage();
            ArsAffinity.LOGGER.error(errorMsg, e);
            throw new RuntimeException("Ars Affinity: " + errorMsg, e);
        }
    }

    private static void loadPerkFile(Path rootDir, Path jsonFile) throws IOException {
        String fileName = jsonFile.getFileName().toString();
        if (!fileName.endsWith(".json")) {
            return;
        }

        // Extract school and level from path relative to rootDir
        Path relativePath = rootDir.relativize(jsonFile);
        if (relativePath.getNameCount() != 2) {
            ArsAffinity.LOGGER.warn("Invalid perk file structure: {} (expected school/level.json)", jsonFile);
            return;
        }

        String schoolName = relativePath.getName(0).toString();
        String levelName = relativePath.getName(1).toString().replace(".json", "");

        // Parse spell school
        SpellSchool school = parseSpellSchool(schoolName);
        if (school == null) {
            ArsAffinity.LOGGER.warn("Unknown spell school: {} in file: {}", schoolName, jsonFile);
            return;
        }

        // Parse level
        int level;
        try {
            level = Integer.parseInt(levelName);
            if (level <= 0) {
                ArsAffinity.LOGGER.warn("Invalid level: {} in file: {}", levelName, jsonFile);
                return;
            }
        } catch (NumberFormatException e) {
            ArsAffinity.LOGGER.warn("Invalid level format: {} in file: {}", levelName, jsonFile);
            return;
        }

        // Load and parse JSON
        try (var reader = Files.newBufferedReader(jsonFile, StandardCharsets.UTF_8)) {
            JsonElement jsonElement = JsonParser.parseReader(reader);

            if (jsonElement.isJsonArray()) {
                AffinityPerk[] perks = GSON.fromJson(jsonElement, AffinityPerk[].class);
                for (AffinityPerk perk : perks) addPerk(school, level, perk);
            } else {
                AffinityPerk perk = GSON.fromJson(jsonElement, AffinityPerk.class);
                addPerk(school, level, perk);
            }
        } catch (JsonParseException e) {
            String errorMsg = "JSON parsing error in perk file: " + jsonFile + " - " + e.getMessage();
            ArsAffinity.LOGGER.error(errorMsg, e);
            throw new RuntimeException("Ars Affinity: " + errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Failed to parse perk file: " + jsonFile + " - " + e.getMessage();
            ArsAffinity.LOGGER.error(errorMsg, e);
            throw new RuntimeException("Ars Affinity: " + errorMsg, e);
        }
    }

    private static void addPerk(SpellSchool school, int level, AffinityPerk perk) {
        schoolPerks.computeIfAbsent(school, k -> new HashMap<>())
                .computeIfAbsent(level, k -> new java.util.ArrayList<>())
                .add(perk);
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

    public static List<AffinityPerk> getPerksForLevel(SpellSchool school, int level) {
        if (!isLoaded) {
            loadConfig();
        }

        Map<Integer, List<AffinityPerk>> schoolPerkMap = schoolPerks.get(school);
        if (schoolPerkMap == null) {
            return new java.util.ArrayList<>();
        }

        List<AffinityPerk> perks = new java.util.ArrayList<>();
        for (int i = 1; i <= level; i++) {
            List<AffinityPerk> levelPerks = schoolPerkMap.get(i);
            if (levelPerks != null) {
                perks.addAll(levelPerks);
            }
        }

        return perks;
    }

    public static List<AffinityPerk> getPerksForCurrentLevel(SpellSchool school, int level) {
        if (!isLoaded) {
            loadConfig();
        }

        Map<Integer, List<AffinityPerk>> schoolPerkMap = schoolPerks.get(school);
        if (schoolPerkMap == null) {
            return new java.util.ArrayList<>();
        }

        List<AffinityPerk> levelPerks = schoolPerkMap.get(level);
        return levelPerks != null ? levelPerks : new java.util.ArrayList<>();
    }
    
    // Use this to access static perk data regardless of whether a player actually has this perk or not
    // Later on, we should be using this instead of storing perk data per player. Players should only have the perk type and tier stored, not the data.
    public static AffinityPerk getPerk(SpellSchool school, int tier, AffinityPerkType perkType) {
        if (!isLoaded) {
            loadConfig();
        }
        
        Map<Integer, List<AffinityPerk>> schoolPerkMap = schoolPerks.get(school);
        if (schoolPerkMap == null) {
            return null;
        }
        
        List<AffinityPerk> tierPerks = schoolPerkMap.get(tier);
        if (tierPerks == null) {
            return null;
        }
        
        for (AffinityPerk perk : tierPerks) {
            if (perk.perk == perkType) {
                return perk;
            }
        }
        
        return null;
    }
} 