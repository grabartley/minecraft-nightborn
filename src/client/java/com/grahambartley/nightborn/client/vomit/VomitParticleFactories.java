package com.grahambartley.nightborn.client.vomit;

import com.grahambartley.nightborn.client.particle.ParticleSprites;
import com.grahambartley.nightborn.vomit.VomitEffects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;

public final class VomitParticleFactories {

  private static ParticleSprites drops;
  private static ParticleSprites splashes;

  private VomitParticleFactories() {}

  static void register() {
    drops = ParticleSprites.of(VomitEffects.DROP);
    splashes = ParticleSprites.of(VomitEffects.SPLASH);
  }

  static void spawnDrop(
      final MinecraftClient client, final Vec3d at, final Vec3d velocity, final boolean splashing) {
    if (client.world != null && drops != null) {
      drops
          .get()
          .ifPresent(
              sprites ->
                  client.particleManager.addParticle(
                      new VomitDropParticle(
                          client.world,
                          at.x,
                          at.y,
                          at.z,
                          velocity.x,
                          velocity.y,
                          velocity.z,
                          sprites,
                          splashing)));
    }
  }

  static void spawnSplash(final ClientWorld world, final double x, final double y, final double z) {
    if (splashes != null) {
      splashes
          .get()
          .ifPresent(
              sprites ->
                  MinecraftClient.getInstance()
                      .particleManager
                      .addParticle(new VomitSplashParticle(world, x, y, z, sprites)));
    }
  }
}
