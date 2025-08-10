package com.github.ars_affinity;

import com.github.ars_affinity.capability.SchoolAffinityProgressCapability;
import com.github.ars_affinity.capability.SchoolAffinityProgressProvider;
import com.github.ars_affinity.client.ArsAffinityClient;
import com.github.ars_affinity.command.ArsAffinityCommands;
import com.github.ars_affinity.common.ability.NetworkHandler;
import com.github.ars_affinity.config.ArsAffinityConfig;
import com.github.ars_affinity.perk.AffinityPerkManager;
import com.github.ars_affinity.registry.ModCreativeTabs;
import com.github.ars_affinity.registry.ModDataComponents;
import com.github.ars_affinity.registry.ModItems;
import com.github.ars_affinity.registry.ModPotions;
import com.github.ars_affinity.registry.ModRecipeRegistry;
import com.hollingsworth.arsnouveau.api.registry.ImbuementRecipeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.Logger;


@Mod(ArsAffinity.MOD_ID)
public class ArsAffinity {
    public static final String MOD_ID = "ars_affinity";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    static {
        if (!FMLEnvironment.production) {
            Configurator.setLevel(LOGGER.getName(), org.apache.logging.log4j.Level.INFO);
        } else {
            Configurator.setLevel(LOGGER.getName(), org.apache.logging.log4j.Level.WARN);
        }
    }

    public ArsAffinity(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, ArsAffinityConfig.SERVER_CONFIG);

        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(NetworkHandler::registerPayloads);

        ModPotions.EFFECTS.register(modEventBus);

        ModItems.ITEMS.register(modEventBus);
        ModDataComponents.DATA.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModRecipeRegistry.RECIPE_TYPES.register(modEventBus);
        ModRecipeRegistry.RECIPE_SERIALIZERS.register(modEventBus);
        
        // Register our imbuement recipe type with Ars Nouveau so the chamber recognizes it
        ImbuementRecipeRegistry.INSTANCE.addRecipeType(ModRecipeRegistry.CHARM_CHARGING_TYPE);
        
        if (FMLEnvironment.dist.isClient()) {
            ArsAffinityClient.init(modEventBus);
        }

        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);

        AffinityPerkManager.loadConfig();
        
    }
    
    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerEntity(
            SchoolAffinityProgressCapability.SCHOOL_AFFINITY_PROGRESS,
            EntityType.PLAYER,
            (entity, context) -> {
                if (entity instanceof Player player) {
                    return SchoolAffinityProgressProvider.getAffinityProgress(player);
                }
                return null;
            }
        );
        LOGGER.info("Registered SchoolAffinityProgress capability");
    }
    
    private void onRegisterCommands(RegisterCommandsEvent event) {
        ArsAffinityCommands.register(event.getDispatcher());
        LOGGER.info("Registered Ars Affinity commands");
    }
    
    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        SchoolAffinityProgressProvider.loadPlayerProgress(event.getEntity());
    }

    private void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        SchoolAffinityProgressProvider.savePlayerProgress(event.getEntity());
    }
    
    private void onServerStopping(ServerStoppingEvent event) {
        SchoolAffinityProgressProvider.saveAllProgress();
        SchoolAffinityProgressProvider.clearCache();
    }

    public static ResourceLocation prefix(String str) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, str);
    }
} 