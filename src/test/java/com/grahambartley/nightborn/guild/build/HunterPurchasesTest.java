package com.grahambartley.nightborn.guild.build;

import static com.grahambartley.nightborn.guild.build.HunterBuildFixtures.CORE;
import static com.grahambartley.nightborn.guild.build.HunterBuildFixtures.FAITHFUL;
import static com.grahambartley.nightborn.guild.build.HunterBuildFixtures.SLAYER;
import static com.grahambartley.nightborn.guild.build.HunterBuildFixtures.TRAPPER;
import static com.grahambartley.nightborn.guild.build.HunterBuildFixtures.id;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.skill.BoughtSkill;
import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.TreeProgress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class HunterPurchasesTest {

  private static final HunterPurchases.Shop SHOP = HunterBuildFixtures.shop();
  private static final Map<Identifier, Skill> SKILLS = HunterBuildFixtures.skills();

  private static TreeProgress start(final int points, final Identifier... paths) {
    return new TreeProgress(points, 0.0F, List.of(), List.of(paths));
  }

  @ParameterizedTest(name = "rank {0}, {1} points, paths {2} and {3}, seed {4}")
  @CsvSource({
    "0, 0, slayer, faithful, 1",
    "0, 4, trapper, seeker, 2",
    "1, 12, slayer, trapper, 3",
    "1, 19, faithful, seeker, 4",
    "2, 30, slayer, faithful, 5",
    "2, 44, trapper, seeker, 6",
    "3, 50, slayer, seeker, 7",
    "3, 55, faithful, trapper, 8"
  })
  @DisplayName(
      "a hunter spends its points by the player's rules, in its core and two paths only, until"
          + " nothing more is allowed")
  void spendsLegally(
      final int rank, final int points, final String one, final String two, final long seed) {
    final TreeProgress spent =
        HunterPurchases.spend(
            SHOP, start(points, id(one), id(two)), rank, HunterBuildFixtures.rolls(seed));
    assertTrue(HunterPurchases.replays(SHOP, spent, rank));
    assertTrue(
        Set.of(CORE, id(one), id(two)).containsAll(HunterBuildFixtures.branchesOwned(spent)));
    assertEquals(points, spent.earned());
    assertTrue(spent.spent() <= points);
    assertEquals(List.of(), HunterPurchases.open(SHOP, spent, rank));
  }

  @ParameterizedTest(name = "rank {0}: the rank-gated skill {1} can be bought: {2}")
  @CsvSource({
    "0, slayer/heartstrike, false",
    "1, slayer/heartstrike, true",
    "1, slayer/last_stand, false",
    "2, slayer/last_stand, true",
    "2, slayer/onslaught, false",
    "3, slayer/onslaught, true"
  })
  @DisplayName("a skill gated behind a rank opens only once the hunter holds that rank")
  void rankGates(final int rank, final String skill, final boolean open) {
    final TreeProgress all =
        HunterPurchases.every(
            SHOP, start(0, SLAYER, FAITHFUL), HunterBuildFixtures.ladder().size() - 1);
    final List<BoughtSkill> before =
        all.bought().stream().takeWhile(b -> !b.skill().equals(id(skill))).toList();
    final TreeProgress ready =
        new TreeProgress(TreeProgress.MAX_POINTS, 0.0F, before, List.of(SLAYER, FAITHFUL));
    assertEquals(open, HunterPurchases.open(SHOP, ready, rank).contains(id(skill)));
  }

  @ParameterizedTest(name = "paths {0} and {1}")
  @CsvSource({"slayer, faithful", "trapper, seeker", "seeker, slayer", "faithful, trapper"})
  @DisplayName("every skill of the core and both paths, at every rank of each, and nothing else")
  void everySkillOfTheCoreAndPaths(final String one, final String two) {
    final TreeProgress all = HunterPurchases.every(SHOP, start(0, id(one), id(two)), 3);
    final Set<Identifier> expected = HunterBuildFixtures.treeOf(Set.of(CORE, id(one), id(two)));
    assertEquals(expected, Set.copyOf(all.ownedInOrder()));
    expected.forEach(skill -> assertEquals(SKILLS.get(skill).maxRank(), all.rank(skill)));
    assertEquals(all.spent(), all.earned());
    assertTrue(HunterPurchases.replays(SHOP, all, 3));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "legal: a core skill and two paths, faithful/blessing;trapper/snare, true",
    "a skill before what it needs, slayer/riposte, false",
    "a third path, faithful/blessing;seeker/sixth_sense, false",
    "a rank-gated skill too early, trapper/snare;trapper/flash_trap, false"
  })
  @DisplayName("a replay fails for a set of skills a player could not have bought")
  void replayRejectsIllegalBuilds(final String why, final String extra, final boolean replays) {
    final List<BoughtSkill> bought =
        new ArrayList<>(
            List.of(
                new BoughtSkill(id("hunter/tracking"), 1),
                new BoughtSkill(id("hunter/faith"), 2),
                new BoughtSkill(id("trapper/field_craft"), 1)));
    for (final String skill : extra.split(";")) {
      bought.add(new BoughtSkill(id(skill), SKILLS.get(id(skill)).nextCost(0)));
    }
    final TreeProgress built = new TreeProgress(50, 0.0F, bought, List.of());
    assertEquals(replays, HunterPurchases.replays(SHOP, built, 0), why);
  }

  @ParameterizedTest(name = "points {0}: replays {1}")
  @CsvSource({"6, true", "5, false"})
  @DisplayName("a replay fails when the points earned cannot pay for what was bought")
  void replayNeedsThePoints(final int points, final boolean replays) {
    final TreeProgress built =
        new TreeProgress(
            points,
            0.0F,
            List.of(
                new BoughtSkill(id("hunter/tracking"), 1),
                new BoughtSkill(id("hunter/faith"), 2),
                new BoughtSkill(id("faithful/rebuke"), 2),
                new BoughtSkill(id("hunter/weathered"), 1)),
            List.of());
    assertEquals(replays, HunterPurchases.replays(SHOP, built, 0));
  }

  @ParameterizedTest(name = "roll {0}")
  @CsvSource({"0.0", "0.5", "0.9999"})
  @DisplayName("what it buys leans toward the weighted skills")
  void leansTowardTheWeightedSkill(final double roll) {
    final HunterPurchases.Shop leaning =
        HunterBuildFixtures.shop(skill -> skill.equals(id("slayer/stake_throw")) ? 1000 : 0);
    final TreeProgress tracked =
        new TreeProgress(
            10, 0.0F, List.of(new BoughtSkill(id("hunter/tracking"), 1)), List.of(SLAYER, TRAPPER));
    assertEquals(
        Optional.of(id("slayer/stake_throw")), HunterPurchases.next(leaning, tracked, 0, roll));
  }

  @ParameterizedTest(name = "{0} on a path: {1}")
  @CsvSource({
    "slayer/heartstrike, true",
    "trapper/snare, true",
    "hunter/tracking, false",
    "core/blood_rush, false",
    "missing/skill, false"
  })
  @DisplayName("only a skill of a line counts as a path skill")
  void onPath(final String skill, final boolean path) {
    assertEquals(
        path, HunterPurchases.onPath(SKILLS.get(id(skill)), HunterBuildFixtures.branches()));
  }
}
