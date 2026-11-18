package com.grahambartley.nightborn.moment;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public enum MomentCue {
  HEARTBEAT(SoundEvents.ENTITY_WARDEN_HEARTBEAT, 1.0F, 0.7F, ParticleTypes.SMOKE),
  BELL(SoundEvents.BLOCK_BELL_USE, 1.0F, 0.5F, ParticleTypes.SMOKE),
  HORN(SoundEvents.GOAT_HORN_SOUNDS.get(5), 1.0F, 0.9F, ParticleTypes.END_ROD);

  private final RegistryEntry<SoundEvent> sound;
  private final float volume;
  private final float pitch;
  private final ParticleEffect particle;

  MomentCue(
      final SoundEvent sound,
      final float volume,
      final float pitch,
      final ParticleEffect particle) {
    this(RegistryEntry.of(sound), volume, pitch, particle);
  }

  MomentCue(
      final RegistryEntry<SoundEvent> sound,
      final float volume,
      final float pitch,
      final ParticleEffect particle) {
    this.sound = sound;
    this.volume = volume;
    this.pitch = pitch;
    this.particle = particle;
  }

  public RegistryEntry<SoundEvent> sound() {
    return sound;
  }

  public float volume() {
    return volume;
  }

  public float pitch() {
    return pitch;
  }

  public ParticleEffect particle() {
    return particle;
  }
}
