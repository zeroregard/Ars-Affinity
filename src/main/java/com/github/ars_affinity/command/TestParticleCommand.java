package com.github.ars_affinity.command;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.client.particles.SpiralParticleHelper;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID)
public class TestParticleCommand {
    
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("testparticles")
            .requires(source -> source.hasPermission(2))
            .executes(context -> {
                if (context.getSource().getEntity() instanceof net.minecraft.world.entity.player.Player player) {
                    if (player.level().isClientSide()) {
                        ArsAffinity.LOGGER.info("TestParticleCommand: Spawning test particles on client");
                        SpiralParticleHelper.spawnSpiralParticles(
                            (net.minecraft.client.multiplayer.ClientLevel) player.level(),
                            player,
                            SpellSchools.ELEMENTAL_FIRE,
                            10
                        );
                        context.getSource().sendSuccess(() -> Component.literal("Test particles spawned!"), false);
                    } else {
                        context.getSource().sendFailure(Component.literal("This command must be run on the client side"));
                    }
                } else {
                    context.getSource().sendFailure(Component.literal("This command must be run by a player"));
                }
                return 1;
            })
        );
    }
}

