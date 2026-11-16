package com.grahambartley.nightborn.occult;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FamiliarPower(
    int lifetimeTicks,
    float health,
    float damage,
    float scoutRange,
    int scoutTicks,
    float markRadius,
    int markTicks,
    int carrySlots) {

  public static final Codec<FamiliarPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.intRange(20, 72000)
                          .fieldOf("lifetime_ticks")
                          .forGetter(FamiliarPower::lifetimeTicks),
                      Codec.floatRange(1.0F, 100.0F)
                          .fieldOf("health")
                          .forGetter(FamiliarPower::health),
                      Codec.floatRange(0.0F, 40.0F)
                          .fieldOf("damage")
                          .forGetter(FamiliarPower::damage),
                      Codec.floatRange(1.0F, 64.0F)
                          .fieldOf("scout_range")
                          .forGetter(FamiliarPower::scoutRange),
                      Codec.intRange(20, 1200)
                          .fieldOf("scout_ticks")
                          .forGetter(FamiliarPower::scoutTicks),
                      Codec.floatRange(1.0F, 32.0F)
                          .fieldOf("mark_radius")
                          .forGetter(FamiliarPower::markRadius),
                      Codec.intRange(1, 1200)
                          .fieldOf("mark_ticks")
                          .forGetter(FamiliarPower::markTicks),
                      Codec.intRange(0, 27)
                          .fieldOf("carry_slots")
                          .forGetter(FamiliarPower::carrySlots))
                  .apply(instance, FamiliarPower::new));
}
