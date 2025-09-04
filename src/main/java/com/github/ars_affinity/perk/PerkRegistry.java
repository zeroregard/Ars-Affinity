package com.github.ars_affinity.perk;

import com.github.ars_affinity.ArsAffinity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonDeserializationContext;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;
import java.lang.reflect.Type;

public class PerkRegistry {
    private static final Map<String, PerkData> PERK_REGISTRY = new HashMap<>();
    private static final int MAX_TIER = 3;
    private static final Gson GSON = new GsonBuilder().create();
    
    private static final SpellSchool[] SUPPORTED_SCHOOLS = {
        SpellSchools.ELEMENTAL_FIRE,
        SpellSchools.ELEMENTAL_WATER,
        SpellSchools.ELEMENTAL_EARTH,
        SpellSchools.ELEMENTAL_AIR,
        SpellSchools.ABJURATION,
        SpellSchools.NECROMANCY,
        SpellSchools.CONJURATION,
        SpellSchools.MANIPULATION
    };
    
    public static void initializeFromConfig() {
        PERK_REGISTRY.clear();
        
        Path configDir = Path.of("config", "ars_affinity", "perks");
        ArsAffinity.LOGGER.info("Looking for perk configs in: {}", configDir.toAbsolutePath());
        
        try {
            if (!Files.exists(configDir)) {
                ArsAffinity.LOGGER.warn("Perk config directory does not exist: {}", configDir);
                return;
            }
            
            // Walk through school subdirectories
            try (var schoolPaths = Files.list(configDir)) {
                List<Path> schoolDirs = schoolPaths
                    .filter(Files::isDirectory)
                    .collect(Collectors.toList());
                
                ArsAffinity.LOGGER.info("Found {} school directories: {}", schoolDirs.size(), 
                    schoolDirs.stream().map(path -> path.getFileName().toString()).collect(Collectors.joining(", ")));
                
                for (Path schoolDir : schoolDirs) {
                    String schoolName = schoolDir.getFileName().toString().toUpperCase();
                    ArsAffinity.LOGGER.info("Processing school directory: {}", schoolName);
                    
                    // Walk through tier subdirectories
                    try (var tierPaths = Files.list(schoolDir)) {
                        List<Path> tierDirs = tierPaths
                            .filter(Files::isDirectory)
                            .collect(Collectors.toList());
                        
                        ArsAffinity.LOGGER.info("Found {} tier directories in {}: {}", 
                            tierDirs.size(), schoolName, 
                            tierDirs.stream().map(path -> path.getFileName().toString()).collect(Collectors.joining(", ")));
                        
                        for (Path tierDir : tierDirs) {
                            String tierName = tierDir.getFileName().toString();
                            ArsAffinity.LOGGER.info("Processing tier directory: {} for school {}", tierName, schoolName);
                            
                            try {
                                int tier = Integer.parseInt(tierName);
                                
                                // Process JSON files in this tier directory
                                try (var jsonPaths = Files.list(tierDir)) {
                                    List<Path> jsonFiles = jsonPaths
                                        .filter(Files::isRegularFile)
                                        .filter(path -> path.toString().endsWith(".json"))
                                        .collect(Collectors.toList());
                                    
                                    ArsAffinity.LOGGER.info("Found {} JSON files in {}_{}: {}", 
                                        jsonFiles.size(), schoolName, tier,
                                        jsonFiles.stream().map(path -> path.getFileName().toString()).collect(Collectors.joining(", ")));
                                    
                                    for (Path jsonFile : jsonFiles) {
                                        try {
                                            ArsAffinity.LOGGER.info("Processing config file: {} for {}_{}", jsonFile, schoolName, tier);
                                            loadPerkFileFromSchoolTier(schoolName, tier, jsonFile);
                                        } catch (Exception e) {
                                            ArsAffinity.LOGGER.error("Failed to load perk file: {} - {}", jsonFile, e.getMessage());
                                        }
                                    }
                                }
                                
                            } catch (NumberFormatException e) {
                                ArsAffinity.LOGGER.warn("Invalid tier directory name: {}", tierName);
                            }
                        }
                    }
                }
            }
            
            ArsAffinity.LOGGER.info("PerkRegistry initialized with {} perk configurations", PERK_REGISTRY.size());
            
        } catch (Exception e) {
            ArsAffinity.LOGGER.error("Error loading perk configs: {}", e.getMessage(), e);
        }
    }
    
