package com.github.ars_affinity.common.item;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.client.TooltipUtils;
import com.github.ars_affinity.common.item.data.AnchorCharmData;
import com.github.ars_affinity.config.ArsAffinityConfig;
import com.github.ars_affinity.registry.ModDataComponents;
import com.github.ars_affinity.registry.ModItems;
import com.hollingsworth.arsnouveau.api.item.ArsNouveauCurio;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;

public class AffinityAnchorCharm extends ArsNouveauCurio {
    private final int maxCharges;

    public AffinityAnchorCharm() {
        super(ModItems.defaultItemProperties().stacksTo(1).durability(getDefaultCharges()).component(ModDataComponents.ANCHOR_CHARM_DATA, new AnchorCharmData(getDefaultCharges())));
        this.maxCharges = getDefaultCharges();
    }

    private static int getDefaultCharges() {
        return ArsAffinityConfig.ANCHOR_CHARM_DEFAULT_CHARGES.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);

        tooltip.add(Component.translatable("tooltip.ars_affinity.anchor_charm.desc").withStyle(ChatFormatting.GRAY));

        int charges = AnchorCharmData.getOrDefault(stack, maxCharges).charges();
        tooltip.add(Component.translatable("tooltip.ars_affinity.anchor_charm.charges", charges, maxCharges).withStyle(ChatFormatting.GOLD));

        TooltipUtils.addOnShift(tooltip, () -> {
            tooltip.add(Component.translatable("tooltip.ars_affinity.anchor_charm.shift_info").withStyle(ChatFormatting.AQUA));
        }, "anchor_charm");
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return maxCharges;
    }

    @Override
    public int getDamage(ItemStack stack) {
        return maxCharges - AnchorCharmData.getOrDefault(stack, maxCharges).charges();
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        int charges = AnchorCharmData.getOrDefault(stack, maxCharges).charges();
        return charges != maxCharges;
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {}

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return AnchorCharmData.getOrDefault(stack, maxCharges).charges() > 0;
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    public static boolean hasCharges(ItemStack stack) {
        return AnchorCharmData.getOrDefault(stack, getDefaultCharges()).charges() > 0;
    }

    public static void useCharge(ItemStack stack) {
        int chargesBefore = getCharges(stack);
        AnchorCharmData.getOrDefault(stack, getDefaultCharges()).use(1).write(stack);
        int chargesAfter = getCharges(stack);
        ArsAffinity.LOGGER.debug("Anchor Charm charge consumed: {} -> {} charges", chargesBefore, chargesAfter);
    }

    public static void setCharges(ItemStack stack, int charges) {
        AnchorCharmData.getOrDefault(stack, getDefaultCharges()).set(charges).write(stack);
    }

    public static int getCharges(ItemStack stack) {
        return AnchorCharmData.getOrDefault(stack, getDefaultCharges()).charges();
    }
} 