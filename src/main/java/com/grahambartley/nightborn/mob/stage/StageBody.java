package com.grahambartley.nightborn.mob.stage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record StageBody(double health, double speed, double damage) {

  public static final StageBody SAME = new StageBody(0.0, 0.0, 0.0);

  static final double MIN = -0.9;
  static final double MAX = 30.0;

  public static final Codec<StageBody> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.doubleRange(MIN, MAX)
                          .optionalFieldOf("health", 0.0)
                          .forGetter(StageBody::health),
                      Codec.doubleRange(MIN, MAX)
                          .optionalFieldOf("speed", 0.0)
                          .forGetter(StageBody::speed),
                      Codec.doubleRange(MIN, MAX)
                          .optionalFieldOf("damage", 0.0)
                          .forGetter(StageBody::damage))
                  .apply(instance, StageBody::new));
}
