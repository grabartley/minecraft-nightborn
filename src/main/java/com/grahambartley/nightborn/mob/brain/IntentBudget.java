package com.grahambartley.nightborn.mob.brain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record IntentBudget(int bloodReserve, int cooldownPrice, float threshold) {

  public static final IntentBudget FREE = new IntentBudget(0, 0, 0.0F);

  public static final Codec<IntentBudget> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.intRange(0, 20)
                          .optionalFieldOf("blood_reserve", 0)
                          .forGetter(IntentBudget::bloodReserve),
                      Codec.intRange(0, 1200)
                          .optionalFieldOf("cooldown_price", 0)
                          .forGetter(IntentBudget::cooldownPrice),
                      Codec.floatRange(0.0F, MobUse.MAX_WEIGHT)
                          .optionalFieldOf("threshold", 0.0F)
                          .forGetter(IntentBudget::threshold))
                  .apply(instance, IntentBudget::new));

  public int floor(final int mobFloor) {
    return Math.max(mobFloor, bloodReserve);
  }

  public boolean affords(final int blood, final int cost, final int mobFloor) {
    return blood - cost >= floor(mobFloor);
  }

  public boolean passes(final double score) {
    return score > threshold;
  }
}
