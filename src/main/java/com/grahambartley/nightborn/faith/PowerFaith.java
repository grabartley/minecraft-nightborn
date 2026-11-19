package com.grahambartley.nightborn.faith;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.stream.Stream;

public record PowerFaith(
    Optional<PrayerPower> prayer,
    Optional<RebukePower> rebuke,
    Optional<BlessingPower> blessing,
    Optional<KindlePower> kindle) {

  public static final PowerFaith NONE =
      new PowerFaith(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

  public static final MapCodec<PowerFaith> CODEC =
      RecordCodecBuilder.<PowerFaith>mapCodec(
              instance ->
                  instance
                      .group(
                          PrayerPower.CODEC.optionalFieldOf("prayer").forGetter(PowerFaith::prayer),
                          RebukePower.CODEC.optionalFieldOf("rebuke").forGetter(PowerFaith::rebuke),
                          BlessingPower.CODEC
                              .optionalFieldOf("blessing")
                              .forGetter(PowerFaith::blessing),
                          KindlePower.CODEC
                              .optionalFieldOf("blood_fire")
                              .forGetter(PowerFaith::kindle))
                      .apply(instance, PowerFaith::new))
          .validate(PowerFaith::single);

  public int count() {
    return (int) Stream.of(prayer, rebuke, blessing, kindle).filter(Optional::isPresent).count();
  }

  public boolean any() {
    return prayer.isPresent() || rebuke.isPresent() || blessing.isPresent() || kindle.isPresent();
  }

  public Optional<Float> spotRange() {
    return prayer.flatMap(PrayerPower::spotRange);
  }

  public boolean armsAWeapon() {
    return blessing.isPresent() || kindle.isPresent();
  }

  private static DataResult<PowerFaith> single(final PowerFaith faith) {
    return faith.count() > 1
        ? DataResult.error(() -> "A power prays, rebukes, blesses or lights a weapon, one only")
        : DataResult.success(faith);
  }
}
