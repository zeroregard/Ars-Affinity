package com.github.ars_affinity.perk;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.config.ArsAffinityConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class PerkTreeManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<SpellSchool, Map<String, PerkNode>> perkTrees = new HashMap<>();
    private static final Map<String, PerkNode> allNodes = new HashMap<>();
    private static boolean isLoaded = false;
    
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
    
    public static void loadPerkTrees() {
        perkTrees.clear();
        allNodes.clear();
        
        Path configDir = FMLPaths.CONFIGDIR.get().resolve("ars_affinity").resolve("perk_trees");
        ArsAffinity.LOGGER.info("Loading perk trees from: {}", configDir.toAbsolutePath());
        
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
                ArsAffinity.LOGGER.warn("Perk trees directory does not exist, created: {}", configDir);
            }
            
            for (SpellSchool school : SUPPORTED_SCHOOLS) {
                String schoolName = getSchoolName(school);
                Path schoolFile = configDir.resolve(schoolName + ".json");
                
                // First try to load from config directory
                if (Files.exists(schoolFile)) {
                    ArsAffinity.LOGGER.info("Loading perk tree for school: {} from config", schoolName);
                    loadSchoolPerkTree(school, schoolFile);
                } else {
                    // Try to copy from JAR resources
                    try {
                        String resourcePath = "data/ars_affinity/config/perk_trees/" + schoolName + ".json";
                        var resourceStream = PerkTreeManager.class.getClassLoader().getResourceAsStream(resourcePath);
                        if (resourceStream != null) {
                            ArsAffinity.LOGGER.info("Copying perk tree for school: {} from JAR resources", schoolName);
                            Files.createDirectories(schoolFile.getParent());
                            Files.copy(resourceStream, schoolFile);
                            loadSchoolPerkTree(school, schoolFile);
                        } else {
                            ArsAffinity.LOGGER.warn("No perk tree file found for school: {}", schoolName);
                        }
                    } catch (IOException e) {
                        ArsAffinity.LOGGER.error("Failed to copy perk tree file for school {}: {}", schoolName, e.getMessage());
                    }
                }
            }
            
            isLoaded = true;
            ArsAffinity.LOGGER.info("Loaded perk trees for {} schools with {} total nodes", 
                perkTrees.size(), allNodes.size());
            
        } catch (Exception e) {
            ArsAffinity.LOGGER.error("Error loading perk trees: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load perk trees", e);
        }
    }
    
    private static void loadSchoolPerkTree(SpellSchool school, Path schoolFile) throws IOException {
        String content = Files.readString(schoolFile, StandardCharsets.UTF_8);
        JsonObject schoolData = JsonParser.parseString(content).getAsJsonObject();
        
        Map<String, PerkNode> schoolNodes = new HashMap<>();
        
        // Load all perks from the simplified structure
        if (schoolData.has("perks")) {
            JsonArray perksArray = schoolData.getAsJsonArray("perks");
            for (JsonElement nodeElement : perksArray) {
                PerkNode node = parsePerkNode(nodeElement.getAsJsonObject(), school);
                schoolNodes.put(node.getId(), node);
                allNodes.put(node.getId(), node);
            }
        }
        
        perkTrees.put(school, schoolNodes);
        ArsAffinity.LOGGER.info("Loaded {} nodes for school {}", schoolNodes.size(), school.getId());
    }
    
    private static PerkNode parsePerkNode(JsonObject nodeData, SpellSchool school) {
        String id = nodeData.get("id").getAsString();
        AffinityPerkType perkType = AffinityPerkType.valueOf(nodeData.get("perk").getAsString());
        int tier = nodeData.get("tier").getAsInt();
        int pointCost = nodeData.get("pointCost").getAsInt();
        PerkCategory category = PerkCategory.valueOf(nodeData.get("category").getAsString());
        
        // Parse configurable perk values with defaults
        float amount = nodeData.has("amount") ? nodeData.get("amount").getAsFloat() : 0.0f;
        int time = nodeData.has("time") ? nodeData.get("time").getAsInt() : 0;
        int cooldown = nodeData.has("cooldown") ? nodeData.get("cooldown").getAsInt() : 0;
        float manaCost = nodeData.has("manaCost") ? nodeData.get("manaCost").getAsFloat() : 0.0f;
        float damage = nodeData.has("damage") ? nodeData.get("damage").getAsFloat() : 0.0f;
        int freezeTime = nodeData.has("freezeTime") ? nodeData.get("freezeTime").getAsInt() : 0;
        float radius = nodeData.has("radius") ? nodeData.get("radius").getAsFloat() : 0.0f;
        float dashLength = nodeData.has("dashLength") ? nodeData.get("dashLength").getAsFloat() : 0.0f;
        float dashDuration = nodeData.has("dashDuration") ? nodeData.get("dashDuration").getAsFloat() : 0.0f;
        float health = nodeData.has("health") ? nodeData.get("health").getAsFloat() : 0.0f;
        float hunger = nodeData.has("hunger") ? nodeData.get("hunger").getAsFloat() : 0.0f;
        
        List<String> prerequisites = new ArrayList<>();
        if (nodeData.has("prerequisites")) {
            JsonArray prereqArray = nodeData.getAsJsonArray("prerequisites");
            for (JsonElement prereqElement : prereqArray) {
                prerequisites.add(prereqElement.getAsString());
            }
        }
        
        String prerequisiteGlyph = nodeData.has("prerequisite_glyph") ? nodeData.get("prerequisite_glyph").getAsString() : null;
        
        return new PerkNode(id, perkType, school, tier, pointCost, prerequisites, prerequisiteGlyph,
                           category, amount, time, cooldown, manaCost, damage,
                           freezeTime, radius, dashLength, dashDuration, health, hunger);
    }
    
    public static PerkNode getNode(String nodeId) {
        if (!isLoaded) {
            loadPerkTrees();
        }
        return allNodes.get(nodeId);
    }
    
    public static Map<String, PerkNode> getSchoolNodes(SpellSchool school) {
        if (!isLoaded) {
            loadPerkTrees();
        }
        return perkTrees.getOrDefault(school, new HashMap<>());
    }
    
    public static List<PerkNode> getRootNodes(SpellSchool school) {
        Map<String, PerkNode> schoolNodes = getSchoolNodes(school);
        return schoolNodes.values().stream()
            .filter(PerkNode::isRootNode)
            .collect(Collectors.toList());
    }
    
    public static List<PerkNode> getChildNodes(String parentNodeId) {
        if (!isLoaded) {
            loadPerkTrees();
        }
        
        return allNodes.values().stream()
            .filter(node -> node.getPrerequisites().contains(parentNodeId))
            .collect(Collectors.toList());
    }
    
    public static int calculatePointCost(SpellSchool school, int currentPoints) {
        // Use the same scaling formula as the old system
        double scalingDecay = ArsAffinityConfig.AFFINITY_SCALING_DECAY_STRENGTH.get();
        double minimumFactor = ArsAffinityConfig.AFFINITY_SCALING_MINIMUM_FACTOR.get();
        
        // Calculate the scaling factor based on current points
        // This is the inverse of the old system - we want to know how much it costs to gain the next point
        double scalingFactor = Math.max(minimumFactor, Math.pow(1.0 - (currentPoints / 1000.0), scalingDecay));
        
        // Base cost for gaining a point
        double baseCost = 1.0;
        double scaledCost = baseCost / scalingFactor;
        
        return Math.max(1, (int) Math.round(scaledCost));
    }
    
    public static int convertAffinityToPoints(float affinityPercentage) {
        // Convert 0-100% affinity to points using the same scaling as the old system
        double scalingDecay = ArsAffinityConfig.AFFINITY_SCALING_DECAY_STRENGTH.get();
        double minimumFactor = ArsAffinityConfig.AFFINITY_SCALING_MINIMUM_FACTOR.get();
        
        // Calculate total points that would have been gained to reach this affinity
        // This is a rough approximation - will need refinement based on actual usage
        double basePoints = 100.0; // Base points for 100% affinity
        double scalingFactor = Math.max(minimumFactor, Math.pow(1.0 - affinityPercentage, scalingDecay));
        double totalPoints = basePoints * scalingFactor;
        
        return Math.max(0, (int) Math.round(totalPoints));
    }
    
    public static int getTotalPerksForSchool(SpellSchool school) {
        Map<String, PerkNode> schoolNodes = getSchoolNodes(school);
        return schoolNodes.size();
    }
    
    public static int getMaxPointsForSchool(SpellSchool school) {
        // Max points should equal the number of perks available
        return getTotalPerksForSchool(school);
    }
    
    public static float convertPointsToPercentage(int currentPoints, int maxPoints) {
        if (maxPoints <= 0) return 0.0f;
        return Math.min(1.0f, (float) currentPoints / maxPoints);
    }
    
    public static void createDefaultPerkTrees() {
        Path configDir = FMLPaths.CONFIGDIR.get().resolve("ars_affinity").resolve("perk_trees");
        
        try {
            Files.createDirectories(configDir);
            
            // Create example perk tree for Fire school
            createExampleFirePerkTree(configDir);
            
            ArsAffinity.LOGGER.info("Created default perk trees in: {}", configDir);
            
        } catch (IOException e) {
            ArsAffinity.LOGGER.error("Failed to create default perk trees: {}", e.getMessage(), e);
        }
    }
    
    private static void createExampleFirePerkTree(Path configDir) throws IOException {
        JsonObject fireTree = new JsonObject();
        
        JsonArray perks = new JsonArray();
        
        // Fire Thorns - Root node
        JsonObject fireThorns = new JsonObject();
        fireThorns.addProperty("id", "fire_thorns_1");
        fireThorns.addProperty("perk", "PASSIVE_FIRE_THORNS");
        fireThorns.addProperty("tier", 1);
        fireThorns.addProperty("pointCost", 10);
        fireThorns.addProperty("category", "PASSIVE");
        fireThorns.addProperty("isStackable", false);
        fireThorns.addProperty("maxStacks", 1);
        perks.add(fireThorns);
        
        // Fire Dash - Root node
        JsonObject fireDash = new JsonObject();
        fireDash.addProperty("id", "fire_dash_1");
        fireDash.addProperty("perk", "ACTIVE_FIRE_DASH");
        fireDash.addProperty("tier", 1);
        fireDash.addProperty("pointCost", 15);
        fireDash.addProperty("category", "ACTIVE");
        fireDash.addProperty("isStackable", false);
        fireDash.addProperty("maxStacks", 1);
        perks.add(fireDash);
        
        // Stackable Spell Power - Root node
        JsonObject spellPower = new JsonObject();
        spellPower.addProperty("id", "fire_spell_power_1");
        spellPower.addProperty("perk", "STACKABLE_SPELL_POWER");
        spellPower.addProperty("tier", 1);
        spellPower.addProperty("pointCost", 5);
        spellPower.addProperty("category", "STACKABLE");
        spellPower.addProperty("isStackable", true);
        spellPower.addProperty("maxStacks", 5);
        perks.add(spellPower);
        
        // Enhanced Fire Thorns - Requires Fire Thorns
        JsonObject enhancedFireThorns = new JsonObject();
        enhancedFireThorns.addProperty("id", "fire_thorns_2");
        enhancedFireThorns.addProperty("perk", "PASSIVE_FIRE_THORNS");
        enhancedFireThorns.addProperty("tier", 2);
        enhancedFireThorns.addProperty("pointCost", 20);
        enhancedFireThorns.addProperty("category", "PASSIVE");
        enhancedFireThorns.addProperty("isStackable", false);
        enhancedFireThorns.addProperty("maxStacks", 1);
        JsonArray prereqs = new JsonArray();
        prereqs.add("fire_thorns_1");
        enhancedFireThorns.add("prerequisites", prereqs);
        perks.add(enhancedFireThorns);
        
        fireTree.add("perks", perks);
        
        // Write to file
        Path fireFile = configDir.resolve("fire.json");
        Files.writeString(fireFile, GSON.toJson(fireTree), StandardCharsets.UTF_8);
    }
    
    private static String getSchoolName(SpellSchool school) {
        String schoolId = school.getId().toString();
        return switch (schoolId) {
            case "ars_nouveau:elemental_fire" -> "fire";
            case "ars_nouveau:elemental_water" -> "water";
            case "ars_nouveau:elemental_earth" -> "earth";
            case "ars_nouveau:elemental_air" -> "air";
            case "ars_nouveau:abjuration" -> "abjuration";
            case "ars_nouveau:necromancy" -> "necromancy";
            case "ars_nouveau:conjuration" -> "conjuration";
            case "ars_nouveau:manipulation" -> "manipulation";
            default -> schoolId.replace(":", "_");
        };
    }
}
