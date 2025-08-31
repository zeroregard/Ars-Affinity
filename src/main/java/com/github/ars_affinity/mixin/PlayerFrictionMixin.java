package com.github.ars_affinity.mixin;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkManager;
import com.github.ars_affinity.perk.AffinityPerkType;

import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;
import java.util.Set;

@Mixin(LivingEntity.class)
public abstract class PlayerFrictionMixin {

    private static final Set<BlockState> COLD_WALKER_BLOCKS = Set.of(
        Blocks.ICE.defaultBlockState(),
        Blocks.BLUE_ICE.defaultBlockState(),
        Blocks.PACKED_ICE.defaultBlockState(),
        Blocks.SNOW.defaultBlockState(),
        Blocks.SNOW_BLOCK.defaultBlockState(),
        Blocks.POWDER_SNOW.defaultBlockState()
    );


    @ModifyVariable(
            method = "travel(Lnet/minecraft/world/phys/Vec3;)V",
            at = @At("STORE"),
            ordinal = 0
    )
    private float modifyFriction(float friction) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        if (!(entity instanceof Player player)) {
            return friction;
        }
        
        if (!player.onGround()) {
            return friction;
        }
        
        BlockPos groundPos = player.blockPosition().below();
        BlockState groundState = player.level().getBlockState(groundPos);
        
        // Also check for thin snow above the player
        BlockPos abovePos = player.blockPosition();
        BlockState aboveState = player.level().getBlockState(abovePos);
        
        if (COLD_WALKER_BLOCKS.contains(groundState) || COLD_WALKER_BLOCKS.contains(aboveState)) {
            var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
            if (progress != null) {
                int waterTier = progress.getTier(SpellSchools.ELEMENTAL_WATER);
                if (waterTier > 0) {
                    // Get the perk directly from the manager
                    List<AffinityPerk> perks = AffinityPerkManager.getPerksForCurrentLevel(SpellSchools.ELEMENTAL_WATER, waterTier);
                    for (AffinityPerk perk : perks) {
                        if (perk.perk == AffinityPerkType.PASSIVE_COLD_WALKER) {
                            if (perk instanceof AffinityPerk.AmountBasedPerk amountPerk) {
                                float newFriction = 0.6F - (amountPerk.amount * 0.6F);
                                ArsAffinity.LOGGER.debug(
                                        "Player {} has COLD_WALKER perk with amount {} - overriding ice/snow/powdered snow friction from {} to {}",
                                        player.getName().getString(),
                                        amountPerk.amount,
                                        friction,
                                        newFriction
                                    );
                                // Calculate friction based on iceAmplifier: 0.6 - (iceAmplifier * 0.6)
                                // This means: iceAmplifier 0.0 = 0.6F (normal ground), iceAmplifier 1.0 = 0.0F (maximum speed)
                                return newFriction;
                            }
                            break;
                        }
                    }
                }
            }
        }
        
        return friction;
    }
}
