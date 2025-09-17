package com.github.ars_affinity.registry;

import com.github.ars_affinity.ArsAffinity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, ArsAffinity.MOD_ID);

    public static Item.Properties defaultItemProperties() {
        return new Item.Properties();
    }
} 