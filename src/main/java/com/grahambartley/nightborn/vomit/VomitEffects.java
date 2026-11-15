package com.grahambartley.nightborn.vomit;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class VomitEffects {

  public static final SoundEvent RETCH = sound("entity.vampire.retch");
  public static final SimpleParticleType DROP = particle("vomit");
  public static final SimpleParticleType SPLASH = particle("vomit_splash");

  private VomitEffects() {}

  public static void register() {}

  private static SoundEvent sound(final String path) {
    final Identifier id = Identifier.of(Nightborn.MOD_ID, path);
    return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
  }

  private static SimpleParticleType particle(final String path) {
    return Registry.register(
        Registries.PARTICLE_TYPE,
        Identifier.of(Nightborn.MOD_ID, path),
        FabricParticleTypes.simple());
  }
}
