package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.lord.RallyPower;
import com.grahambartley.nightborn.lord.SummonPower;
import com.grahambartley.nightborn.occult.CirclePower;
import com.grahambartley.nightborn.occult.FamiliarPower;
import com.grahambartley.nightborn.occult.FogPower;
import com.grahambartley.nightborn.occult.SwarmPower;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;

public record PowerCalls(
    Optional<FamiliarPower> familiar,
    Optional<SummonPower> summon,
    Optional<RallyPower> rally,
    Optional<FogPower> fog,
    Optional<CirclePower> circle,
    Optional<SwarmPower> swarm) {

  public static final PowerCalls NONE =
      new PowerCalls(
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty());

  public static final MapCodec<PowerCalls> CODEC =
      RecordCodecBuilder.mapCodec(
          instance ->
              instance
                  .group(
                      FamiliarPower.CODEC
                          .optionalFieldOf("familiar")
                          .forGetter(PowerCalls::familiar),
                      SummonPower.CODEC.optionalFieldOf("summon").forGetter(PowerCalls::summon),
                      RallyPower.CODEC.optionalFieldOf("rally").forGetter(PowerCalls::rally),
                      FogPower.CODEC.optionalFieldOf("fog").forGetter(PowerCalls::fog),
                      CirclePower.CODEC.optionalFieldOf("circle").forGetter(PowerCalls::circle),
                      SwarmPower.CODEC.optionalFieldOf("swarm").forGetter(PowerCalls::swarm))
                  .apply(instance, PowerCalls::new));
}