    private static void loadPerkFileFromSchoolTier(String schoolName, int tier, Path jsonFile) throws IOException {
        String content = Files.readString(jsonFile);
        ArsAffinity.LOGGER.info("Loading perk file: {} for school {} tier {}", jsonFile.getFileName(), schoolName, tier);
        
        try {
            // Parse the JSON array of perks
            JsonArray perksArray = JsonParser.parseString(content).getAsJsonArray();
            
            for (JsonElement perkElement : perksArray) {
                JsonObject perkObject = perkElement.getAsJsonObject();
                
                // Create the perk key in the format: SCHOOL_PERKTYPE_TIER
                String perkTypeStr = perkObject.get("perk").getAsString();
                String perkKey = String.format("%s_%s_%d", schoolName, perkTypeStr, tier);
                
                ArsAffinity.LOGGER.info("Registering perk: {} -> {}", perkKey, perkObject);
                
                // Create AffinityPerk from JSON and then PerkData
                try {
                    // Create a simple JsonDeserializationContext for parsing
                    JsonDeserializationContext context = new JsonDeserializationContext() {
                        @Override
                        public <T> T deserialize(JsonElement json, Type typeOfT) {
                            return GSON.fromJson(json, typeOfT);
                        }
                    };
                    
                    AffinityPerk affinityPerk = AffinityPerk.fromJson(perkElement, context);
                    SpellSchool school = parseSpellSchool(schoolName.toLowerCase());
                    if (school != null) {
                        PerkData data = new PerkData(affinityPerk, school, tier);
                        PERK_REGISTRY.put(perkKey, data);
                    } else {
                        ArsAffinity.LOGGER.warn("Unknown school: {} for perk {}", schoolName, perkKey);
                    }
                } catch (Exception e) {
                    ArsAffinity.LOGGER.error("Failed to create AffinityPerk from JSON: {} - {}", perkObject, e.getMessage());
                }
            }
            
        } catch (Exception e) {
            ArsAffinity.LOGGER.error("Failed to parse perk file: {} - {}", jsonFile, e.getMessage());
            throw e;
        }
    }

    private static void loadPerkFile(Path configDir, Path jsonFile) throws IOException {
        // This method is no longer used - keeping for compatibility but it's deprecated
        ArsAffinity.LOGGER.warn("loadPerkFile is deprecated, use loadPerkFileFromSchoolTier instead");
    }
    
    private static SpellSchool parseSpellSchool(String schoolName) {
        return switch (schoolName.toLowerCase()) {
            case "fire" -> SpellSchools.ELEMENTAL_FIRE;
            case "water" -> SpellSchools.ELEMENTAL_WATER;
            case "earth" -> SpellSchools.ELEMENTAL_EARTH;
            case "air" -> SpellSchools.ELEMENTAL_AIR;
            case "abjuration" -> SpellSchools.ABJURATION;
            case "conjuration" -> SpellSchools.CONJURATION;
            case "necromancy" -> SpellSchools.NECROMANCY;
            case "manipulation" -> SpellSchools.MANIPULATION;
            default -> null;
        };
    }
    
    private static String generateKey(SpellSchool school, AffinityPerkType perkType, int tier) {
        return String.format("%s_%s_%d", 
            school.getId().toString().toUpperCase().replace(":", "_"), 
            perkType.name(), 
            tier);
    }
    
    public static PerkData getPerk(String key) {
        return PERK_REGISTRY.get(key);
    }
    
    public static PerkData getPerk(SpellSchool school, AffinityPerkType perkType, int tier) {
        String key = generateKey(school, perkType, tier);
        return PERK_REGISTRY.get(key);
    }
    
    public static boolean hasPerk(String key) {
        return PERK_REGISTRY.containsKey(key);
    }
    
    public static Set<String> getAllPerkKeys() {
        return new HashSet<>(PERK_REGISTRY.keySet());
    }
    
    public static int getTotalPerkCount() {
        return PERK_REGISTRY.size();
    }
}