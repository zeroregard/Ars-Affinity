package com.github.ars_affinity.common.ritual;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.PlayerAffinityData;
import com.github.ars_affinity.capability.PlayerAffinityDataHelper;
import com.github.ars_affinity.util.ChatMessageHelper;
import com.hollingsworth.arsnouveau.api.ritual.AbstractRitual;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import alexthw.ars_elemental.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class RitualAmnesia extends AbstractRitual {
    
    @Override
    protected void tick() {
        Level world = getWorld();
        if (world.isClientSide) {
            return;
        }
        
        if (!getContext().isStarted) {
            return;
        }
        
        // Find the school to reset based on consumed essences
        SpellSchool targetSchool = getTargetSchool();
        if (targetSchool != null) {
            resetSpecificSchool(world, targetSchool);
        }
        
        // Mark ritual as done
        getContext().isDone = true;
    }
    
    private SpellSchool getTargetSchool() {
        // Check for consumed essences
        if (didConsumeItem(ItemsRegistry.FIRE_ESSENCE.get())) {
            return SpellSchools.ELEMENTAL_FIRE;
        } else if (didConsumeItem(ItemsRegistry.WATER_ESSENCE.get())) {
            return SpellSchools.ELEMENTAL_WATER;
        } else if (didConsumeItem(ItemsRegistry.EARTH_ESSENCE.get())) {
            return SpellSchools.ELEMENTAL_EARTH;
        } else if (didConsumeItem(ItemsRegistry.AIR_ESSENCE.get())) {
            return SpellSchools.ELEMENTAL_AIR;
        } else if (didConsumeItem(ItemsRegistry.ABJURATION_ESSENCE.get())) {
            return SpellSchools.ABJURATION;
        } else if (didConsumeItem(ItemsRegistry.CONJURATION_ESSENCE.get())) {
            return SpellSchools.CONJURATION;
        } else if (didConsumeItem(ItemsRegistry.MANIPULATION_ESSENCE.get())) {
            return SpellSchools.MANIPULATION;
        } else if (didConsumeItem(ModItems.ANIMA_ESSENCE.get())) {
            return SpellSchools.NECROMANCY;
        }
        return null;
    }
    
    
    private void resetSpecificSchool(Level world, SpellSchool school) {
        BlockPos pos = getPos();
        if (pos == null) return;
        
        // Get all players within ritual range (6 blocks)
        AABB area = new AABB(pos).inflate(6);
        List<Player> players = world.getEntitiesOfClass(Player.class, area);
        
        for (Player player : players) {
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                PlayerAffinityData data = PlayerAffinityDataHelper.getPlayerAffinityData(player);
                int pointsToReset = data.getSchoolPoints(school);
                
                // Reset school points and percentage
                data.setSchoolPoints(school, 0);
                data.resetSchoolPercentage(school);
                
                data.respecSchool(school);
                PlayerAffinityDataHelper.savePlayerData(player);
                ChatMessageHelper.sendSchoolResetMessage(serverPlayer, school, pointsToReset);
            }
        }
    }
    
    @Override
    public boolean canConsumeItem(ItemStack stack) {
        // Allow consumption of any essence
        return stack.getItem() == ItemsRegistry.FIRE_ESSENCE.get() ||
               stack.getItem() == ItemsRegistry.WATER_ESSENCE.get() ||
               stack.getItem() == ItemsRegistry.EARTH_ESSENCE.get() ||
               stack.getItem() == ItemsRegistry.AIR_ESSENCE.get() ||
               stack.getItem() == ItemsRegistry.ABJURATION_ESSENCE.get() ||
               stack.getItem() == ItemsRegistry.CONJURATION_ESSENCE.get() ||
               stack.getItem() == ItemsRegistry.MANIPULATION_ESSENCE.get() ||
               stack.getItem() == ModItems.ANIMA_ESSENCE.get();
    }
    
    @Override
    public ResourceLocation getRegistryName() {
        return ArsAffinity.prefix("ritual_amnesia");
    }
    
    @Override
    public String getName() {
        return "Ritual of Amnesia";
    }
    
    @Override
    public String getDescriptionKey() {
        return "ritual.ars_affinity.amnesia";
    }
    
    @Override
    public boolean canStart(Player player) {
        // Check if there's at least one essence in the ritual brazier
        return getContext().consumedItems.size() > 0;
    }
    
    @Override
    public void modifyTooltips(List<Component> tooltips) {
        tooltips.add(Component.translatable("tooltip.ars_affinity.tablet_of_amnesia"));
        tooltips.add(Component.translatable("tooltip.ars_affinity.tablet_of_amnesia.essence_required"));
    }
}
