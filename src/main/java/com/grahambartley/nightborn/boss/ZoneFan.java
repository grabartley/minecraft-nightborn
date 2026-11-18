package com.grahambartley.nightborn.boss;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ZoneFan(int count, float degrees) {

  public static final ZoneFan SINGLE = new ZoneFan(1, 0.0F);

  public static final Codec<ZoneFan> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.intRange(1, 9).fieldOf("count").forGetter(ZoneFan::count),
                      Codec.floatRange(0.0F, 90.0F)
                          .optionalFieldOf("degrees", 0.0F)
                          .forGetter(ZoneFan::degrees))
                  .apply(instance, ZoneFan::new));

  public double turn(final int index) {
    return count <= 1 ? 0.0 : Math.toRadians(degrees * (index - (count - 1) / 2.0));
  }
}
