package com.grahambartley.nightborn.guild.build;

import static com.grahambartley.nightborn.guild.build.HunterBuildFixtures.CORE;
import static com.grahambartley.nightborn.guild.build.HunterBuildFixtures.NEEDS;
import static com.grahambartley.nightborn.guild.build.HunterBuildFixtures.id;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.config.SkillSettings;
import com.grahambartley.nightborn.faction.Oath;
import com.grahambartley.nightborn.rank.RankLadder;
import com.grahambartley.nightborn.rank.RankNeeds;
import com.grahambartley.nightborn.skill.SkillCurve;
import com.grahambartley.nightborn.skill.TreeProgress;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class HunterGrowthTest {

  private static final HunterPurchases.Shop SHOP = HunterBuildFixtures.shop();
  private static final RankLadder LADDER = HunterBuildFixtures.ladder();
  private static final HunterGrowth.Curve CURVE = new HunterGrowth.Curve(100, 3, NEEDS);
  private static final HunterGrowth.Curve DEFAULT_CURVE =
      HunterGrowth.Curve.of(NightbornConfig.DEFAULTS.skills());

  @ParameterizedTest(name = "rank {0} -> {1}")
  @CsvSource({"0, 0", "2, 2", "3, 3", "9, 3", "-1, 0"})
  @DisplayName("a hunter mob is sworn at its rank, inside the ladder, with every deed waived")
  void swornWithDeedsWaived(final int rank, final int held) {
    final Oath oath = HunterGrowth.sworn(LADDER, rank);
    assertTrue(oath.sworn());
    assertEquals(held, oath.rank());
    for (int deed = 0; deed < LADDER.size(); deed++) {
      assertTrue(oath.deedDone(deed));
    }
  }

  @ParameterizedTest(name = "rank {0}, {1} points, paths {2} and {3}, seed {4}")
  @CsvSource({
    "0, 3, slayer, faithful, 11",
    "1, 10, trapper, seeker, 12",
    "2, 30, faithful, trapper, 13",
    "3, 50, slayer, seeker, 14"
  })
  @DisplayName("a new hunter holds its rank, its two paths and a legal build of its points")
  void beginsWithALegalBuild(
      final int rank, final int points, final String one, final String two, final long seed) {
    final HunterGrowth.Standing standing =
        HunterGrowth.begin(
            SHOP, rank, points, List.of(id(one), id(two)), HunterBuildFixtures.rolls(seed));
    assertEquals(rank, standing.oath().rank());
    assertEquals(points, standing.progress().earned());
    assertEquals(List.of(id(one), id(two)), standing.progress().lines());
    assertTrue(standing.progress().spent() > 0);
    assertTrue(HunterPurchases.replays(SHOP, standing.progress(), rank));
  }

  @ParameterizedTest(name = "paths {0} and {1}")
  @CsvSource({"slayer, faithful", "trapper, seeker"})
  @DisplayName("a Guild Master is Master rank with every skill of the core and his two paths")
  void masterHoldsEverySkill(final String one, final String two) {
    final HunterGrowth.Standing standing =
        HunterGrowth.master(SHOP, LADDER.size() - 1, List.of(id(one), id(two)));
    assertEquals(LADDER.size() - 1, standing.oath().rank());
    assertEquals(
        HunterBuildFixtures.treeOf(Set.of(CORE, id(one), id(two))),
        Set.copyOf(standing.progress().ownedInOrder()));
  }

  @ParameterizedTest(name = "earned {0} with {1} banked, gains {2}: {3} earned, rank {4}")
  @CsvSource({
    "0, 0, 0, 0, 0",
    "0, 0, 99, 0, 0",
    "0, 0, 100, 1, 0",
    "4, 0, 113, 5, 1",
    "19, 0, 175, 20, 2",
    "3, 50, 51, 3, 0"
  })
  @DisplayName(
      "experience becomes points on the player's curve, and points raise the rank as a player's"
          + " do")
  void growsOnThePlayersCurve(
      final int earned, final float banked, final float gained, final int after, final int rank) {
    final int startRank = earned >= 5 ? (earned >= 20 ? 2 : 1) : 0;
    final HunterGrowth.Standing before =
        new HunterGrowth.Standing(
            HunterGrowth.sworn(LADDER, startRank),
            new TreeProgress(earned, banked, List.of(), List.of(id("slayer"), id("faithful"))));
    final HunterGrowth.Standing grown =
        HunterGrowth.grow(before, gained, SHOP, CURVE, HunterBuildFixtures.rolls(earned));
    assertEquals(after, grown.progress().earned());
    assertEquals(rank, grown.oath().rank());
    assertTrue(HunterPurchases.replays(SHOP, grown.progress(), grown.oath().rank()));
  }

  @ParameterizedTest(name = "seed {0}")
  @CsvSource({"21", "22", "23"})
  @DisplayName("a new point is spent on a new skill as soon as one is affordable")
  void buysWithANewPoint(final long seed) {
    final HunterGrowth.Standing tracked =
        HunterGrowth.begin(
            SHOP, 0, 1, List.of(id("slayer"), id("faithful")), HunterBuildFixtures.rolls(seed));
    assertEquals(List.of(id("hunter/tracking")), tracked.progress().ownedInOrder());
    final float onePoint = SkillCurve.cost(1, 100, 3);
    final HunterGrowth.Standing grown =
        HunterGrowth.grow(tracked, onePoint, SHOP, CURVE, HunterBuildFixtures.rolls(seed));
    assertEquals(2, grown.progress().earned());
    assertEquals(2, grown.progress().ownedInOrder().size());
  }

  @ParameterizedTest(name = "rank {0}")
  @CsvSource({"1", "2", "3"})
  @DisplayName("a rank is never lost, however few points the hunter holds")
  void neverLosesARank(final int rank) {
    final HunterGrowth.Standing low =
        new HunterGrowth.Standing(HunterGrowth.sworn(LADDER, rank), TreeProgress.EMPTY);
    assertEquals(
        rank,
        HunterGrowth.grow(low, 1.0F, SHOP, CURVE, HunterBuildFixtures.rolls(rank)).oath().rank());
  }

  @ParameterizedTest(name = "paths {0} and {1}")
  @CsvSource({
    "slayer, faithful",
    "trapper, seeker",
    "seeker, slayer",
    "slayer, trapper",
    "faithful, trapper",
    "faithful, seeker"
  })
  @DisplayName(
      "a hunter that owns every skill open to it banks new experience as points and buys nothing")
  void ownsEverythingAndBanksPoints(final String one, final String two) {
    final HunterGrowth.Standing full =
        HunterGrowth.master(SHOP, LADDER.size() - 1, List.of(id(one), id(two)));
    final HunterGrowth.Standing grown =
        HunterGrowth.grow(full, 5000.0F, SHOP, DEFAULT_CURVE, HunterBuildFixtures.rolls(1));
    assertTrue(grown.progress().earned() > full.progress().earned());
    assertEquals(full.progress().bought(), grown.progress().bought());
  }

  @ParameterizedTest(name = "{0} points, paths {1} and {2}, seed {3}")
  @CsvSource({
    "0, slayer, faithful, 31",
    "4, trapper, seeker, 32",
    "9, faithful, trapper, 33",
    "9, seeker, slayer, 34",
    "9, slayer, trapper, 35",
    "9, faithful, seeker, 36"
  })
  @DisplayName(
      "an initiate buys at least one skill with 5000 experience on the default curve, at any"
          + " starting points and path pair")
  void initiateBuysWithExperience(
      final int points, final String one, final String two, final long seed) {
    final HunterGrowth.Standing start =
        HunterGrowth.begin(
            SHOP, 0, points, List.of(id(one), id(two)), HunterBuildFixtures.rolls(seed));
    final HunterGrowth.Standing grown =
        HunterGrowth.grow(
            start, 5000.0F, SHOP, DEFAULT_CURVE, HunterBuildFixtures.rolls(seed + 100));
    assertTrue(grown.progress().bought().size() > start.progress().bought().size());
  }

  @ParameterizedTest(name = "first point {0}, rise {1}%, ranks at {2}, {3} and {4}")
  @CsvSource({"200, 1, 10, 45, 100", "50, 0, 0, 0, 0", "1000, 25, 5, 20, 400"})
  @DisplayName("a hunter's point curve and rank needs are the server's skill settings")
  void curveFollowsTheSkillSettings(
      final int first, final int rise, final int hunter, final int veteran, final int master) {
    final SkillSettings settings =
        NightbornConfig.DEFAULTS
            .with(SkillSettings.FIRST_POINT_EXPERIENCE, first)
            .with(SkillSettings.POINT_COST_RISE, rise)
            .with(SkillSettings.HUNTER_RANK_POINTS, hunter)
            .with(SkillSettings.VETERAN_RANK_POINTS, veteran)
            .with(SkillSettings.MASTER_RANK_POINTS, master)
            .skills();
    assertEquals(
        new HunterGrowth.Curve(first, rise, new RankNeeds(List.of(hunter, veteran, master))),
        HunterGrowth.Curve.of(settings));
  }
}
