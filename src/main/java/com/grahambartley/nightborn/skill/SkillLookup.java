package com.grahambartley.nightborn.skill;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public record SkillLookup(
    @Nullable Skill skill, @Nullable SkillBranch branch, SkillPurchase.Verdict verdict) {

  public static SkillLookup of(
      final DynamicRegistryManager registries,
      final TreeProgress progress,
      final SkillTree playerTree,
      final Identifier id) {
    final Skill skill = Skills.skill(registries, id);
    final SkillBranch branch = skill == null ? null : Skills.branch(registries, skill.branch());
    return new SkillLookup(
        skill, branch, SkillPurchase.check(progress, playerTree, id, skill, branch));
  }
}
