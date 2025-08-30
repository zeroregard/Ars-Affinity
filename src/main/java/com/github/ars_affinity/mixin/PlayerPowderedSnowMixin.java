package com.github.ars_affinity.mixin;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkManager;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

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
            var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
            if (progress != null) {
                int waterTier = progress.getTier(SpellSchools.ELEMENTAL_WATER);
                if (waterTier > 0) {
                    List<AffinityPerk> perks = AffinityPerkManager.getPerksForCurrentLevel(SpellSchools.ELEMENTAL_WATER, waterTier);
                    for (AffinityPerk perk : perks) {
                        if (perk.perk == AffinityPerkType.PASSIVE_COLD_WALKER) {
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
                }
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
            var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
            if (progress != null) {
                int waterTier = progress.getTier(SpellSchools.ELEMENTAL_WATER);
                if (waterTier > 0) {
                    List<AffinityPerk> perks = AffinityPerkManager.getPerksForCurrentLevel(SpellSchools.ELEMENTAL_WATER, waterTier);
                    for (AffinityPerk perk : perks) {
                        if (perk.perk == AffinityPerkType.PASSIVE_COLD_WALKER) {
                        
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
        }
    }
}
