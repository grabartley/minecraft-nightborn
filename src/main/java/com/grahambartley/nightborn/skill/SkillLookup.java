package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.mob.stage.StageGate;
import com.grahambartley.nightborn.rank.RankGate;
import java.util.OptionalInt;
import java.util.function.Function;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public record SkillLookup(
    @Nullable Skill skill, @Nullable SkillBranch branch, SkillPurchase.Verdict verdict) {

  static boolean reached(
      @Nullable final Skill skill, final Function<Skill, OptionalInt> needed, final int held) {
    return skill == null || StageGate.open(needed.apply(skill), held);
  }

  public static SkillLookup of(
      final DynamicRegistryManager registries,
      final boolean sided,
      final TreeProgress progress,
      final SkillTree playerTree,
      final Identifier id,
      final int stageRank,
      final int guildRank) {
    final Skill skill = Skills.skill(registries, id);
    final SkillBranch branch = skill == null ? null : Skills.branch(registries, skill.branch());
    return new SkillLookup(
        skill,
        branch,
        SkillPurchase.check(
            sided,
            progress,
            playerTree,
            id,
            skill,
            branch,
            reached(skill, s -> StageGate.needed(registries, s), stageRank),
            reached(skill, s -> RankGate.needed(registries, s), guildRank)));
  }
}
