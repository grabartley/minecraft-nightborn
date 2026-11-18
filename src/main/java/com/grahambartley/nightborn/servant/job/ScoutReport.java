package com.grahambartley.nightborn.servant.job;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;

public record ScoutReport(int villagers, int hunters, int vampires, int humans) {

  public static final Codec<ScoutReport> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.INT.fieldOf("villagers").forGetter(ScoutReport::villagers),
                      Codec.INT.fieldOf("hunters").forGetter(ScoutReport::hunters),
                      Codec.INT.fieldOf("vampires").forGetter(ScoutReport::vampires),
                      Codec.INT.fieldOf("humans").forGetter(ScoutReport::humans))
                  .apply(instance, ScoutReport::new));

  public static ScoutReport of(final Collection<ScoutSighting> sightings) {
    return new ScoutReport(
        count(sightings, ScoutSighting.VILLAGER),
        count(sightings, ScoutSighting.HUNTER),
        count(sightings, ScoutSighting.VAMPIRE),
        count(sightings, ScoutSighting.HUMAN));
  }

  private static int count(final Collection<ScoutSighting> sightings, final ScoutSighting kind) {
    return (int) sightings.stream().filter(kind::equals).count();
  }
}
