package com.grahambartley.nightborn.mob.brain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record IntentBudget(
    int bloodReserve,
    int cooldownPrice,
    float threshold,
    float thrift,
    float spread,
    int thinkScale) {

  public static final IntentBudget FREE = new IntentBudget(0, 0, 0.0F, 0.0F, 0.0F);

  static final int MAX_THINK_SCALE = 20;

  static final double FULL_BLOOD_PRICE = 10.0;
  static final double FULL_COOLDOWN_PRICE = 6000.0;

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
                          .forGetter(IntentBudget::threshold),
                      Codec.floatRange(0.0F, 1.0F)
                          .optionalFieldOf("thrift", 0.0F)
                          .forGetter(IntentBudget::thrift),
                      Codec.floatRange(0.0F, 1.0F)
                          .optionalFieldOf("spread", 0.0F)
                          .forGetter(IntentBudget::spread),
                      Codec.intRange(1, MAX_THINK_SCALE)
                          .optionalFieldOf("think_scale", 1)
                          .forGetter(IntentBudget::thinkScale))
                  .apply(instance, IntentBudget::new));

  public IntentBudget(final int bloodReserve, final int cooldownPrice, final float threshold) {
    this(bloodReserve, cooldownPrice, threshold, 0.0F, 0.0F);
  }

  public IntentBudget(
      final int bloodReserve,
      final int cooldownPrice,
      final float threshold,
      final float thrift,
      final float spread) {
    this(bloodReserve, cooldownPrice, threshold, thrift, spread, 1);
  }

  public int floor(final int mobFloor) {
    return Math.max(mobFloor, bloodReserve);
  }

  public boolean affords(final int blood, final int cost, final int mobFloor) {
    return blood - cost >= (cost == 0 ? mobFloor : floor(mobFloor));
  }

  public boolean passes(final double score) {
    return score > threshold;
  }

  public double kept(final int blood, final int cooldownTicks, final float ownHealth) {
    final double price =
        Math.clamp(blood / FULL_BLOOD_PRICE + cooldownTicks / FULL_COOLDOWN_PRICE, 0.0, 1.0);
    return 1.0 - thrift * price * Math.clamp(ownHealth, 0.0F, 1.0F);
  }

  public int thinkTicks(final int fightThinkTicks) {
    return fightThinkTicks * thinkScale;
  }

  public boolean close(final double score, final double best) {
    return score >= best * (1.0 - spread);
  }
}
