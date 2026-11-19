package com.grahambartley.nightborn.occult;

import com.grahambartley.nightborn.particle.ModParticles;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.random.Random;

public final class FamiliarWisps {

  public static final SimpleParticleType WISP = ModParticles.simple("familiar_wisp");

  private FamiliarWisps() {}

  public static void register() {}

  public static void trail(final GhostlyFamiliarEntity familiar) {
    if (!WispTrail.sheds(familiar.age, familiar.fading())) {
      return;
    }
    final Random random = familiar.getRandom();
    familiar
        .getWorld()
        .addParticle(
            WISP,
            familiar.getX() + WispTrail.offset(random.nextDouble()),
            familiar.getY() + WispTrail.height(),
            familiar.getZ() + WispTrail.offset(random.nextDouble()),
            0.0,
            WispTrail.driftUp(),
            0.0);
  }
}
