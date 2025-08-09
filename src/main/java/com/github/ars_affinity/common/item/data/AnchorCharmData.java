package com.github.ars_affinity.common.item.data;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.registry.ModDataComponents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record AnchorCharmData(int charges) {
    public static Codec<AnchorCharmData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.optionalFieldOf("charges", 0).forGetter(AnchorCharmData::charges)
    ).apply(instance, AnchorCharmData::new));

    public static StreamCodec<RegistryFriendlyByteBuf, AnchorCharmData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, AnchorCharmData::charges, AnchorCharmData::new
    );

    public AnchorCharmData use(int charges) {
        int newCharges = Math.max(this.charges - charges, 0);
        ArsAffinity.LOGGER.debug("AnchorCharmData: consuming {} charges, {} -> {}", charges, this.charges, newCharges);
        return set(newCharges);
    }

    public AnchorCharmData set(int charges) {
        int clampedCharges = Math.max(charges, 0);
        ArsAffinity.LOGGER.debug("AnchorCharmData: setting charges to {} (was {})", clampedCharges, this.charges);
        return new AnchorCharmData(clampedCharges);
    }

    public AnchorCharmData write(ItemStack stack) {
        ArsAffinity.LOGGER.debug("AnchorCharmData: writing {} charges to item stack", this.charges);
        return stack.set(ModDataComponents.ANCHOR_CHARM_DATA, this);
    }

    public static AnchorCharmData getOrDefault(ItemStack stack) {
        return getOrDefault(stack, 0);
    }

    public static AnchorCharmData getOrDefault(ItemStack stack, int charges) {
        return stack.getOrDefault(ModDataComponents.ANCHOR_CHARM_DATA, new AnchorCharmData(charges));
    }
    
} 