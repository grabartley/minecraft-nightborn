package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.trap.TrapGift;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public record SkillRank(
    int cost,
    List<SkillAttribute> attributes,
    double bareHandDamage,
    List<PowerGift> powerGifts,
    List<TrapGift> trapGifts) {

  public static final Codec<SkillRank> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.intRange(0, 100).fieldOf("cost").forGetter(SkillRank::cost),
                      SkillAttribute.CODEC
                          .listOf()
                          .optionalFieldOf("attributes", List.of())
                          .forGetter(SkillRank::attributes),
                      Codec.doubleRange(0.0, 20.0)
                          .optionalFieldOf("bare_hand_damage", 0.0)
                          .forGetter(SkillRank::bareHandDamage),
                      PowerGift.CODEC
                          .listOf()
                          .optionalFieldOf("power_gifts", List.of())
                          .forGetter(SkillRank::powerGifts),
                      TrapGift.CODEC
                          .listOf()
                          .optionalFieldOf("trap_gifts", List.of())
                          .forGetter(SkillRank::trapGifts))
                  .apply(instance, SkillRank::new));

  public SkillRank {
    attributes = List.copyOf(attributes);
    powerGifts = List.copyOf(powerGifts);
    trapGifts = List.copyOf(trapGifts);
  }

  public static SkillRank costing(final int cost) {
    return new SkillRank(cost, List.of(), 0.0, List.of(), List.of());
  }
}
