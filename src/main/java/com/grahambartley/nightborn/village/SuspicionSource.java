package com.grahambartley.nightborn.village;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SuspicionSource(int amount) {

  public static final int MOST = 100;

  public static final Codec<SuspicionSource> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.intRange(0, MOST).fieldOf("amount").forGetter(SuspicionSource::amount))
                  .apply(instance, SuspicionSource::new));
}
