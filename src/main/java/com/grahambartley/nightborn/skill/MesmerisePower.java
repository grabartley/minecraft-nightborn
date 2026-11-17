package com.grahambartley.nightborn.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MesmerisePower(
    float range, int stareTicks, int durationTicks, int playerDurationTicks, int playerSlowness) {

  public static final Codec<MesmerisePower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(1.0F, MobPowerUse.MAX_RANGE)
                          .fieldOf("range")
                          .forGetter(MesmerisePower::range),
                      Codec.intRange(1, 200)
                          .fieldOf("stare_ticks")
                          .forGetter(MesmerisePower::stareTicks),
                      Codec.intRange(1, 6000)
                          .fieldOf("duration_ticks")
                          .forGetter(MesmerisePower::durationTicks),
                      Codec.intRange(1, 6000)
                          .fieldOf("player_duration_ticks")
                          .forGetter(MesmerisePower::playerDurationTicks),
                      Codec.intRange(0, 6)
                          .fieldOf("player_slowness")
                          .forGetter(MesmerisePower::playerSlowness))
                  .apply(instance, MesmerisePower::new));

  public int durationFor(final boolean player) {
    return player ? playerDurationTicks : durationTicks;
  }
}
