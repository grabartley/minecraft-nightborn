package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.slayer.DownBlow;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;

public record BoostPower(
    int durationTicks,
    List<SkillAttribute> attributes,
    int killTicks,
    int mostTicks,
    Optional<Float> rescueBelow,
    Optional<DownBlow> downs) {

  public static final Codec<BoostPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.intRange(1, 6000)
                          .fieldOf("duration_ticks")
                          .forGetter(BoostPower::durationTicks),
                      SkillAttribute.CODEC
                          .listOf()
                          .fieldOf("attributes")
                          .forGetter(BoostPower::attributes),
                      Codec.intRange(0, 6000)
                          .optionalFieldOf("kill_ticks", 0)
                          .forGetter(BoostPower::killTicks),
                      Codec.intRange(1, 12000)
                          .optionalFieldOf("most_ticks", 6000)
                          .forGetter(BoostPower::mostTicks),
                      Codec.floatRange(0.0F, 1.0F)
                          .optionalFieldOf("rescue_below")
                          .forGetter(BoostPower::rescueBelow),
                      DownBlow.CODEC.optionalFieldOf("downs").forGetter(BoostPower::downs))
                  .apply(instance, BoostPower::new));

  public BoostPower {
    attributes = List.copyOf(attributes);
  }
}
