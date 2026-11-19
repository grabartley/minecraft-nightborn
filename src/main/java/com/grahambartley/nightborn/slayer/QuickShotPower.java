package com.grahambartley.nightborn.slayer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record QuickShotPower(float speed, float divergence) {

  public static final Codec<QuickShotPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(0.5F, 6.0F)
                          .fieldOf("speed")
                          .forGetter(QuickShotPower::speed),
                      Codec.floatRange(0.0F, 10.0F)
                          .fieldOf("divergence")
                          .forGetter(QuickShotPower::divergence))
                  .apply(instance, QuickShotPower::new));
}
