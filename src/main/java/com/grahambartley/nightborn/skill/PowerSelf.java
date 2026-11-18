package com.grahambartley.nightborn.skill;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;

public record PowerSelf(
    Optional<TrackPower> track,
    Optional<HealPower> heal,
    Optional<SensePower> sense,
    Optional<BoostPower> boost,
    Optional<FreePower> frees) {

  public static final PowerSelf NONE =
      new PowerSelf(
          Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

  public static final MapCodec<PowerSelf> CODEC =
      RecordCodecBuilder.mapCodec(
          instance ->
              instance
                  .group(
                      TrackPower.CODEC.optionalFieldOf("track").forGetter(PowerSelf::track),
                      HealPower.CODEC.optionalFieldOf("heal").forGetter(PowerSelf::heal),
                      SensePower.CODEC.optionalFieldOf("sense").forGetter(PowerSelf::sense),
                      BoostPower.CODEC.optionalFieldOf("boost").forGetter(PowerSelf::boost),
                      FreePower.CODEC.optionalFieldOf("frees").forGetter(PowerSelf::frees))
                  .apply(instance, PowerSelf::new));
}
