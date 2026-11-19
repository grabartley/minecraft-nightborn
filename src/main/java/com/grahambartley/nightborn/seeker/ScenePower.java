package com.grahambartley.nightborn.seeker;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ScenePower(float range, float killRadius, int freshTicks) {

  public static final Codec<ScenePower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(1.0F, 16.0F).fieldOf("range").forGetter(ScenePower::range),
                      Codec.floatRange(0.5F, 16.0F)
                          .fieldOf("kill_radius")
                          .forGetter(ScenePower::killRadius),
                      Codec.intRange(20, Scenes.LONGEST_TICKS)
                          .fieldOf("fresh_ticks")
                          .forGetter(ScenePower::freshTicks))
                  .apply(instance, ScenePower::new));
}
