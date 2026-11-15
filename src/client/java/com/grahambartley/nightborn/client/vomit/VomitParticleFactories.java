package com.grahambartley.nightborn.client.vomit;

import com.grahambartley.nightborn.vomit.VomitEffects;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.Vec3d;

public final class VomitParticleFactories {

  private static SpriteProvider drops;
  private static SpriteProvider splashes;

  private VomitParticleFactories() {}

  static void register() {
    ParticleFactoryRegistry.getInstance()
        .register(
            VomitEffects.DROP,
            sprites -> {
              drops = sprites;
              return (ParticleFactory<SimpleParticleType>)
                  (type, world, x, y, z, vx, vy, vz) ->
                      new VomitDropParticle(world, x, y, z, vx, vy, vz, sprites, true);
            });
    ParticleFactoryRegistry.getInstance()
        .register(
            VomitEffects.SPLASH,
            sprites -> {
              splashes = sprites;
              return (ParticleFactory<SimpleParticleType>)
                  (type, world, x, y, z, vx, vy, vz) ->
                      new VomitSplashParticle(world, x, y, z, sprites);
            });
  }

  static void spawnDrop(
      final MinecraftClient client, final Vec3d at, final Vec3d velocity, final boolean splashing) {
    if (client.world == null || drops == null) {
      return;
    }
    client.particleManager.addParticle(
        new VomitDropParticle(
            client.world, at.x, at.y, at.z, velocity.x, velocity.y, velocity.z, drops, splashing));
  }

  static void spawnSplash(final ClientWorld world, final double x, final double y, final double z) {
    if (splashes != null) {
      MinecraftClient.getInstance()
          .particleManager
          .addParticle(new VomitSplashParticle(world, x, y, z, splashes));
    }
  }
}
