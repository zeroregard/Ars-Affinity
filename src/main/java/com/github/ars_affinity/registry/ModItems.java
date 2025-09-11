package com.github.ars_affinity.registry;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.common.item.TabletOfAmnesia;
import com.hollingsworth.arsnouveau.setup.registry.ItemRegistryWrapper;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, ArsAffinity.MOD_ID);

    public static final ItemRegistryWrapper<TabletOfAmnesia> TABLET_OF_AMNESIA = register("tablet_of_amnesia", TabletOfAmnesia::new);


    private static <T extends Item> ItemRegistryWrapper<T> register(String name, java.util.function.Supplier<T> item) {
        return new ItemRegistryWrapper<>(ITEMS.register(name, item));
    }

    public static Item.Properties defaultItemProperties() {
        return new Item.Properties();
    }
} 