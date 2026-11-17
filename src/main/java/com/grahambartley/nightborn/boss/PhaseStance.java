package com.grahambartley.nightborn.boss;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record PhaseStance(float near, float far, double speed, int strafeTicks) {

  public static final PhaseStance DEFAULT = new PhaseStance(2.0F, 5.0F, 1.0, 40);

  static final float SIDESTEP = 0.5F;

  public static final Codec<PhaseStance> CODEC =
      RecordCodecBuilder.<PhaseStance>create(
              instance ->
                  instance
                      .group(
                          Codec.floatRange(0.0F, 32.0F)
                              .optionalFieldOf("near", DEFAULT.near)
                              .forGetter(PhaseStance::near),
                          Codec.floatRange(0.0F, 32.0F)
                              .optionalFieldOf("far", DEFAULT.far)
                              .forGetter(PhaseStance::far),
                          Codec.doubleRange(0.1, 3.0)
                              .optionalFieldOf("speed", DEFAULT.speed)
                              .forGetter(PhaseStance::speed),
                          Codec.intRange(1, 1200)
                              .optionalFieldOf("strafe_ticks", DEFAULT.strafeTicks)
                              .forGetter(PhaseStance::strafeTicks))
                      .apply(instance, PhaseStance::new))
          .validate(
              stance ->
                  stance.near <= stance.far
                      ? DataResult.success(stance)
                      : DataResult.error(() -> "A stance needs near <= far"));

  public enum Move {
    CLOSE,
    BACK,
    CIRCLE
  }

  public Move move(final double distance) {
    if (distance > far) {
      return Move.CLOSE;
    }
    return distance < near ? Move.BACK : Move.CIRCLE;
  }

  public float side(final int age) {
    return (age / strafeTicks) % 2 == 0 ? SIDESTEP : 0.0F - SIDESTEP;
  }
}
