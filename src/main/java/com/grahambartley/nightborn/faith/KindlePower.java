package com.grahambartley.nightborn.faith;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record KindlePower(float cost, int durationTicks, float damage, int burnTicks) {

  public static final Codec<KindlePower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(0.0F, 20.0F)
                          .fieldOf("health_cost")
                          .forGetter(KindlePower::cost),
                      Codec.intRange(20, 72000)
                          .fieldOf("duration_ticks")
                          .forGetter(KindlePower::durationTicks),
                      Codec.floatRange(0.0F, 20.0F)
                          .fieldOf("damage")
                          .forGetter(KindlePower::damage),
                      Codec.intRange(0, 400)
                          .fieldOf("burn_ticks")
                          .forGetter(KindlePower::burnTicks))
                  .apply(instance, KindlePower::new));

  public Flame flame(final long now) {
    return new Flame(now + durationTicks, damage, burnTicks);
  }
}
