package com.github.ars_affinity.registry;

import com.github.ars_affinity.ArsAffinity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, ArsAffinity.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> DASH = SOUNDS.register("dash", () -> SoundEvent.createVariableRangeEvent(ArsAffinity.prefix("dash")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GROUND_SLAM = SOUNDS.register("ground_slam", () -> SoundEvent.createVariableRangeEvent(ArsAffinity.prefix("ground_slam")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ICE_BLAST = SOUNDS.register("ice_blast", () -> SoundEvent.createVariableRangeEvent(ArsAffinity.prefix("ice_blast")));
    
    // School tier change sounds
   
    public static final DeferredHolder<SoundEvent, SoundEvent> TIER_CHANGE_FIRE = SOUNDS.register("tier_change_fire", () -> SoundEvent.createVariableRangeEvent(ArsAffinity.prefix("tier_change_fire")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TIER_CHANGE_WATER = SOUNDS.register("tier_change_water", () -> SoundEvent.createVariableRangeEvent(ArsAffinity.prefix("tier_change_water")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TIER_CHANGE_EARTH = SOUNDS.register("tier_change_earth", () -> SoundEvent.createVariableRangeEvent(ArsAffinity.prefix("tier_change_earth")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TIER_CHANGE_AIR = SOUNDS.register("tier_change_air", () -> SoundEvent.createVariableRangeEvent(ArsAffinity.prefix("tier_change_air")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TIER_CHANGE_ABJURATION = SOUNDS.register("tier_change_abjuration", () -> SoundEvent.createVariableRangeEvent(ArsAffinity.prefix("tier_change_abjuration")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TIER_CHANGE_CONJURATION = SOUNDS.register("tier_change_conjuration", () -> SoundEvent.createVariableRangeEvent(ArsAffinity.prefix("tier_change_conjuration")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TIER_CHANGE_NECROMANCY = SOUNDS.register("tier_change_necromancy", () -> SoundEvent.createVariableRangeEvent(ArsAffinity.prefix("tier_change_necromancy")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TIER_CHANGE_MANIPULATION = SOUNDS.register("tier_change_manipulation", () -> SoundEvent.createVariableRangeEvent(ArsAffinity.prefix("tier_change_manipulation")));
    
    public static final DeferredHolder<SoundEvent, SoundEvent> CURSE_FIELD = SOUNDS.register("curse_field", () -> SoundEvent.createVariableRangeEvent(ArsAffinity.prefix("curse_field")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SANCTUARY = SOUNDS.register("sanctuary", () -> SoundEvent.createVariableRangeEvent(ArsAffinity.prefix("sanctuary")));
}
