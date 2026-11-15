package com.grahambartley.nightborn.client.particle;

import java.util.Optional;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.particle.SimpleParticleType;

public final class ParticleSprites {

  private SpriteProvider sprites;

  private ParticleSprites() {}

  public static ParticleSprites of(final SimpleParticleType type) {
    final ParticleSprites holder = new ParticleSprites();
    ParticleFactoryRegistry.getInstance()
        .register(
            type,
            sprites -> {
              holder.sprites = sprites;
              return (ParticleFactory<SimpleParticleType>)
                  (particle, world, x, y, z, vx, vy, vz) -> null;
            });
    return holder;
  }

  public Optional<SpriteProvider> get() {
    return Optional.ofNullable(this.sprites);
  }
}
