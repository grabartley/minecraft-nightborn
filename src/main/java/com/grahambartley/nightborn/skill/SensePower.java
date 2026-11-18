package com.grahambartley.nightborn.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SensePower(float range, int durationTicks) {

  public static final Codec<SensePower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(1.0F, MobPowerUse.MAX_RANGE)
                          .fieldOf("range")
                          .forGetter(SensePower::range),
                      Codec.intRange(1, 6000)
                          .fieldOf("duration_ticks")
                          .forGetter(SensePower::durationTicks))
                  .apply(instance, SensePower::new));
}
