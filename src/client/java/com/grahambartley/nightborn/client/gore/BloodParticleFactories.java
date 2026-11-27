package com.grahambartley.nightborn.client.gore;

import com.grahambartley.nightborn.client.particle.FlatDecalParticle;
import com.grahambartley.nightborn.client.particle.ParticleSprites;
import com.grahambartley.nightborn.gore.GoreParticles;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class BloodParticleFactories {

  private static ParticleSprites drops;
  private static ParticleSprites splats;

  private BloodParticleFactories() {}

  static void register() {
    drops = ParticleSprites.of(GoreParticles.DROP);
    splats = ParticleSprites.of(GoreParticles.SPLAT);
  }

  static void spawnDrop(
      final MinecraftClient client, final Vec3d at, final Vec3d velocity, final SplatterLook look) {
    if (client.world != null && drops != null) {
      drops
          .get()
          .ifPresent(
              sprites ->
                  client.particleManager.addParticle(
                      new BloodDropParticle(client.world, at, velocity, sprites, look)));
    }
  }

  static void spawnSplat(
      final MinecraftClient client,
      final Vec3d contact,
      final Direction face,
      final SplatterLook look,
      final boolean pool) {
    if (client.world != null && splats != null) {
      splats
          .get()
          .ifPresent(
              sprites ->
                  FlatDecalParticle.place(
                      client.particleManager,
                      new BloodSplatParticle(client.world, contact, face, sprites, look, pool)));
    }
  }
}
