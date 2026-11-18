package com.grahambartley.nightborn.mind;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record GlamourPower(int durationTicks) {

  public static final Codec<GlamourPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.intRange(1, 12000)
                          .fieldOf("duration_ticks")
                          .forGetter(GlamourPower::durationTicks))
                  .apply(instance, GlamourPower::new));
}
