package com.github.ars_affinity.registry;

import com.github.ars_affinity.ArsAffinity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, ArsAffinity.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> DASH = SOUNDS.register("dash", () -> SoundEvent.createVariableRangeEvent(ArsAffinity.prefix("dash")));
}


