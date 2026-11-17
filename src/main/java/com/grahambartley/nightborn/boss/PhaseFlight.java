package com.grahambartley.nightborn.boss;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.Vec3d;

public record PhaseFlight(float height, float radius, float speed, int groundTicks) {

  static final double MARGIN = 2.0;

  public static final Codec<PhaseFlight> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(1.0F, 32.0F)
                          .fieldOf("height")
                          .forGetter(PhaseFlight::height),
                      Codec.floatRange(0.0F, 48.0F)
                          .fieldOf("radius")
                          .forGetter(PhaseFlight::radius),
                      Codec.floatRange(0.05F, 1.5F)
                          .optionalFieldOf("speed", 0.3F)
                          .forGetter(PhaseFlight::speed),
                      Codec.intRange(0, 1200)
                          .optionalFieldOf("ground_ticks", 80)
                          .forGetter(PhaseFlight::groundTicks))
                  .apply(instance, PhaseFlight::new));

  public boolean airborne(
      final boolean fighting, final boolean held, final boolean diving, final long sinceLanding) {
    return fighting && !held && !diving && sinceLanding >= groundTicks;
  }

  public Vec3d spot(final Arena arena, final long time) {
    final double ring = Math.max(0.0, Math.min(radius, arena.radius() - MARGIN));
    final double lift = Math.max(1.0, Math.min(height, arena.above() - MARGIN));
    final double angle = ring <= 0.0 ? 0.0 : time * (double) speed / ring;
    return new Vec3d(
        arena.midX() + Math.cos(angle) * ring,
        arena.centre().getY() + lift,
        arena.midZ() + Math.sin(angle) * ring);
  }

  public Vec3d steer(final Vec3d from, final Vec3d spot) {
    final Vec3d gap = spot.subtract(from);
    final double fastest = speed * 2.0;
    return gap.length() <= fastest ? gap : gap.normalize().multiply(fastest);
  }
}
