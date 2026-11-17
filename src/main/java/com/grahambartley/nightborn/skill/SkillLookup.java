package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.mob.stage.StageGate;
import java.util.OptionalInt;
import java.util.function.Function;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public record SkillLookup(
    @Nullable Skill skill, @Nullable SkillBranch branch, SkillPurchase.Verdict verdict) {

  static boolean stageReached(
      @Nullable final Skill skill, final Function<Skill, OptionalInt> needed, final int stageRank) {
    return skill == null || StageGate.open(needed.apply(skill), stageRank);
  }

  public static SkillLookup of(
      final DynamicRegistryManager registries,
      final TreeProgress progress,
      final SkillTree playerTree,
      final Identifier id,
      final int stageRank) {
    final Skill skill = Skills.skill(registries, id);
    final SkillBranch branch = skill == null ? null : Skills.branch(registries, skill.branch());
    final boolean stageReached =
        stageReached(skill, s -> StageGate.needed(registries, s), stageRank);
    return new SkillLookup(
        skill, branch, SkillPurchase.check(progress, playerTree, id, skill, branch, stageReached));
  }
}
