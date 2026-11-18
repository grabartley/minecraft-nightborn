package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.batform.WingedFormPower;
import com.grahambartley.nightborn.occult.MistPower;
import com.grahambartley.nightborn.occult.ShadowStepPower;
import com.grahambartley.nightborn.vanish.VanishPower;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.stream.Stream;

public record PowerMoves(
    Optional<BatFormPower> batForm,
    Optional<LeapPower> leap,
    Optional<ShadowStepPower> shadowStep,
    Optional<VanishPower> vanish,
    Optional<PouncePower> pounce,
    Optional<WingedFormPower> wingedForm,
    Optional<MistPower> mist) {

  public static final PowerMoves NONE =
      new PowerMoves(
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty());

  public static final MapCodec<PowerMoves> CODEC =
      RecordCodecBuilder.mapCodec(
          instance ->
              instance
                  .group(
                      BatFormPower.CODEC.optionalFieldOf("bat_form").forGetter(PowerMoves::batForm),
                      LeapPower.CODEC.optionalFieldOf("leap").forGetter(PowerMoves::leap),
                      ShadowStepPower.CODEC
                          .optionalFieldOf("shadow_step")
                          .forGetter(PowerMoves::shadowStep),
                      VanishPower.CODEC.optionalFieldOf("vanish").forGetter(PowerMoves::vanish),
                      PouncePower.CODEC.optionalFieldOf("pounce").forGetter(PowerMoves::pounce),
                      WingedFormPower.CODEC
                          .optionalFieldOf("winged_form")
                          .forGetter(PowerMoves::wingedForm),
                      MistPower.CODEC.optionalFieldOf("mist").forGetter(PowerMoves::mist))
                  .apply(instance, PowerMoves::new));

  public int count() {
    return (int)
        Stream.of(batForm, leap, shadowStep, vanish, pounce, wingedForm, mist)
            .filter(Optional::isPresent)
            .count();
  }
}
