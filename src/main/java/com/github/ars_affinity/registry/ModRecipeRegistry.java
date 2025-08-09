package com.github.ars_affinity.registry;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.common.recipe.AnchorCharmChargingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipeRegistry {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, ArsAffinity.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, ArsAffinity.MOD_ID);

    public static final String CHARM_CHARGING_RECIPE_ID = "charm_charging";

    public static final DeferredHolder<RecipeType<?>, RecipeType<AnchorCharmChargingRecipe>> CHARM_CHARGING_TYPE = RECIPE_TYPES.register(CHARM_CHARGING_RECIPE_ID, ModRecipeType::new);
    public static final DeferredHolder<RecipeSerializer<?>, AnchorCharmChargingRecipe.Serializer> CHARM_CHARGING_SERIALIZER = RECIPE_SERIALIZERS.register(CHARM_CHARGING_RECIPE_ID, AnchorCharmChargingRecipe.Serializer::new);

    private static class ModRecipeType<T extends Recipe<?>> implements RecipeType<T> {
        @Override
        public String toString() {
            return BuiltInRegistries.RECIPE_TYPE.getKey(this).toString();
        }
    }
}