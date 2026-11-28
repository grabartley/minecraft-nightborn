package com.grahambartley.nightborn.guild.build;

import com.grahambartley.nightborn.config.SkillSettings;
import com.grahambartley.nightborn.faction.Oath;
import com.grahambartley.nightborn.rank.RankLadder;
import com.grahambartley.nightborn.rank.RankNeeds;
import com.grahambartley.nightborn.rank.RankRules;
import com.grahambartley.nightborn.skill.TreeProgress;
import java.util.List;
import java.util.function.DoubleSupplier;
import net.minecraft.util.Identifier;

public final class HunterGrowth {

  public record Standing(Oath oath, TreeProgress progress) {}

  public record Curve(int firstCost, int risePercent, RankNeeds needs) {

    public static Curve of(final SkillSettings settings) {
      return new Curve(
          settings.firstPointExperience(), settings.pointCostRise(), settings.rankNeeds());
    }
  }

  private HunterGrowth() {}

  public static Oath sworn(final RankLadder ladder, final int rank) {
    Oath oath = Oath.NONE.swear().withRank(Math.max(0, Math.min(rank, ladder.size() - 1)));
    for (int deed = 0; deed < ladder.size(); deed++) {
      oath = oath.withDeed(deed);
    }
    return oath;
  }

  public static Standing begin(
      final HunterPurchases.Shop shop,
      final int rank,
      final int points,
      final List<Identifier> paths,
      final DoubleSupplier rolls) {
    final Oath oath = sworn(shop.ladder(), rank);
    final TreeProgress start = new TreeProgress(points, 0.0F, List.of(), paths);
    return new Standing(oath, HunterPurchases.spend(shop, start, oath.rank(), rolls));
  }

  public static Standing master(
      final HunterPurchases.Shop shop, final int rank, final List<Identifier> paths) {
    final Oath oath = sworn(shop.ladder(), rank);
    final TreeProgress start = new TreeProgress(0, 0.0F, List.of(), paths);
    return new Standing(oath, HunterPurchases.every(shop, start, oath.rank()));
  }

  public static Standing grow(
      final Standing standing,
      final float experience,
      final HunterPurchases.Shop shop,
      final Curve curve,
      final DoubleSupplier rolls) {
    final TreeProgress earned =
        standing.progress().addExperience(experience, curve.firstCost(), curve.risePercent());
    final Oath oath =
        RankRules.advance(standing.oath(), false, earned.earned(), shop.ladder(), curve.needs());
    return new Standing(oath, HunterPurchases.spend(shop, earned, oath.rank(), rolls));
  }
}
