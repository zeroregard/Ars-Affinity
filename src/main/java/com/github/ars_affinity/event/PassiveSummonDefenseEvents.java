package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.PlayerAffinityDataHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkManager;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.event.SummonEvent;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;

public class PassiveSummonDefenseEvents {

    @SubscribeEvent
    public static void onSummonEvent(SummonEvent event) {
        if (!(event.shooter instanceof Player player)) return;
        if (event.world.isClientSide()) return;

        // Check if player has the summon defense perk allocated
        if (AffinityPerkHelper.hasActivePerk(player, AffinityPerkType.PASSIVE_SUMMON_DEFENSE)) {
            if (event.summon.getLivingEntity() != null) {
                // Get the highest tier the player has allocated for this perk
                int highestTier = AffinityPerkHelper.getPerkTier(player, AffinityPerkType.PASSIVE_SUMMON_DEFENSE);
                equipArmorToSummon(event.summon.getLivingEntity(), highestTier, event.world);
                ArsAffinity.LOGGER.debug("Player {} summoned entity with PASSIVE_SUMMON_DEFENSE perk at tier {}",
                    player.getName().getString(), highestTier);
            }
        }
    }

    private static void equipArmorToSummon(net.minecraft.world.entity.LivingEntity summon, int conjurationTier, net.minecraft.world.level.Level level) {
        if (summon instanceof net.minecraft.world.entity.animal.horse.Horse horse) {
            equipHorseArmor(horse, conjurationTier, level);
        } else if (summon instanceof net.minecraft.world.entity.Mob mob) {
            equipMobArmor(mob, conjurationTier, level);
        }
    }

    private static void equipHorseArmor(net.minecraft.world.entity.animal.horse.Horse horse, int conjurationTier, net.minecraft.world.level.Level level) {
        net.minecraft.world.item.Item horseArmor;

        switch (conjurationTier) {
            case 1:
                horseArmor = Items.IRON_HORSE_ARMOR;
                break;
            case 2:
                horseArmor = Items.GOLDEN_HORSE_ARMOR;
                break;
            case 3:
                horseArmor = Items.DIAMOND_HORSE_ARMOR;
                break;
            default:
                // For any tier above 3, use diamond (or could be configurable)
                horseArmor = Items.DIAMOND_HORSE_ARMOR;
                break;
        }

        ItemStack horseArmorStack = new ItemStack(horseArmor);
        
        // Horse armor can't be enchanted, so no enchantment logic needed
        
        horse.setItemSlot(EquipmentSlot.CHEST, horseArmorStack);
        horse.setDropChance(EquipmentSlot.CHEST, 0.0F);

        ArsAffinity.LOGGER.debug("Equipped {} horse armor to summon {} for tier {}",
            conjurationTier == 1 ? "iron" : conjurationTier == 2 ? "golden" : "diamond",
            horse.getType().getDescriptionId(), conjurationTier);
    }

    private static void equipMobArmor(net.minecraft.world.entity.Mob mob, int conjurationTier, net.minecraft.world.level.Level level) {
        net.minecraft.world.item.Item helmet, chestplate, leggings, boots;

        switch (conjurationTier) {
            case 1:
                helmet = Items.LEATHER_HELMET;
                chestplate = Items.LEATHER_CHESTPLATE;
                leggings = Items.LEATHER_LEGGINGS;
                boots = Items.LEATHER_BOOTS;
                break;
            case 2:
                helmet = Items.IRON_HELMET;
                chestplate = Items.IRON_CHESTPLATE;
                leggings = Items.IRON_LEGGINGS;
                boots = Items.IRON_BOOTS;
                break;
            case 3:
                helmet = Items.NETHERITE_HELMET;
                chestplate = Items.NETHERITE_CHESTPLATE;
                leggings = Items.NETHERITE_LEGGINGS;
                boots = Items.NETHERITE_BOOTS;
                break;
            default:
                // For any tier above 3, use netherite (or could be configurable)
                helmet = Items.NETHERITE_HELMET;
                chestplate = Items.NETHERITE_CHESTPLATE;
                leggings = Items.NETHERITE_LEGGINGS;
                boots = Items.NETHERITE_BOOTS;
                break;
        }

        ItemStack helmetStack = new ItemStack(helmet);
        ItemStack chestplateStack = new ItemStack(chestplate);
        ItemStack leggingsStack = new ItemStack(leggings);
        ItemStack bootsStack = new ItemStack(boots);



        mob.setItemSlot(EquipmentSlot.HEAD, helmetStack);
        mob.setItemSlot(EquipmentSlot.CHEST, chestplateStack);
        mob.setItemSlot(EquipmentSlot.LEGS, leggingsStack);
        mob.setItemSlot(EquipmentSlot.FEET, bootsStack);

        mob.setDropChance(EquipmentSlot.HEAD, 0.0F);
        mob.setDropChance(EquipmentSlot.CHEST, 0.0F);
        mob.setDropChance(EquipmentSlot.LEGS, 0.0F);
        mob.setDropChance(EquipmentSlot.FEET, 0.0F);

        ArsAffinity.LOGGER.debug("Equipped {} armor to summon {} for tier {}",
            conjurationTier == 1 ? "leather" : conjurationTier == 2 ? "iron" : "netherite",
            mob.getType().getDescriptionId(), conjurationTier);
    }
}
