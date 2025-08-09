package com.github.ars_affinity.util;

import com.github.ars_affinity.common.item.AffinityAnchorCharm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;

public class CuriosHelper {
    
    public static boolean hasActiveAnchorCharm(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof AffinityAnchorCharm && AffinityAnchorCharm.hasCharges(mainHand)) {
            return true;
        }
        
        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof AffinityAnchorCharm && AffinityAnchorCharm.hasCharges(offHand)) {
            return true;
        }
        
        Optional<SlotResult> curiosCharm = findCuriosCharm(player);
        if (curiosCharm.isPresent()) {
            return true;
        }
        
        return false;
    }
    
    public static void consumeAnchorCharmCharge(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof AffinityAnchorCharm && AffinityAnchorCharm.hasCharges(mainHand)) {
            AffinityAnchorCharm.useCharge(mainHand);
            checkCharmCharges(player, mainHand, "main hand");
            return;
        }
        
        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof AffinityAnchorCharm && AffinityAnchorCharm.hasCharges(offHand)) {
            AffinityAnchorCharm.useCharge(offHand);
            checkCharmCharges(player, offHand, "offhand");
            return;
        }
        
        Optional<SlotResult> curiosCharm = findCuriosCharm(player);
        if (curiosCharm.isPresent()) {
            ItemStack charmStack = curiosCharm.get().stack();
            AffinityAnchorCharm.useCharge(charmStack);
            checkCharmCharges(player, charmStack, "Curios slot");
            return;
        }
    }
    
    private static Optional<SlotResult> findCuriosCharm(Player player) {
        return CuriosApi.getCuriosInventory(player)
            .flatMap(curios -> curios.findFirstCurio(stack -> 
                stack.getItem() instanceof AffinityAnchorCharm && AffinityAnchorCharm.hasCharges(stack)));
    }
    
    private static void checkCharmCharges(Player player, ItemStack charm, String location) {
        // Charge monitoring logic can be added here if needed
    }
} 