package com.grahambartley.nightborn.raid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record NoiseKind(float loudness, int reach) {

  public static final int FARTHEST = 64;
  public static final float LOUDEST = 1000.0F;

  public static final Codec<NoiseKind> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(0.0F, LOUDEST)
                          .fieldOf("loudness")
                          .forGetter(NoiseKind::loudness),
                      Codec.intRange(1, FARTHEST).fieldOf("reach").forGetter(NoiseKind::reach))
                  .apply(instance, NoiseKind::new));

  public boolean silent() {
    return loudness <= 0.0F;
  }
}
