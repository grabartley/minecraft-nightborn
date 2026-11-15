package com.grahambartley.nightborn.skill;

import java.util.Locale;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class SkillPurchase {

  public static final int BLOODLINE_LIMIT = 2;

  public enum Verdict {
    ALLOWED,
    UNKNOWN,
    WRONG_TREE,
    OWNED,
    BLOODLINE_LOCKED,
    NEEDS_SKILL,
    TOO_FEW_POINTS;

    public String translationKey() {
      return "nightborn.skills.verdict." + name().toLowerCase(Locale.ROOT);
    }
  }

  private SkillPurchase() {}

  public static Verdict check(
      final TreeProgress progress,
      final SkillTree playerTree,
      final Identifier id,
      @Nullable final Skill skill,
      @Nullable final SkillBranch branch) {
    if (skill == null || branch == null) {
      return Verdict.UNKNOWN;
    }
    if (branch.tree() != playerTree) {
      return Verdict.WRONG_TREE;
    }
    if (progress.owns(id)) {
      return Verdict.OWNED;
    }
    if (locked(progress, skill.branch(), branch)) {
      return Verdict.BLOODLINE_LOCKED;
    }
    if (!skill.requires().stream().allMatch(progress::owns)) {
      return Verdict.NEEDS_SKILL;
    }
    if (progress.free() < skill.cost()) {
      return Verdict.TOO_FEW_POINTS;
    }
    return Verdict.ALLOWED;
  }

  public static boolean locked(
      final TreeProgress progress, final Identifier branchId, final SkillBranch branch) {
    return branch.bloodline()
        && !progress.bloodlines().contains(branchId)
        && progress.bloodlines().size() >= BLOODLINE_LIMIT;
  }

  public static TreeProgress apply(
      final TreeProgress progress,
      final Identifier id,
      final Skill skill,
      final SkillBranch branch) {
    return progress.buy(id, skill.cost(), bloodline(skill, branch));
  }

  @Nullable
  public static Identifier bloodline(
      @Nullable final Skill skill, @Nullable final SkillBranch branch) {
    return skill != null && branch != null && branch.bloodline() ? skill.branch() : null;
  }
}
