package com.github.ars_affinity.event;

import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.github.ars_affinity.perk.PerkData;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Event fired when a player's active perks change.
 * This can happen when:
 * - A player's tier changes (gaining/losing perks)
 * - A player's perk configuration changes
 * - A player gains/loses access to a perk type
 */
public class PerkChangeEvent extends PlayerEvent {
    private final AffinityPerkType perkType;
    private final PerkData oldPerkData;
    private final PerkData newPerkData;
    private final SpellSchool sourceSchool;
    private final int sourceTier;
    
    public PerkChangeEvent(Player player, AffinityPerkType perkType, PerkData oldPerkData, PerkData newPerkData) {
        super(player);
        this.perkType = perkType;
        this.oldPerkData = oldPerkData;
        this.newPerkData = newPerkData;
        this.sourceSchool = newPerkData != null ? newPerkData.sourceSchool : null;
        this.sourceTier = newPerkData != null ? newPerkData.sourceTier : 0;
    }
    
    /**
     * @return The type of perk that changed
     */
    public AffinityPerkType getPerkType() {
        return perkType;
    }
    
    /**
     * @return The previous perk data, or null if no perk was active before
     */
    public PerkData getOldPerkData() {
        return oldPerkData;
    }
    
    /**
     * @return The new perk data, or null if no perk is active now
     */
    public PerkData getNewPerkData() {
        return newPerkData;
    }
    
    /**
     * @return The school that provides the new perk, or null if no perk is active
     */
    public SpellSchool getSourceSchool() {
        return sourceSchool;
    }
    
    /**
     * @return The tier that provides the new perk, or 0 if no perk is active
     */
    public int getSourceTier() {
        return sourceTier;
    }
    
    /**
     * @return True if a perk was gained (had none before, have one now)
     */
    public boolean isPerkGained() {
        return oldPerkData == null && newPerkData != null;
    }
    
    /**
     * @return True if a perk was lost (had one before, have none now)
     */
    public boolean isPerkLost() {
        return oldPerkData != null && newPerkData == null;
    }
    
    /**
     * @return True if a perk was upgraded (same type but higher tier)
     */
    public boolean isPerkUpgraded() {
        return oldPerkData != null && newPerkData != null && 
               newPerkData.sourceTier > oldPerkData.sourceTier;
    }
    
    /**
     * @return True if a perk was downgraded (same type but lower tier)
     */
    public boolean isPerkDowngraded() {
        return oldPerkData != null && newPerkData != null && 
               newPerkData.sourceTier < oldPerkData.sourceTier;
    }
    
    /**
     * @return True if the source school changed for the same perk type
     */
    public boolean isSourceSchoolChanged() {
        return oldPerkData != null && newPerkData != null && 
               oldPerkData.sourceSchool != newPerkData.sourceSchool;
    }
}