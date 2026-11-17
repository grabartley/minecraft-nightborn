package com.grahambartley.nightborn.age;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record NightWatch(long night, int ticks, boolean counted) {

  public static final long NO_NIGHT = Long.MIN_VALUE;

  public static final NightWatch NONE = new NightWatch(NO_NIGHT, 0, false);

  public static final Codec<NightWatch> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.LONG.fieldOf("night").forGetter(NightWatch::night),
                      Codec.INT.optionalFieldOf("ticks", 0).forGetter(NightWatch::ticks),
                      Codec.BOOL.optionalFieldOf("counted", false).forGetter(NightWatch::counted))
                  .apply(instance, NightWatch::new));

  public NightWatch {
    ticks = Math.max(0, ticks);
  }
}
