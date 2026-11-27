package com.grahambartley.nightborn.mob.brain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Consideration(String fact, Curve curve, float low, float high) {

  public static final Codec<Consideration> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.STRING.fieldOf("fact").forGetter(Consideration::fact),
                      Curve.CODEC
                          .optionalFieldOf("curve", Curve.BETWEEN)
                          .forGetter(Consideration::curve),
                      Codec.FLOAT.optionalFieldOf("low", 0.0F).forGetter(Consideration::low),
                      Codec.FLOAT.fieldOf("high").forGetter(Consideration::high))
                  .apply(instance, Consideration::new));

  public double value(final Situation situation) {
    return Fact.byKey(fact).map(known -> curve.value(known.read(situation), low, high)).orElse(0.0);
  }
}
