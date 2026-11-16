package com.grahambartley.nightborn.occult;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ShadowStepPower(float range, float behind) {

  public static final Codec<ShadowStepPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(2.0F, 32.0F)
                          .fieldOf("range")
                          .forGetter(ShadowStepPower::range),
                      Codec.floatRange(0.0F, 4.0F)
                          .fieldOf("behind")
                          .forGetter(ShadowStepPower::behind))
                  .apply(instance, ShadowStepPower::new));
}
