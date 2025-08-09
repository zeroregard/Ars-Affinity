package com.github.ars_affinity.common.recipe;

import com.github.ars_affinity.common.item.AffinityAnchorCharm;
import com.github.ars_affinity.registry.ModRecipeRegistry;
import com.hollingsworth.arsnouveau.api.imbuement_chamber.IImbuementRecipe;
import com.hollingsworth.arsnouveau.common.block.tile.ImbuementTile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public record AnchorCharmChargingRecipe(ResourceLocation id, Item input, int costPerCharge) implements IImbuementRecipe {
    
    @Override
    public boolean matches(ImbuementTile imbuementTile, Level level) {
        ItemStack reagent = imbuementTile.stack;
        if (reagent.getItem() instanceof AffinityAnchorCharm charm) {
            // Only allow recharging if the charm has damage (is not fully charged)
            if (charm.getDamage(reagent) == 0) {
                return false;
            }
            return reagent.is(input);
        }
        return false;
    }

    @Override
    public ItemStack assemble(ImbuementTile imbuementTile, HolderLookup.Provider provider) {
        ItemStack reagent = imbuementTile.stack;
        ItemStack result = reagent.copy();
        // Fully recharge the charm
        AffinityAnchorCharm.setCharges(result, result.getMaxDamage());
        return result;
    }

    @Override
    public int getSourceCost(ImbuementTile imbuementTile) {
        ItemStack reagent = imbuementTile.stack;
        return reagent.getDamageValue() * costPerCharge;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeRegistry.CHARM_CHARGING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeRegistry.CHARM_CHARGING_TYPE.get();
    }

    @Override
    public Component getCraftingStartedText(ImbuementTile imbuementTile) {
        return Component.translatable("chat.ars_affinity.anchor_charm.charging_started", 
            assemble(imbuementTile, imbuementTile.getLevel().registryAccess()).getHoverName());
    }

    @Override
    public Component getCraftingText(ImbuementTile imbuementTile) {
        return Component.translatable("tooltip.ars_affinity.anchor_charm.charging", 
            assemble(imbuementTile, imbuementTile.getLevel().registryAccess()).getHoverName());
    }

    @Override
    public Component getCraftingProgressText(ImbuementTile imbuementTile, int progress) {
        return Component.translatable("tooltip.ars_affinity.anchor_charm.charging_progress", progress)
            .withStyle(ChatFormatting.GOLD);
    }

    public static class Serializer implements RecipeSerializer<AnchorCharmChargingRecipe> {
        public static final MapCodec<AnchorCharmChargingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("id").forGetter(AnchorCharmChargingRecipe::id),
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(AnchorCharmChargingRecipe::input),
                Codec.INT.optionalFieldOf("costPerDamage", 5).forGetter(AnchorCharmChargingRecipe::costPerCharge)
        ).apply(instance, AnchorCharmChargingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, AnchorCharmChargingRecipe> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, AnchorCharmChargingRecipe::id,
                ByteBufCodecs.registry(Registries.ITEM), AnchorCharmChargingRecipe::input,
                ByteBufCodecs.INT, AnchorCharmChargingRecipe::costPerCharge,
                AnchorCharmChargingRecipe::new
        );

        @Override
        public MapCodec<AnchorCharmChargingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AnchorCharmChargingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}