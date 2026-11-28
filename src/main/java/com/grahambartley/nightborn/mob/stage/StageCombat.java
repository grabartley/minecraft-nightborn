package com.grahambartley.nightborn.mob.stage;

import com.grahambartley.nightborn.mob.brain.Disposition;
import com.grahambartley.nightborn.mob.brain.Mind;
import com.grahambartley.nightborn.mob.brain.Sharpness;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record StageCombat(
    int bloodFloor,
    int thinkTicks,
    Sharpness sharpness,
    int leapChance,
    int dashChance,
    int dodgeChance,
    int powerDodgeChance,
    int retreatHealth,
    StageStuck stuck) {

  static final int PLAIN_FLOOR = 20;
  static final int PLAIN_THINK = 40;

  public static final StageCombat PLAIN =
      new StageCombat(PLAIN_FLOOR, PLAIN_THINK, Sharpness.KEEN, 0, 0, 0, 0, 0, StageStuck.PLAIN);

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
                      Sharpness.CODEC
                          .optionalFieldOf("sharpness", Sharpness.KEEN)
                          .forGetter(StageCombat::sharpness),
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
                          .forGetter(StageCombat::retreatHealth),
                      StageStuck.CODEC
                          .optionalFieldOf("stuck", StageStuck.PLAIN)
                          .forGetter(StageCombat::stuck))
                  .apply(instance, StageCombat::new));

  public Mind mind(final Disposition disposition) {
    return new Mind(thinkTicks, sharpness, disposition);
  }
}
