package com.grahambartley.nightborn.vomit;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.particle.ModParticles;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class VomitEffects {

  public static final SoundEvent RETCH_MALE = sound("entity.vampire.male.retch");
  public static final SoundEvent RETCH_FEMALE = sound("entity.vampire.female.retch");
  public static final SimpleParticleType DROP = ModParticles.simple("vomit");
  public static final SimpleParticleType SPLASH = ModParticles.simple("vomit_splash");

  private VomitEffects() {}

  public static void register() {}

  public static SoundEvent retch(final boolean slim) {
    return slim ? RETCH_FEMALE : RETCH_MALE;
  }

  private static SoundEvent sound(final String path) {
    final Identifier id = Identifier.of(Nightborn.MOD_ID, path);
    return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
  }
}
