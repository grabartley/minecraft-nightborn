package com.grahambartley.nightborn.slayer;

import com.grahambartley.nightborn.skill.SkillAttribute;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public record StandGift(
    float below,
    int durationTicks,
    int cooldownTicks,
    boolean steadfast,
    List<SkillAttribute> attributes) {

  public static final Codec<StandGift> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(0.0F, 1.0F).fieldOf("below").forGetter(StandGift::below),
                      Codec.intRange(1, 6000)
                          .fieldOf("duration_ticks")
                          .forGetter(StandGift::durationTicks),
                      Codec.intRange(0, 72000)
                          .fieldOf("cooldown_ticks")
                          .forGetter(StandGift::cooldownTicks),
                      Codec.BOOL
                          .optionalFieldOf("steadfast", false)
                          .forGetter(StandGift::steadfast),
                      SkillAttribute.CODEC
                          .listOf()
                          .optionalFieldOf("attributes", List.of())
                          .forGetter(StandGift::attributes))
                  .apply(instance, StandGift::new));

  public StandGift {
    attributes = List.copyOf(attributes);
  }

  public boolean rises(final float health, final float maxHealth, final long cooldownLeft) {
    return cooldownLeft <= 0L && health > 0.0F && health < maxHealth * below;
  }
}
