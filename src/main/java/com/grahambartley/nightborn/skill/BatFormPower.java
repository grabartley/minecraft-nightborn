package com.grahambartley.nightborn.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BatFormPower(float landRange) {

  public static final Codec<BatFormPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(1.0F, MobPowerUse.MAX_RANGE)
                          .fieldOf("land_range")
                          .forGetter(BatFormPower::landRange))
                  .apply(instance, BatFormPower::new));

  public boolean landsAt(final double distance) {
    return distance <= landRange;
  }
}
