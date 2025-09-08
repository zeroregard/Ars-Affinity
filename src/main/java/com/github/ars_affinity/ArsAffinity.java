package com.github.ars_affinity;

import com.github.ars_affinity.capability.SchoolAffinityProgressCapability;
import com.github.ars_affinity.capability.SchoolAffinityProgressProvider;
import com.github.ars_affinity.capability.WetTicksCapability;
import com.github.ars_affinity.capability.WetTicksProvider;
import com.github.ars_affinity.client.ArsAffinityClient;
import com.github.ars_affinity.command.ArsAffinityCommands;
import com.github.ars_affinity.command.TestParticleCommand;
import com.github.ars_affinity.config.ArsAffinityConfig;
import com.github.ars_affinity.event.*;
import com.github.ars_affinity.perk.AffinityPerkManager;
import com.github.ars_affinity.registry.ModCreativeTabs;
import com.github.ars_affinity.registry.ModDataComponents;
import com.github.ars_affinity.registry.ModItems;
import com.github.ars_affinity.registry.ModPotions;
import com.github.ars_affinity.registry.ParticleRegistry;
import com.github.ars_affinity.registry.ModSounds;
import com.github.ars_affinity.common.network.Networking;

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

        ModPotions.EFFECTS.register(modEventBus);
        ModPotions.POTIONS.register(modEventBus);

        ModItems.ITEMS.register(modEventBus);
        ModDataComponents.DATA.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ParticleRegistry.PARTICLES.register(modEventBus);
        
        modEventBus.addListener(Networking::register);
        
        if (FMLEnvironment.dist.isClient()) {
            ArsAffinityClient.init(modEventBus);
        }

        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        NeoForge.EVENT_BUS.register(TierChangeEffectsEvents.class);
        
        // Register event classes
        NeoForge.EVENT_BUS.register(PassiveLichFeastEvents.class);
        NeoForge.EVENT_BUS.register(GhostStepEvents.class);
        NeoForge.EVENT_BUS.register(DeflectionEvents.class);
        NeoForge.EVENT_BUS.register(FieldAbilityTicker.class);
        NeoForge.EVENT_BUS.register(FireThornsEvents.class);
        NeoForge.EVENT_BUS.register(ManaRegenCalcEvents.class);
        NeoForge.EVENT_BUS.register(MobPacificationEvents.class);
        NeoForge.EVENT_BUS.register(PassiveBuriedEvents.class);
        NeoForge.EVENT_BUS.register(PassiveDehydratedEvents.class);
        NeoForge.EVENT_BUS.register(PassiveFreeJumpEvents.class);
        NeoForge.EVENT_BUS.register(PassiveGroundedEvents.class);
        NeoForge.EVENT_BUS.register(PassiveManaTapEvents.class);
        NeoForge.EVENT_BUS.register(PassiveManipulationSicknessEvents.class);
        NeoForge.EVENT_BUS.register(PassivePacifistEvents.class);
        NeoForge.EVENT_BUS.register(PassiveSoulspikeEvents.class);
        NeoForge.EVENT_BUS.register(PassiveStoneSkinEvents.class);
        NeoForge.EVENT_BUS.register(PassiveSummonDefenseEvents.class);
        NeoForge.EVENT_BUS.register(PassiveSummonHealthEvents.class);
        NeoForge.EVENT_BUS.register(PassiveSummoningPowerEvents.class);
        NeoForge.EVENT_BUS.register(PassiveUnstableSummoningEvents.class);
        NeoForge.EVENT_BUS.register(SpellAmplificationEvents.class);
        NeoForge.EVENT_BUS.register(SpellBlightEvents.class);
        NeoForge.EVENT_BUS.register(PassiveHydrationEvents.class);
        NeoForge.EVENT_BUS.register(SanctuaryEvents.class);
        NeoForge.EVENT_BUS.register(SilencedEvents.class);



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
        
        event.registerEntity(
            WetTicksCapability.WET_TICKS,
            EntityType.PLAYER,
            (entity, context) -> {
                if (entity instanceof Player player) {
                    return WetTicksProvider.getWetTicks(player);
                }
                return null;
            }
        );
        
        LOGGER.info("Registered SchoolAffinityProgress capability");
        LOGGER.info("Registered WetTicks capability");
    }
    
    private void onRegisterCommands(RegisterCommandsEvent event) {
        ArsAffinityCommands.register(event.getDispatcher());
        LOGGER.info("Registered Ars Affinity commands");
    }
    
    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        SchoolAffinityProgressProvider.loadPlayerProgress(event.getEntity());
        WetTicksProvider.loadPlayerWetTicks(event.getEntity());
    }

    private void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        SchoolAffinityProgressProvider.savePlayerProgress(event.getEntity());
        WetTicksProvider.savePlayerWetTicks(event.getEntity());
    }
    
    private void onServerStopping(ServerStoppingEvent event) {
        SchoolAffinityProgressProvider.saveAllProgress();
        SchoolAffinityProgressProvider.clearCache();
        WetTicksProvider.clearCache();
    }

    public static ResourceLocation prefix(String str) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, str);
    }
} 