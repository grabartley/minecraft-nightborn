package com.grahambartley.nightborn.mob.combat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

public record Presence(float radius, Identifier effect, int amplifier) {

  public static final float MOST_RADIUS = 32.0F;
  public static final int MOST_AMPLIFIER = 4;
  public static final int PULSE_TICKS = 20;
  public static final int LINGER_TICKS = 50;

  public static final Codec<Presence> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(1.0F, MOST_RADIUS)
                          .fieldOf("radius")
                          .forGetter(Presence::radius),
                      Identifier.CODEC.fieldOf("effect").forGetter(Presence::effect),
                      Codec.intRange(0, MOST_AMPLIFIER)
                          .optionalFieldOf("amplifier", 0)
                          .forGetter(Presence::amplifier))
                  .apply(instance, Presence::new));

  public static boolean pulses(final long age) {
    return age % PULSE_TICKS == 0;
  }

  public boolean reaches(
      final boolean leaderFighting, final boolean allyFighting, final double distance) {
    return leaderFighting && allyFighting && distance <= radius;
  }
}
