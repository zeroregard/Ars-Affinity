package com.github.ars_affinity.mixin;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class PlayerPowderedSnowMixin {

    /**
     * Prevents players with COLD_WALKER perk from accumulating frozen ticks
     */
    @Inject(
        method = "setTicksFrozen",
        at = @At("HEAD"),
        cancellable = true
    )
    private void setTicksFrozen(int ticks, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        
        if (!(entity instanceof Player player)) {
            return;
        }

        // Only prevent freezing if trying to accumulate frozen ticks
        if (ticks > entity.getTicksFrozen()) {
            var perk = AffinityPerkHelper.getActivePerk(player, AffinityPerkType.PASSIVE_COLD_WALKER);
            if (perk instanceof AffinityPerk.AmountBasedPerk amountPerk && amountPerk.amount > 0) {
                // Keep current frozen ticks instead of increasing them
                ci.cancel();
                ArsAffinity.LOGGER.debug(
                    "Player {} has COLD_WALKER perk - preventing frozen tick accumulation",
                    player.getName().getString()
                );
                return;
            }
        }
    }

    /**
     * Prevents players with COLD_WALKER perk from getting movement slowdown in powdered snow
     * and improves their movement speed within it.
     */
    @Inject(
        method = "makeStuckInBlock",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onMakeStuckInBlock(BlockState state, Vec3 motionMultiplier, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        
        if (!(entity instanceof Player player)) {
            return;
        }

        if (state.getBlock() instanceof PowderSnowBlock) {
            var perk = AffinityPerkHelper.getActivePerk(player, AffinityPerkType.PASSIVE_COLD_WALKER);
            if (perk instanceof AffinityPerk) {
                // Cancel vanilla slowdown entirely
                ci.cancel();
                
                ArsAffinity.LOGGER.debug(
                    "Player {} has COLD_WALKER perk - no slowdown",
                    player.getName().getString()
                );
                return;
            }
        }
    }
}
