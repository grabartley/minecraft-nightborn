package com.grahambartley.nightborn.gore;

import com.grahambartley.nightborn.particle.ModParticles;
import net.minecraft.particle.SimpleParticleType;

public final class GoreParticles {

  public static final SimpleParticleType DROP = ModParticles.simple("blood_drop");
  public static final SimpleParticleType SPLAT = ModParticles.simple("blood_splat");

  private GoreParticles() {}

  public static void register() {}
}
