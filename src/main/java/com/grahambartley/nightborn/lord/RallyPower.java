package com.grahambartley.nightborn.lord;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record RallyPower(float range) {

  public static final Codec<RallyPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(1.0F, 48.0F).fieldOf("range").forGetter(RallyPower::range))
                  .apply(instance, RallyPower::new));

  public boolean answers(
      final boolean caster,
      final boolean vampireMob,
      final boolean sameHome,
      final boolean helpless,
      final double distance) {
    return !caster && vampireMob && sameHome && !helpless && distance <= range;
  }
}
