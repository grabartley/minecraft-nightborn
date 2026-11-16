package com.grahambartley.nightborn.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;

public record Skill(
    Identifier branch,
    int cost,
    List<Identifier> requires,
    Identifier icon,
    int column,
    int row,
    List<SkillAttribute> attributes,
    double bareHandDamage,
    Optional<SkillPower> power) {

  public static final int MAX_COLUMN = 2;
  public static final int MAX_ROW = 5;

  public static final Codec<Skill> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Identifier.CODEC.fieldOf("branch").forGetter(Skill::branch),
                      Codec.intRange(0, 100).fieldOf("cost").forGetter(Skill::cost),
                      Identifier.CODEC
                          .listOf()
                          .optionalFieldOf("requires", List.of())
                          .forGetter(Skill::requires),
                      Identifier.CODEC.fieldOf("icon").forGetter(Skill::icon),
                      Codec.intRange(0, MAX_COLUMN)
                          .optionalFieldOf("column", 0)
                          .forGetter(Skill::column),
                      Codec.intRange(0, MAX_ROW).optionalFieldOf("row", 0).forGetter(Skill::row),
                      SkillAttribute.CODEC
                          .listOf()
                          .optionalFieldOf("attributes", List.of())
                          .forGetter(Skill::attributes),
                      Codec.doubleRange(0.0, 20.0)
                          .optionalFieldOf("bare_hand_damage", 0.0)
                          .forGetter(Skill::bareHandDamage),
                      SkillPower.CODEC.optionalFieldOf("power").forGetter(Skill::power))
                  .apply(instance, Skill::new));

  public boolean active() {
    return power.isPresent();
  }
}
