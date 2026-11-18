package com.grahambartley.nightborn.occult;

import com.grahambartley.nightborn.skill.MobPowerUse;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SwarmPower(
    float radius, int durationTicks, float damage, int everyTicks, int recentTicks) {

  public static final Codec<SwarmPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(1.0F, MobPowerUse.MAX_RANGE)
                          .fieldOf("radius")
                          .forGetter(SwarmPower::radius),
                      Codec.intRange(1, 6000)
                          .fieldOf("duration_ticks")
                          .forGetter(SwarmPower::durationTicks),
                      Codec.floatRange(0.0F, 20.0F).fieldOf("damage").forGetter(SwarmPower::damage),
                      Codec.intRange(1, 200)
                          .fieldOf("every_ticks")
                          .forGetter(SwarmPower::everyTicks),
                      Codec.intRange(0, 6000)
                          .fieldOf("recent_ticks")
                          .forGetter(SwarmPower::recentTicks))
                  .apply(instance, SwarmPower::new));
}
