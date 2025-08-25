package com.github.ars_affinity.common.item;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.config.ArsAffinityConfig;
import com.alexthw.sauce.api.item.AbstractCharm;
import com.alexthw.sauce.util.CharmUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AffinityAnchorCharm extends AbstractCharm {
    private static final int DEFAULT_CHARGES = 1000; // Fallback value

    public AffinityAnchorCharm() {
        super(getDefaultCharges());
    }

    private static int getDefaultCharges() {
        try {
            return ArsAffinityConfig.ANCHOR_CHARM_DEFAULT_CHARGES.get();
        } catch (IllegalStateException e) {
            // Config not loaded yet, return fallback
            return DEFAULT_CHARGES;
        }
    }

    public static boolean hasCharges(ItemStack stack) {
        return CharmUtil.isEnabled(stack);
    }

    public static void useCharge(ItemStack stack) {
        int chargesBefore = getCharges(stack);
        CharmUtil.useCharges(stack, 1);
        int chargesAfter = getCharges(stack);
        ArsAffinity.LOGGER.debug("Anchor Charm charge consumed: {} -> {} charges", chargesBefore, chargesAfter);
    }

    public static int getCharges(ItemStack stack) {
        return CharmUtil.getCharges(stack);
    }

    public static void setCharges(ItemStack stack, int charges) {
        CharmUtil.setCharges(stack, charges);
    }
} 