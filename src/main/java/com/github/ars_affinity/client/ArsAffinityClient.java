package com.github.ars_affinity.client;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.client.screen.AffinityScreen;
import com.github.ars_affinity.common.ability.ActiveAbilityPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

public class ArsAffinityClient {
    
    public static final Lazy<net.minecraft.client.KeyMapping> AFFINITY_UI_KEY = Lazy.of(() -> 
        new net.minecraft.client.KeyMapping(
            "key.ars_affinity.affinity_ui", 
            GLFW.GLFW_KEY_LEFT_BRACKET, // Default to [ key (next to P on US layout)
            "key.categories.ars_affinity"
        )
    );
    
    public static final Lazy<net.minecraft.client.KeyMapping> ABILITY_KEY = Lazy.of(() -> 
        new net.minecraft.client.KeyMapping(
            "key.ars_affinity.ability", 
            GLFW.GLFW_KEY_F, // Default to F key
            "key.categories.ars_affinity"
        )
    );
    
    public static void init(IEventBus modEventBus) {
        ArsAffinity.LOGGER.info("Initializing Ars Affinity client...");
        
        modEventBus.addListener(ArsAffinityClient::clientSetup);
        modEventBus.addListener(ArsAffinityClient::registerKeybindings);
        
        NeoForge.EVENT_BUS.register(ArsAffinityClient.class);
    }
    
    private static void clientSetup(final FMLClientSetupEvent event) {
        ArsAffinity.LOGGER.info("Ars Affinity client setup complete!");
    }
    
    private static void registerKeybindings(final RegisterKeyMappingsEvent event) {
        event.register(AFFINITY_UI_KEY.get());
        event.register(ABILITY_KEY.get());
        ArsAffinity.LOGGER.info("Ars Affinity keybindings registered!");
    }
    
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }
        
        if (AFFINITY_UI_KEY.get().consumeClick()) {
            LocalPlayer player = minecraft.player;
            minecraft.setScreen(new AffinityScreen(player));
        }
        
        if (ABILITY_KEY.get().consumeClick()) {
            // Send packet to server to trigger active ability
            ActiveAbilityPacket.sendToServer();
        }
    }
} 