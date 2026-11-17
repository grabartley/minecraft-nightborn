package com.grahambartley.nightborn.boss;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;

public record PhaseAmbience(int minTicks, int maxTicks, List<Cue> cues) {

  public record Cue(
      Optional<Identifier> sound, int weight, float volume, float pitch, boolean lightning) {

    public static final Codec<Cue> CODEC =
        RecordCodecBuilder.<Cue>create(
                instance ->
                    instance
                        .group(
                            Identifier.CODEC.optionalFieldOf("sound").forGetter(Cue::sound),
                            Codec.intRange(1, 100)
                                .optionalFieldOf("weight", 1)
                                .forGetter(Cue::weight),
                            Codec.floatRange(0.0F, 4.0F)
                                .optionalFieldOf("volume", 1.0F)
                                .forGetter(Cue::volume),
                            Codec.floatRange(0.5F, 2.0F)
                                .optionalFieldOf("pitch", 1.0F)
                                .forGetter(Cue::pitch),
                            Codec.BOOL
                                .optionalFieldOf("lightning", false)
                                .forGetter(Cue::lightning))
                        .apply(instance, Cue::new))
            .validate(
                cue ->
                    cue.sound.isPresent() || cue.lightning
                        ? DataResult.success(cue)
                        : DataResult.error(() -> "An ambience cue needs a sound or lightning"));
  }

  public static final Codec<PhaseAmbience> CODEC =
      RecordCodecBuilder.<PhaseAmbience>create(
              instance ->
                  instance
                      .group(
                          Codec.intRange(20, 72000)
                              .fieldOf("min_ticks")
                              .forGetter(PhaseAmbience::minTicks),
                          Codec.intRange(20, 72000)
                              .fieldOf("max_ticks")
                              .forGetter(PhaseAmbience::maxTicks),
                          Cue.CODEC.listOf(1, 32).fieldOf("cues").forGetter(PhaseAmbience::cues))
                      .apply(instance, PhaseAmbience::new))
          .validate(
              ambience ->
                  ambience.minTicks <= ambience.maxTicks
                      ? DataResult.success(ambience)
                      : DataResult.error(() -> "Ambience needs min_ticks <= max_ticks"));

  public PhaseAmbience {
    cues = List.copyOf(cues);
  }

  public int spread() {
    return maxTicks - minTicks + 1;
  }

  public long nextAt(final long now, final int roll) {
    return now + minTicks + Math.floorMod(roll, spread());
  }

  public int totalWeight() {
    return cues.stream().mapToInt(Cue::weight).sum();
  }

  public Cue pick(final int roll) {
    int left = Math.floorMod(roll, totalWeight());
    for (final Cue cue : cues) {
      left -= cue.weight();
      if (left < 0) {
        return cue;
      }
    }
    return cues.get(cues.size() - 1);
  }
}
