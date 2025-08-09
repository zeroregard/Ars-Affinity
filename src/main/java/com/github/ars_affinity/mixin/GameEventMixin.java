package com.github.ars_affinity.mixin;

import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public class GameEventMixin {
    
    @Inject(method = "gameEvent(Lnet/minecraft/world/level/gameevent/GameEvent;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/gameevent/GameEvent$Context;)V", at = @At("HEAD"), cancellable = true)
    private void onGameEvent(GameEvent gameEvent, BlockPos pos, GameEvent.Context context, CallbackInfo ci) {
        if (((Level)(Object)this).isClientSide()) {
            return;
        }
        
        Entity sourceEntity = context.sourceEntity();
        if (!(sourceEntity instanceof Player player)) {
            return;
        }
        
        // Check if the game event is one that skulk sensors detect
        if (isSkulkDetectableEvent(gameEvent)) {
            var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
            if (progress != null) {
                int earthTier = progress.getTier(SpellSchools.ELEMENTAL_EARTH);
                if (earthTier >= 2) { // Tier 2 or higher
                    AffinityPerkHelper.applyHighestTierPerk(progress, earthTier, SpellSchools.ELEMENTAL_EARTH, AffinityPerkType.PASSIVE_IGNORE_SKULK, perk -> {
                        if (perk instanceof AffinityPerk.SimplePerk) {
                            // Cancel the game event to prevent skulk sensors from detecting it
                            ci.cancel();
                        }
                    });
                }
            }
        }
    }
    
    private boolean isSkulkDetectableEvent(GameEvent gameEvent) {
        // Check if this is a game event that skulk sensors typically detect
        return gameEvent == GameEvent.STEP.value() || 
               gameEvent == GameEvent.SWIM.value() || 
               gameEvent == GameEvent.FLAP.value() ||
               gameEvent == GameEvent.PROJECTILE_SHOOT.value() ||
               gameEvent == GameEvent.ITEM_INTERACT_START.value() ||
               gameEvent == GameEvent.ITEM_INTERACT_FINISH.value() ||
               gameEvent == GameEvent.BLOCK_PLACE.value() ||
               gameEvent == GameEvent.BLOCK_DESTROY.value() ||
               gameEvent == GameEvent.FLUID_PLACE.value() ||
               gameEvent == GameEvent.FLUID_PICKUP.value() ||
               gameEvent == GameEvent.CONTAINER_OPEN.value() ||
               gameEvent == GameEvent.CONTAINER_CLOSE.value() ||
               gameEvent == GameEvent.BLOCK_OPEN.value() ||
               gameEvent == GameEvent.BLOCK_CLOSE.value() ||
               gameEvent == GameEvent.BLOCK_ACTIVATE.value() ||
               gameEvent == GameEvent.BLOCK_DEACTIVATE.value() ||
               gameEvent == GameEvent.BLOCK_ATTACH.value() ||
               gameEvent == GameEvent.BLOCK_DETACH.value() ||
               gameEvent == GameEvent.EAT.value() ||
               gameEvent == GameEvent.ELYTRA_GLIDE.value() ||
               gameEvent == GameEvent.EQUIP.value() ||
               gameEvent == GameEvent.EXPLODE.value() ||
               gameEvent == GameEvent.HIT_GROUND.value() ||
               gameEvent == GameEvent.INSTRUMENT_PLAY.value() ||
               gameEvent == GameEvent.JUKEBOX_PLAY.value() ||
               gameEvent == GameEvent.JUKEBOX_STOP_PLAY.value() ||
               gameEvent == GameEvent.LIGHTNING_STRIKE.value() ||
               gameEvent == GameEvent.NOTE_BLOCK_PLAY.value() ||
               gameEvent == GameEvent.PRIME_FUSE.value() ||
               gameEvent == GameEvent.PROJECTILE_LAND.value() ||
               gameEvent == GameEvent.SCULK_SENSOR_TENDRILS_CLICKING.value() ||
               gameEvent == GameEvent.SHEAR.value() ||
               gameEvent == GameEvent.SHRIEK.value() ||
               gameEvent == GameEvent.SPLASH.value() ||
               gameEvent == GameEvent.TELEPORT.value() ||
               gameEvent == GameEvent.UNEQUIP.value();
    }
}