package com.grahambartley.nightborn.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record HealPower(float health, int ticks, boolean closesWounds) {

  public static final Codec<HealPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(0.0F, 1024.0F)
                          .fieldOf("health")
                          .forGetter(HealPower::health),
                      Codec.intRange(1, 72000).fieldOf("ticks").forGetter(HealPower::ticks),
                      Codec.BOOL
                          .optionalFieldOf("closes_wounds", false)
                          .forGetter(HealPower::closesWounds))
                  .apply(instance, HealPower::new));

  public Mend mend() {
    return new Mend(health / ticks, ticks);
  }
}
