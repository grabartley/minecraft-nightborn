package com.grahambartley.nightborn.faith;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BlessingPower(float range, int durationTicks) {

  public static final Codec<BlessingPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(1.0F, 16.0F)
                          .fieldOf("range")
                          .forGetter(BlessingPower::range),
                      Codec.intRange(20, 72000)
                          .fieldOf("duration_ticks")
                          .forGetter(BlessingPower::durationTicks))
                  .apply(instance, BlessingPower::new));
}
