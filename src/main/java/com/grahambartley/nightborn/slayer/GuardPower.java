package com.grahambartley.nightborn.slayer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record GuardPower(
    int guardTicks, float reach, int chargeTicks, float push, int staggerTicks) {

  public static final Codec<GuardPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.intRange(1, 100)
                          .fieldOf("guard_ticks")
                          .forGetter(GuardPower::guardTicks),
                      Codec.floatRange(0.5F, 8.0F).fieldOf("reach").forGetter(GuardPower::reach),
                      Codec.intRange(1, 100)
                          .fieldOf("charge_ticks")
                          .forGetter(GuardPower::chargeTicks),
                      Codec.floatRange(0.0F, 8.0F).fieldOf("push").forGetter(GuardPower::push),
                      Codec.intRange(1, 200)
                          .fieldOf("stagger_ticks")
                          .forGetter(GuardPower::staggerTicks))
                  .apply(instance, GuardPower::new));
}
