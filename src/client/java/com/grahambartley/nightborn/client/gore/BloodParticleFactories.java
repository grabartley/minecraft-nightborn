package com.grahambartley.nightborn.client.gore;

import com.grahambartley.nightborn.gore.GoreParticles;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class BloodParticleFactories {

  private static SpriteProvider drops;
  private static SpriteProvider splats;

  private BloodParticleFactories() {}

  static void register() {
    ParticleFactoryRegistry.getInstance()
        .register(
            GoreParticles.DROP,
            sprites -> {
              drops = sprites;
              return (ParticleFactory<SimpleParticleType>)
                  (type, world, x, y, z, vx, vy, vz) ->
                      new BloodDropParticle(
                          world, x, y, z, vx, vy, vz, sprites, SplatterLook.FRESH);
            });
    ParticleFactoryRegistry.getInstance()
        .register(
            GoreParticles.SPLAT,
            sprites -> {
              splats = sprites;
              return (ParticleFactory<SimpleParticleType>)
                  (type, world, x, y, z, vx, vy, vz) ->
                      new BloodSplatParticle(
                          world, x, y, z, Direction.UP, sprites, SplatterLook.FRESH, false);
            });
  }

  static void spawnDrop(
      final MinecraftClient client, final Vec3d at, final Vec3d velocity, final SplatterLook look) {
    if (client.world == null || drops == null) {
      return;
    }
    client.particleManager.addParticle(
        new BloodDropParticle(
            client.world, at.x, at.y, at.z, velocity.x, velocity.y, velocity.z, drops, look));
  }

  static void spawnSplat(
      final ClientWorld world,
      final double x,
      final double y,
      final double z,
      final Direction face,
      final SplatterLook look,
      final boolean pool) {
    if (splats != null) {
      MinecraftClient.getInstance()
          .particleManager
          .addParticle(new BloodSplatParticle(world, x, y, z, face, splats, look, pool));
    }
  }
}
