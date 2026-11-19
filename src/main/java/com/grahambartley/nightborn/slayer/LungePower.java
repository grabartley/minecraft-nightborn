package com.grahambartley.nightborn.slayer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record LungePower(float distance, float stakeShare, DownBlow downs) {

  public static final Codec<LungePower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(1.0F, 16.0F)
                          .fieldOf("distance")
                          .forGetter(LungePower::distance),
                      Codec.floatRange(0.0F, 4.0F)
                          .fieldOf("stake_share")
                          .forGetter(LungePower::stakeShare),
                      DownBlow.CODEC.fieldOf("downs").forGetter(LungePower::downs))
                  .apply(instance, LungePower::new));
}
