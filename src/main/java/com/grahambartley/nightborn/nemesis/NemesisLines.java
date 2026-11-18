package com.grahambartley.nightborn.nemesis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.IntUnaryOperator;

public record NemesisLines(List<String> sighted, List<String> downed, List<String> fled) {

  public enum Moment {
    SIGHTED,
    DOWNED,
    FLED
  }

  public static final NemesisLines NONE = new NemesisLines(List.of(), List.of(), List.of());

  public static final Codec<NemesisLines> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.STRING
                          .listOf()
                          .optionalFieldOf("sighted", List.of())
                          .forGetter(NemesisLines::sighted),
                      Codec.STRING
                          .listOf()
                          .optionalFieldOf("downed", List.of())
                          .forGetter(NemesisLines::downed),
                      Codec.STRING
                          .listOf()
                          .optionalFieldOf("fled", List.of())
                          .forGetter(NemesisLines::fled))
                  .apply(instance, NemesisLines::new));

  public NemesisLines {
    sighted = List.copyOf(sighted);
    downed = List.copyOf(downed);
    fled = List.copyOf(fled);
  }

  public List<String> of(final Moment moment) {
    return switch (moment) {
      case SIGHTED -> sighted;
      case DOWNED -> downed;
      case FLED -> fled;
    };
  }

  public Optional<String> pick(final Moment moment, final IntUnaryOperator random) {
    final List<String> lines = of(moment);
    return lines.isEmpty()
        ? Optional.empty()
        : Optional.of(lines.get(Math.floorMod(random.applyAsInt(lines.size()), lines.size())));
  }
}
