package com.grahambartley.nightborn.faith;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record RebukePower(float range, float arcDegrees, float push, int silenceTicks) {

  public static final Codec<RebukePower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(1.0F, 16.0F).fieldOf("range").forGetter(RebukePower::range),
                      Codec.floatRange(10.0F, 360.0F)
                          .fieldOf("arc_degrees")
                          .forGetter(RebukePower::arcDegrees),
                      Codec.floatRange(0.0F, 5.0F).fieldOf("push").forGetter(RebukePower::push),
                      Codec.intRange(0, 400)
                          .fieldOf("silence_ticks")
                          .forGetter(RebukePower::silenceTicks))
                  .apply(instance, RebukePower::new));
}
