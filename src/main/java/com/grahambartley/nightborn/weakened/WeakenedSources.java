package com.grahambartley.nightborn.weakened;

import com.grahambartley.nightborn.vampire.VisibleState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.Map;

public record WeakenedSources(Map<String, Source> sources) {

  public static final int CAP = VisibleState.WEAKENED_CAP;

  public static final WeakenedSources NONE = new WeakenedSources(Map.of());

  public record Source(int strength, int ticksLeft) {
    public static final Codec<Source> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        Codec.intRange(0, CAP).fieldOf("strength").forGetter(Source::strength),
                        Codec.intRange(0, Integer.MAX_VALUE)
                            .fieldOf("ticksLeft")
                            .forGetter(Source::ticksLeft))
                    .apply(instance, Source::new));
  }

  public static final Codec<WeakenedSources> CODEC =
      Codec.unboundedMap(Codec.STRING, Source.CODEC)
          .xmap(WeakenedSources::new, WeakenedSources::sources);

  public WeakenedSources {
    sources = Map.copyOf(sources);
  }

  public WeakenedSources with(final String id, final int strength, final int ticks) {
    final int clampedStrength = clampStrength(strength);
    if (clampedStrength == 0 || ticks <= 0) {
      return this;
    }
    final Map<String, Source> updated = new LinkedHashMap<>(sources);
    final Source current = updated.get(id);
    updated.put(
        id,
        current == null
            ? new Source(clampedStrength, ticks)
            : new Source(
                Math.max(current.strength(), clampedStrength),
                Math.max(current.ticksLeft(), ticks)));
    return new WeakenedSources(updated);
  }

  public WeakenedSources replacing(final String id, final int strength, final int ticks) {
    final int clampedStrength = clampStrength(strength);
    final Map<String, Source> updated = new LinkedHashMap<>(sources);
    if (clampedStrength == 0 || ticks <= 0) {
      if (updated.remove(id) == null) {
        return this;
      }
    } else {
      updated.put(id, new Source(clampedStrength, ticks));
    }
    return new WeakenedSources(updated);
  }

  public int strength(final String id) {
    final Source source = sources.get(id);
    return source == null ? 0 : source.strength();
  }

  public static int clampStrength(final long strength) {
    return (int) Math.max(0, Math.min(CAP, strength));
  }

  public WeakenedSources tick() {
    if (sources.isEmpty()) {
      return this;
    }
    final Map<String, Source> updated = new LinkedHashMap<>();
    sources.forEach(
        (id, source) -> {
          if (source.ticksLeft() > 1) {
            updated.put(id, new Source(source.strength(), source.ticksLeft() - 1));
          }
        });
    return new WeakenedSources(updated);
  }

  public int level(final boolean atCap) {
    if (atCap) {
      return CAP;
    }
    long total = 0;
    for (final Source source : sources.values()) {
      total += source.strength();
    }
    return (int) Math.min(CAP, total);
  }

  public boolean isEmpty() {
    return sources.isEmpty();
  }
}
