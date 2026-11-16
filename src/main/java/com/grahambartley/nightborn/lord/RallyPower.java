package com.grahambartley.nightborn.lord;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record RallyPower(int range) {

  public static final Codec<RallyPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(Codec.intRange(1, 48).fieldOf("range").forGetter(RallyPower::range))
                  .apply(instance, RallyPower::new));
}
