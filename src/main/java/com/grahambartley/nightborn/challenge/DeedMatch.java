package com.grahambartley.nightborn.challenge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.util.Identifier;

public record DeedMatch(Identifier deed, Map<String, String> facts, Map<String, Integer> atLeast) {

  public static final MapCodec<DeedMatch> MAP_CODEC =
      RecordCodecBuilder.mapCodec(
          instance ->
              instance
                  .group(
                      Identifier.CODEC.fieldOf("deed").forGetter(DeedMatch::deed),
                      Codec.unboundedMap(Codec.STRING, Codec.STRING)
                          .optionalFieldOf("facts", Map.of())
                          .forGetter(DeedMatch::facts),
                      Codec.unboundedMap(Codec.STRING, Codec.INT)
                          .optionalFieldOf("at_least", Map.of())
                          .forGetter(DeedMatch::atLeast))
                  .apply(instance, DeedMatch::new));

  public static final Codec<DeedMatch> CODEC = MAP_CODEC.codec();

  public DeedMatch {
    facts = Map.copyOf(facts);
    atLeast = Map.copyOf(atLeast);
  }

  public static DeedMatch of(final Identifier deed) {
    return new DeedMatch(deed, Map.of(), Map.of());
  }

  public boolean matches(final Deed done) {
    return deed.equals(done.kind())
        && facts.entrySet().stream()
            .allMatch(fact -> done.fact(fact.getKey()).filter(fact.getValue()::equals).isPresent())
        && atLeast.entrySet().stream()
            .allMatch(
                least -> {
                  final var value = done.number(least.getKey());
                  return value.isPresent() && value.getAsInt() >= least.getValue();
                });
  }
}
