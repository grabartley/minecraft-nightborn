package com.grahambartley.nightborn.client.occult;

import com.grahambartley.nightborn.occult.FamiliarWisps;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.particle.SimpleParticleType;

public final class WispFx {

  private WispFx() {}

  public static void register() {
    ParticleFactoryRegistry.getInstance()
        .register(
            FamiliarWisps.WISP,
            sprites ->
                (ParticleFactory<SimpleParticleType>)
                    (type, world, x, y, z, velocityX, velocityY, velocityZ) ->
                        new FamiliarWispParticle(
                            world, x, y, z, velocityX, velocityY, velocityZ, sprites));
  }
}
