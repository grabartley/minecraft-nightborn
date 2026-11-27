package com.grahambartley.nightborn.guild.build;

import com.grahambartley.nightborn.mob.look.Weighted;
import com.grahambartley.nightborn.mob.stage.StageGate;
import com.grahambartley.nightborn.rank.RankGate;
import com.grahambartley.nightborn.rank.RankLadder;
import com.grahambartley.nightborn.skill.BoughtSkill;
import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillBranch;
import com.grahambartley.nightborn.skill.SkillPurchase;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.TreeProgress;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.DoubleSupplier;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import net.minecraft.util.Identifier;

public final class HunterPurchases {

  public record Shop(
      Map<Identifier, Skill> skills,
      Map<Identifier, SkillBranch> branches,
      RankLadder ladder,
      Predicate<Skill> stageReached,
      ToIntFunction<Identifier> weight) {

    public Shop {
      skills = new TreeMap<>(skills);
      branches = Map.copyOf(branches);
    }

    SkillPurchase.Verdict check(final TreeProgress progress, final Identifier id, final int rank) {
      final Skill skill = skills.get(id);
      final SkillBranch branch = skill == null ? null : branches.get(skill.branch());
      return SkillPurchase.check(
          true,
          progress,
          SkillTree.HUNTER,
          id,
          skill,
          branch,
          skill == null || stageReached.test(skill),
          skill == null || StageGate.open(RankGate.needed(ladder, skill), rank));
    }

    TreeProgress buy(final TreeProgress progress, final Identifier id) {
      final Skill skill = skills.get(id);
      return SkillPurchase.apply(progress, id, skill, branches.get(skill.branch()));
    }
  }

  private HunterPurchases() {}

  public static List<Identifier> open(
      final Shop shop, final TreeProgress progress, final int rank) {
    return shop.skills().keySet().stream()
        .filter(id -> shop.check(progress, id, rank) == SkillPurchase.Verdict.ALLOWED)
        .toList();
  }

  public static Optional<Identifier> next(
      final Shop shop, final TreeProgress progress, final int rank, final double roll) {
    return Weighted.pick(open(shop, progress, rank), shop.weight(), roll);
  }

  public static TreeProgress spend(
      final Shop shop, final TreeProgress progress, final int rank, final DoubleSupplier rolls) {
    TreeProgress spent = progress;
    for (Optional<Identifier> next = next(shop, spent, rank, rolls.getAsDouble());
        next.isPresent();
        next = next(shop, spent, rank, rolls.getAsDouble())) {
      spent = shop.buy(spent, next.get());
    }
    return spent;
  }

  public static TreeProgress every(final Shop shop, final TreeProgress progress, final int rank) {
    final TreeProgress all =
        spend(
            new Shop(shop.skills(), shop.branches(), shop.ladder(), shop.stageReached(), id -> 1),
            progress.withEarned(TreeProgress.MAX_POINTS),
            rank,
            () -> 0.0);
    return all.withEarned(all.spent());
  }

  public static boolean replays(final Shop shop, final TreeProgress progress, final int rank) {
    TreeProgress replay = TreeProgress.EMPTY.withEarned(progress.earned());
    for (final BoughtSkill bought : progress.bought()) {
      if (shop.check(replay, bought.skill(), rank) != SkillPurchase.Verdict.ALLOWED) {
        return false;
      }
      replay = shop.buy(replay, bought.skill());
    }
    return true;
  }
}
