package com.grahambartley.nightborn.occult;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MistPower(int durationTicks, float harm) {

  public static final Codec<MistPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.intRange(1, 6000)
                          .fieldOf("duration_ticks")
                          .forGetter(MistPower::durationTicks),
                      Codec.floatRange(1.0F, 10.0F).fieldOf("harm").forGetter(MistPower::harm))
                  .apply(instance, MistPower::new));
}
