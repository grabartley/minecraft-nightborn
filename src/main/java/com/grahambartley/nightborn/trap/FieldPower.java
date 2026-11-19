package com.grahambartley.nightborn.trap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.Identifier;

public record FieldPower(float radius, int durationTicks, List<Identifier> bars, Identifier look) {

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
                      Identifier.CODEC.fieldOf("look").forGetter(FieldPower::look))
                  .apply(instance, FieldPower::new));

  public FieldPower {
    bars = List.copyOf(bars);
  }
}
