package com.github.ars_affinity.common.ability;

import com.github.ars_affinity.perk.AffinityPerk;
import net.minecraft.server.level.ServerPlayer;

public interface AbilityHelper {
    
    /**
     * Execute the ability for the given player
     * @param player The player executing the ability
     * @param perk The active ability perk data
     * @param requiredMana The mana cost of the ability
     * @param currentMana The player's current mana
     */
    void executeAbility(ServerPlayer player, AffinityPerk.ActiveAbilityPerk perk, int requiredMana, int currentMana);
} 