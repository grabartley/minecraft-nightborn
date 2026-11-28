package com.grahambartley.nightborn.mob.brain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Consideration(String fact, Curve curve, float low, float high, float radius) {

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
                      Codec.FLOAT.fieldOf("high").forGetter(Consideration::high),
                      Codec.floatRange(0.0F, GroupViews.VIEW_RADIUS)
                          .optionalFieldOf("radius", 0.0F)
                          .forGetter(Consideration::radius))
                  .apply(instance, Consideration::new));

  public Consideration(final String fact, final Curve curve, final float low, final float high) {
    this(fact, curve, low, high, 0.0F);
  }

  public double value(final Situation situation) {
    return Fact.byKey(fact)
        .map(known -> curve.value(known.read(situation, radius), low, high))
        .orElse(0.0);
  }

  public boolean readsAlly() {
    return Fact.byKey(fact).filter(Fact.ALLY::contains).isPresent();
  }
}
