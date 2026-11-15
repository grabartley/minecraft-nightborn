package com.grahambartley.nightborn.client.smoke;

import com.grahambartley.nightborn.smoke.SmokeParticles;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.particle.SimpleParticleType;

public final class SmokeFx {

  private SmokeFx() {}

  public static void register() {
    ParticleFactoryRegistry.getInstance()
        .register(
            SmokeParticles.VAMPIRE_SMOKE,
            sprites ->
                (ParticleFactory<SimpleParticleType>)
                    (type, world, x, y, z, velocityX, velocityY, velocityZ) ->
                        new VampireSmokeParticle(
                            world, x, y, z, velocityX, velocityY, velocityZ, sprites));
  }
}
