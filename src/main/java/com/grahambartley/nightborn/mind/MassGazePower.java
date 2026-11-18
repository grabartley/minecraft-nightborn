package com.grahambartley.nightborn.mind;

import com.grahambartley.nightborn.skill.MobPowerUse;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MassGazePower(
    float radius, int durationTicks, int playerDurationTicks, int playerSlowness) {

  public static final Codec<MassGazePower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(1.0F, MobPowerUse.MAX_RANGE)
                          .fieldOf("radius")
                          .forGetter(MassGazePower::radius),
                      Codec.intRange(1, 6000)
                          .fieldOf("duration_ticks")
                          .forGetter(MassGazePower::durationTicks),
                      Codec.intRange(1, 6000)
                          .fieldOf("player_duration_ticks")
                          .forGetter(MassGazePower::playerDurationTicks),
                      Codec.intRange(0, 6)
                          .fieldOf("player_slowness")
                          .forGetter(MassGazePower::playerSlowness))
                  .apply(instance, MassGazePower::new));

  public int durationFor(final boolean player) {
    return player ? playerDurationTicks : durationTicks;
  }
}
