package com.grahambartley.nightborn.mob.stage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record StageCombat(
    int bloodFloor,
    int thinkTicks,
    int leapChance,
    int dashChance,
    int dodgeChance,
    int powerDodgeChance,
    int retreatHealth) {

  static final int PLAIN_FLOOR = 20;
  static final int PLAIN_THINK = 40;

  public static final StageCombat PLAIN = new StageCombat(PLAIN_FLOOR, PLAIN_THINK, 0, 0, 0, 0, 0);

  public static final Codec<StageCombat> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.intRange(0, 20)
                          .optionalFieldOf("blood_floor", PLAIN_FLOOR)
                          .forGetter(StageCombat::bloodFloor),
                      Codec.intRange(1, 200)
                          .optionalFieldOf("think_ticks", PLAIN_THINK)
                          .forGetter(StageCombat::thinkTicks),
                      Codec.intRange(0, 100)
                          .optionalFieldOf("leap_chance", 0)
                          .forGetter(StageCombat::leapChance),
                      Codec.intRange(0, 100)
                          .optionalFieldOf("dash_chance", 0)
                          .forGetter(StageCombat::dashChance),
                      Codec.intRange(0, 100)
                          .optionalFieldOf("dodge_chance", 0)
                          .forGetter(StageCombat::dodgeChance),
                      Codec.intRange(0, 100)
                          .optionalFieldOf("power_dodge_chance", 0)
                          .forGetter(StageCombat::powerDodgeChance),
                      Codec.intRange(0, 100)
                          .optionalFieldOf("retreat_health", 0)
                          .forGetter(StageCombat::retreatHealth))
                  .apply(instance, StageCombat::new));
}
