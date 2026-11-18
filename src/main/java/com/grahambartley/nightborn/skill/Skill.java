package com.grahambartley.nightborn.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;

public record Skill(
    Identifier branch,
    List<Identifier> requires,
    Identifier icon,
    int column,
    int row,
    Optional<Identifier> stage,
    Optional<Identifier> rank,
    List<SkillRank> ranks,
    Optional<SkillPower> power) {

  public static final int MAX_COLUMN = 2;
  public static final int MAX_ROW = 6;
  public static final int MAX_RANKS = 3;

  public static final Codec<Skill> CODEC =
      RecordCodecBuilder.<Skill>create(
              instance ->
                  instance
                      .group(
                          Identifier.CODEC.fieldOf("branch").forGetter(Skill::branch),
                          Identifier.CODEC
                              .listOf()
                              .optionalFieldOf("requires", List.of())
                              .forGetter(Skill::requires),
                          Identifier.CODEC.fieldOf("icon").forGetter(Skill::icon),
                          Codec.intRange(0, MAX_COLUMN)
                              .optionalFieldOf("column", 0)
                              .forGetter(Skill::column),
                          Codec.intRange(0, MAX_ROW)
                              .optionalFieldOf("row", 0)
                              .forGetter(Skill::row),
                          Identifier.CODEC.optionalFieldOf("stage").forGetter(Skill::stage),
                          Identifier.CODEC.optionalFieldOf("rank").forGetter(Skill::rank),
                          SkillRank.CODEC
                              .listOf(1, MAX_RANKS)
                              .fieldOf("ranks")
                              .forGetter(Skill::ranks),
                          SkillPower.CODEC.optionalFieldOf("power").forGetter(Skill::power))
                      .apply(instance, Skill::new))
          .validate(Skill::powersHaveOneRank);

  public Skill {
    requires = List.copyOf(requires);
    ranks = List.copyOf(ranks);
  }

  public boolean active() {
    return power.isPresent();
  }

  public int maxRank() {
    return ranks.size();
  }

  public int nextCost(final int ranksOwned) {
    return ranks.get(Math.max(0, Math.min(ranksOwned, ranks.size() - 1))).cost();
  }

  public List<SkillRank> held(final int ranksOwned) {
    return ranks.subList(0, Math.max(0, Math.min(ranksOwned, ranks.size())));
  }

  private static DataResult<Skill> powersHaveOneRank(final Skill skill) {
    return skill.active() && skill.maxRank() > 1
        ? DataResult.error(() -> "A power has one rank, and only passive skills have more")
        : DataResult.success(skill);
  }
}
