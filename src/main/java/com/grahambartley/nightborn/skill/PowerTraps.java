package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.trap.BaitPower;
import com.grahambartley.nightborn.trap.FieldPower;
import com.grahambartley.nightborn.trap.NetPower;
import com.grahambartley.nightborn.trap.TrapPower;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;

public record PowerTraps(
    Optional<TrapPower> trap,
    Optional<NetPower> net,
    Optional<BaitPower> bait,
    Optional<FieldPower> field) {

  public static final PowerTraps NONE =
      new PowerTraps(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

  public static final MapCodec<PowerTraps> CODEC =
      RecordCodecBuilder.mapCodec(
          instance ->
              instance
                  .group(
                      TrapPower.CODEC.optionalFieldOf("trap").forGetter(PowerTraps::trap),
                      NetPower.CODEC.optionalFieldOf("net").forGetter(PowerTraps::net),
                      BaitPower.CODEC.optionalFieldOf("bait").forGetter(PowerTraps::bait),
                      FieldPower.CODEC.optionalFieldOf("field").forGetter(PowerTraps::field))
                  .apply(instance, PowerTraps::new));

  public boolean places() {
    return trap.isPresent() || bait.isPresent();
  }

  public Optional<Float> placeRange() {
    return trap.map(TrapPower::range).or(() -> bait.map(BaitPower::range));
  }
}
