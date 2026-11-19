package com.grahambartley.nightborn.slayer;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.stream.Stream;

public record PowerFight(
    Optional<GuardPower> riposte,
    Optional<StakeThrowPower> stakeThrow,
    Optional<QuickShotPower> quickShot) {

  public static final PowerFight NONE =
      new PowerFight(Optional.empty(), Optional.empty(), Optional.empty());

  public static final MapCodec<PowerFight> CODEC =
      RecordCodecBuilder.<PowerFight>mapCodec(
              instance ->
                  instance
                      .group(
                          GuardPower.CODEC
                              .optionalFieldOf("riposte")
                              .forGetter(PowerFight::riposte),
                          StakeThrowPower.CODEC
                              .optionalFieldOf("stake_throw")
                              .forGetter(PowerFight::stakeThrow),
                          QuickShotPower.CODEC
                              .optionalFieldOf("quick_shot")
                              .forGetter(PowerFight::quickShot))
                      .apply(instance, PowerFight::new))
          .validate(PowerFight::single);

  public int count() {
    return (int) Stream.of(riposte, stakeThrow, quickShot).filter(Optional::isPresent).count();
  }

  private static DataResult<PowerFight> single(final PowerFight fight) {
    return fight.count() > 1
        ? DataResult.error(() -> "A power guards, throws a stake or fires a crossbow, one only")
        : DataResult.success(fight);
  }
}
