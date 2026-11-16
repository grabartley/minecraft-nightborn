package com.grahambartley.nightborn.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;

public record BatFormPower(float landRange, List<String> forcedBy) {

  public static final Codec<BatFormPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(1.0F, MobPowerUse.MAX_RANGE)
                          .fieldOf("land_range")
                          .forGetter(BatFormPower::landRange),
                      Codec.STRING
                          .listOf()
                          .optionalFieldOf("forced_by", List.of())
                          .forGetter(BatFormPower::forcedBy))
                  .apply(instance, BatFormPower::new));

  public static boolean chases(
      final Optional<BatFormPower> form, final boolean targetAlive, final double distance) {
    return targetAlive && form.map(power -> !power.landsAt(distance)).orElse(false);
  }

  public boolean landsAt(final double distance) {
    return distance <= landRange;
  }
}
