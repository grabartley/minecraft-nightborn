package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.slayer.DownBlow;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;

public record Boost(
    long startedAt,
    long endsAt,
    int killTicks,
    int mostTicks,
    List<SkillAttribute> attributes,
    Optional<DownBlow> downs) {

  public static final Codec<Boost> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.LONG.fieldOf("started_at").forGetter(Boost::startedAt),
                      Codec.LONG.fieldOf("ends_at").forGetter(Boost::endsAt),
                      Codec.INT.optionalFieldOf("kill_ticks", 0).forGetter(Boost::killTicks),
                      Codec.INT.optionalFieldOf("most_ticks", 0).forGetter(Boost::mostTicks),
                      SkillAttribute.CODEC
                          .listOf()
                          .optionalFieldOf("attributes", List.of())
                          .forGetter(Boost::attributes),
                      DownBlow.CODEC.optionalFieldOf("downs").forGetter(Boost::downs))
                  .apply(instance, Boost::new));

  public Boost {
    attributes = List.copyOf(attributes);
  }

  public static Boost of(final long now, final int ticks, final List<SkillAttribute> attributes) {
    return new Boost(now, now + ticks, 0, ticks, attributes, Optional.empty());
  }

  public static Boost of(final long now, final BoostPower power) {
    return new Boost(
        now,
        now + power.durationTicks(),
        power.killTicks(),
        Math.max(power.durationTicks(), power.mostTicks()),
        power.attributes(),
        power.downs());
  }

  public boolean over(final long now) {
    return now >= endsAt;
  }

  public Boost onKill() {
    return killTicks <= 0
        ? this
        : new Boost(
            startedAt,
            Math.min(endsAt + killTicks, startedAt + mostTicks),
            killTicks,
            mostTicks,
            attributes,
            downs);
  }
}
