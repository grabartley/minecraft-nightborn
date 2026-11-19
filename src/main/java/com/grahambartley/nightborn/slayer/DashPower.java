package com.grahambartley.nightborn.slayer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DashPower(
    float distance, int untouchableTicks, int dodgeTicks, int staggerTicks, float threatRange) {

  public static final Codec<DashPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(1.0F, 8.0F)
                          .fieldOf("distance")
                          .forGetter(DashPower::distance),
                      Codec.intRange(0, 40)
                          .fieldOf("untouchable_ticks")
                          .forGetter(DashPower::untouchableTicks),
                      Codec.intRange(0, 40).fieldOf("dodge_ticks").forGetter(DashPower::dodgeTicks),
                      Codec.intRange(1, 200)
                          .fieldOf("stagger_ticks")
                          .forGetter(DashPower::staggerTicks),
                      Codec.floatRange(1.0F, 16.0F)
                          .fieldOf("threat_range")
                          .forGetter(DashPower::threatRange))
                  .apply(instance, DashPower::new));
}
