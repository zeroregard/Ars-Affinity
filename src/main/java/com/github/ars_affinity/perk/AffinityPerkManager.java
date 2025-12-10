package com.github.ars_affinity.perk;

import com.github.ars_affinity.ArsAffinity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import net.neoforged.fml.loading.FMLPaths;

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

    public static synchronized void loadConfig() {
        if (isLoaded) {
            return; // Already loaded, avoid concurrent loading
        }
        schoolPerks.clear();

        Path configDir = FMLPaths.CONFIGDIR.get().resolve("ars_affinity").resolve("perk_trees");
        ArsAffinity.LOGGER.debug("Loading perks from perk trees directory: {}", configDir.toAbsolutePath());

        try {
            if (!Files.exists(configDir)) {
                ArsAffinity.LOGGER.warn("Perk config directory does not exist: {}", configDir);
                Files.createDirectories(configDir);
                return;
            }

            try (var paths = Files.walk(configDir)) {
                List<Path> jsonFiles = paths
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".json"))
                        .collect(Collectors.toList());

                ArsAffinity.LOGGER.debug("Found {} JSON perk files to load", jsonFiles.size());

                for (Path path : jsonFiles) {
                    try {
                        ArsAffinity.LOGGER.debug("Loading perk file: {}", path);
                        loadPerkFile(configDir, path);
                    } catch (Exception e) {
                        String errorMsg = "Failed to load perk file: " + path + " - " + e.getMessage();
                        ArsAffinity.LOGGER.error(errorMsg, e);
                        throw new RuntimeException("Ars Affinity: " + errorMsg, e);
                    }
                }
            }

            ArsAffinity.LOGGER.debug("Perk loading complete. Loaded {} schools with perks", schoolPerks.size());
            for (Map.Entry<SpellSchool, Map<Integer, List<AffinityPerk>>> schoolEntry : schoolPerks.entrySet()) {
                SpellSchool school = schoolEntry.getKey();
                Map<Integer, List<AffinityPerk>> tierPerks = schoolEntry.getValue();
                ArsAffinity.LOGGER.debug("School {}: {} tiers with perks", school.getId(), tierPerks.size());
                for (Map.Entry<Integer, List<AffinityPerk>> tierEntry : tierPerks.entrySet()) {
                    int tier = tierEntry.getKey();
                    List<AffinityPerk> perks = tierEntry.getValue();
                    ArsAffinity.LOGGER.debug("  Tier {}: {} perks", tier, perks.size());
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

        // Extract school name from filename (school.json format)
        String schoolName = fileName.replace(".json", "");

        // Parse spell school
        SpellSchool school = parseSpellSchool(schoolName);
        if (school == null) {
            ArsAffinity.LOGGER.warn("Unknown spell school: {} in file: {}", schoolName, jsonFile);
            return;
        }

        // Load and parse JSON
        try (var reader = Files.newBufferedReader(jsonFile, StandardCharsets.UTF_8)) {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            
            if (jsonObject.has("perks") && jsonObject.get("perks").isJsonArray()) {
                JsonArray perksArray = jsonObject.getAsJsonArray("perks");
                for (JsonElement perkElement : perksArray) {
                    try {
                        // Create a temporary JSON object with the old format for compatibility
                        JsonObject perkJson = perkElement.getAsJsonObject();
                        JsonObject oldFormatJson = new JsonObject();
                        
                        // Copy basic properties
                        oldFormatJson.add("perk", perkJson.get("perk"));
                        oldFormatJson.add("isBuff", perkJson.get("category").getAsString().equals("PASSIVE") ? 
                            new com.google.gson.JsonPrimitive(true) : new com.google.gson.JsonPrimitive(false));
                        
                        // Copy amount if present
                        if (perkJson.has("amount")) {
                            oldFormatJson.add("amount", perkJson.get("amount"));
                        }
                        
                        // Copy time if present
                        if (perkJson.has("time")) {
                            oldFormatJson.add("time", perkJson.get("time"));
                        }
                        
                        // Copy cooldown if present
                        if (perkJson.has("cooldown")) {
                            oldFormatJson.add("cooldown", perkJson.get("cooldown"));
                        }
                        
                        // Copy manaCost if present
                        if (perkJson.has("manaCost")) {
                            oldFormatJson.add("manaCost", perkJson.get("manaCost"));
                        }
                        
                        // Copy damage if present
                        if (perkJson.has("damage")) {
                            oldFormatJson.add("damage", perkJson.get("damage"));
                        }
                        
                        // Copy freezeTime if present
                        if (perkJson.has("freezeTime")) {
                            oldFormatJson.add("freezeTime", perkJson.get("freezeTime"));
                        }
                        
                        // Copy radius if present
                        if (perkJson.has("radius")) {
                            oldFormatJson.add("radius", perkJson.get("radius"));
                        }
                        
                        // Copy entities if present
                        if (perkJson.has("entities")) {
                            oldFormatJson.add("entities", perkJson.get("entities"));
                        }
                        
                        // Copy dashLength if present
                        if (perkJson.has("dashLength")) {
                            oldFormatJson.add("dashLength", perkJson.get("dashLength"));
                        }
                        
                        // Copy dashDuration if present
                        if (perkJson.has("dashDuration")) {
                            oldFormatJson.add("dashDuration", perkJson.get("dashDuration"));
                        }
                        
                        // Copy health and hunger for LichFeast
                        if (perkJson.has("health")) {
                            oldFormatJson.add("health", perkJson.get("health"));
                        }
                        if (perkJson.has("hunger")) {
                            oldFormatJson.add("hunger", perkJson.get("hunger"));
                        }
                        
                        // Copy chance for UnstableSummoning
                        if (perkJson.has("chance")) {
                            oldFormatJson.add("chance", perkJson.get("chance"));
                        }
                        
                        AffinityPerk perk = GSON.fromJson(oldFormatJson, AffinityPerk.class);
                        int tier = perkJson.get("tier").getAsInt();
                        addPerk(school, tier, perk);
                    } catch (Exception e) {
                        ArsAffinity.LOGGER.error("Failed to parse perk: {} - {}", perkElement, e.getMessage(), e);
                    }
                }
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
        ArsAffinity.LOGGER.debug("Adding perk: {} for school {} at level {}", perk.perk, school.getId(), level);
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
    
    // Get the highest level perk of a specific type for a school
    public static AffinityPerk getHighestLevelPerk(SpellSchool school, AffinityPerkType perkType) {
        if (!isLoaded) {
            loadConfig();
        }
        
        Map<Integer, List<AffinityPerk>> schoolPerkMap = schoolPerks.get(school);
        if (schoolPerkMap == null) {
            return null;
        }
        
        // Find the highest tier that contains this perk type
        int highestTier = -1;
        AffinityPerk highestPerk = null;
        
        for (Map.Entry<Integer, List<AffinityPerk>> entry : schoolPerkMap.entrySet()) {
            int tier = entry.getKey();
            List<AffinityPerk> tierPerks = entry.getValue();
            
            for (AffinityPerk perk : tierPerks) {
                if (perk.perk == perkType && tier > highestTier) {
                    highestTier = tier;
                    highestPerk = perk;
                }
            }
        }
        
        return highestPerk;
    }
} 