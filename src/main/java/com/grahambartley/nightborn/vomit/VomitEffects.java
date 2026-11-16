package com.grahambartley.nightborn.vomit;

import com.grahambartley.nightborn.particle.ModParticles;
import com.grahambartley.nightborn.sound.NightbornSounds;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.sound.SoundEvent;

public final class VomitEffects {

  public static final SoundEvent RETCH_MALE = NightbornSounds.register("entity.vampire.male.retch");
  public static final SoundEvent RETCH_FEMALE =
      NightbornSounds.register("entity.vampire.female.retch");
  public static final SimpleParticleType DROP = ModParticles.simple("vomit");
  public static final SimpleParticleType SPLASH = ModParticles.simple("vomit_splash");

  private VomitEffects() {}

  public static void register() {}

  public static SoundEvent retch(final boolean slim) {
    return slim ? RETCH_FEMALE : RETCH_MALE;
  }
}
