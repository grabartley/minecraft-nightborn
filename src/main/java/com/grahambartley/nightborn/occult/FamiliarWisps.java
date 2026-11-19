package com.grahambartley.nightborn.occult;

import com.grahambartley.nightborn.particle.ModParticles;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public final class FamiliarWisps {

  public static final SimpleParticleType WISP = ModParticles.simple("familiar_wisp");

  private FamiliarWisps() {}

  public static void register() {}

  public static void trail(final GhostlyFamiliarEntity familiar) {
    if (!familiar.isAlive() || !WispTrail.sheds(familiar.age, familiar.fading())) {
      return;
    }
    final Random random = familiar.getRandom();
    final Vec3d at = WispTrail.spawnAt(familiar.getPos(), random.nextDouble(), random.nextDouble());
    familiar.getWorld().addParticle(WISP, at.x, at.y, at.z, 0.0, WispTrail.DRIFT_UP, 0.0);
  }
}
