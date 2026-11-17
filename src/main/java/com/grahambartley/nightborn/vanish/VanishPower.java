package com.grahambartley.nightborn.vanish;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record VanishPower(float distance, int goneTicks) {

  public static final Codec<VanishPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(2.0F, 8.0F)
                          .fieldOf("distance")
                          .forGetter(VanishPower::distance),
                      Codec.intRange(1, 20).fieldOf("gone_ticks").forGetter(VanishPower::goneTicks))
                  .apply(instance, VanishPower::new));
}
