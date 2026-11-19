package com.grahambartley.nightborn.seeker;

import com.grahambartley.nightborn.skill.MobPowerUse;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public record MarkPower(
    float range, int durationTicks, double damageShare, int colour, List<Veil> hiddenBy) {

  public static final Codec<MarkPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(1.0F, MobPowerUse.MAX_RANGE)
                          .fieldOf("range")
                          .forGetter(MarkPower::range),
                      Codec.intRange(20, 6000)
                          .fieldOf("duration_ticks")
                          .forGetter(MarkPower::durationTicks),
                      Codec.doubleRange(0.0, 1.0)
                          .optionalFieldOf("damage_share", 0.0)
                          .forGetter(MarkPower::damageShare),
                      Codec.intRange(0, 0xFFFFFF).fieldOf("colour").forGetter(MarkPower::colour),
                      Veil.CODEC
                          .listOf()
                          .optionalFieldOf("hidden_by", List.of())
                          .forGetter(MarkPower::hiddenBy))
                  .apply(instance, MarkPower::new));

  public MarkPower {
    hiddenBy = List.copyOf(hiddenBy);
  }
}
