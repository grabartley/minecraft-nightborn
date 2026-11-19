package com.grahambartley.nightborn.trap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;

public record FieldPower(
    float radius,
    int durationTicks,
    List<Identifier> bars,
    boolean rearmsTraps,
    Optional<FieldWeakening> weakens,
    boolean unwings,
    float height,
    Identifier look) {

  public static final Codec<FieldPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(1.0F, 32.0F).fieldOf("radius").forGetter(FieldPower::radius),
                      Codec.intRange(20, 6000)
                          .fieldOf("duration_ticks")
                          .forGetter(FieldPower::durationTicks),
                      Identifier.CODEC
                          .listOf()
                          .optionalFieldOf("bars", List.of())
                          .forGetter(FieldPower::bars),
                      Codec.BOOL
                          .optionalFieldOf("rearms_traps", false)
                          .forGetter(FieldPower::rearmsTraps),
                      FieldWeakening.CODEC
                          .optionalFieldOf("weakens")
                          .forGetter(FieldPower::weakens),
                      Codec.BOOL.optionalFieldOf("unwings", false).forGetter(FieldPower::unwings),
                      Codec.floatRange(1.0F, 64.0F)
                          .optionalFieldOf("height", 8.0F)
                          .forGetter(FieldPower::height),
                      Identifier.CODEC.fieldOf("look").forGetter(FieldPower::look))
                  .apply(instance, FieldPower::new));

  public FieldPower {
    bars = List.copyOf(bars);
  }

  public boolean pulses() {
    return weakens.isPresent() || unwings;
  }
}
