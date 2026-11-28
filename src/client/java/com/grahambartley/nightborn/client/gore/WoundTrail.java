package com.grahambartley.nightborn.client.gore;

import com.grahambartley.nightborn.client.state.GoreLevel;
import com.grahambartley.nightborn.particle.DecalSurface;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

final class WoundTrail {

  static final int REACH = 1;

  private final Int2ObjectOpenHashMap<Vec3d> lastSplat = new Int2ObjectOpenHashMap<>();
  private final Int2ObjectOpenHashMap<Vec3d> seen = new Int2ObjectOpenHashMap<>();
  private ClientWorld world;
  private int laid;

  void begin(final ClientWorld current) {
    if (current != world) {
      lastSplat.clear();
      world = current;
    }
    seen.clear();
    laid = 0;
  }

  void follow(
      final MinecraftClient client,
      final LivingEntity living,
      final GoreLevel gore,
      final ParticlesMode particles) {
    final Vec3d last = lastSplat.get(living.getId());
    final boolean due =
        last == null || BleedTrail.due(gore, particles, last.squaredDistanceTo(living.getPos()));
    if (due && BleedTrail.affordable(laid)) {
      splat(client, living, gore, particles);
      laid++;
      seen.put(living.getId(), living.getPos());
    } else if (last != null) {
      seen.put(living.getId(), last);
    }
  }

  void end() {
    lastSplat.clear();
    lastSplat.putAll(seen);
  }

  void clear() {
    lastSplat.clear();
  }

  private void splat(
      final MinecraftClient client,
      final LivingEntity living,
      final GoreLevel gore,
      final ParticlesMode particles) {
    if (!BleedTrail.drawn(gore, particles)) {
      return;
    }
    final double spread = living.getWidth() / 2.0;
    final Vec3d spot =
        living
            .getPos()
            .add(
                (world.random.nextDouble() - 0.5) * spread,
                0.0,
                (world.random.nextDouble() - 0.5) * spread);
    DecalSurface.floorBelow(world, spot, REACH)
        .ifPresent(
            floor ->
                BloodParticleFactories.spawnSplat(
                    client, floor, Direction.UP, SplatterLook.of(gore), false));
  }
}
