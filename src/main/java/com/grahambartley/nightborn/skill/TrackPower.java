package com.grahambartley.nightborn.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record TrackPower(int range, int durationTicks) {

  public static final Codec<TrackPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.intRange(1, 256).fieldOf("range").forGetter(TrackPower::range),
                      Codec.intRange(1, 6000)
                          .fieldOf("duration_ticks")
                          .forGetter(TrackPower::durationTicks))
                  .apply(instance, TrackPower::new));
}
