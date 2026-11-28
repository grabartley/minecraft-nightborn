package com.grahambartley.nightborn.mob.stage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record StageBody(double health, double speed, double damage) {

  public static final StageBody SAME = new StageBody(0.0, 0.0, 0.0);

  static final double LEAST_HEALTH = -19.0;
  static final double MOST_HEALTH = 1000.0;
  static final double LEAST_SPEED = -0.9;
  static final double MOST_SPEED = 5.0;
  static final double LEAST_DAMAGE = -10.0;
  static final double MOST_DAMAGE = 100.0;

  public static final Codec<StageBody> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.doubleRange(LEAST_HEALTH, MOST_HEALTH)
                          .optionalFieldOf("health", 0.0)
                          .forGetter(StageBody::health),
                      Codec.doubleRange(LEAST_SPEED, MOST_SPEED)
                          .optionalFieldOf("speed", 0.0)
                          .forGetter(StageBody::speed),
                      Codec.doubleRange(LEAST_DAMAGE, MOST_DAMAGE)
                          .optionalFieldOf("damage", 0.0)
                          .forGetter(StageBody::damage))
                  .apply(instance, StageBody::new));

  public StageBody underHealthMultiplier(final double multiplier) {
    return new StageBody(health / (1.0 + Math.max(0.0, multiplier)), speed, damage);
  }
}
