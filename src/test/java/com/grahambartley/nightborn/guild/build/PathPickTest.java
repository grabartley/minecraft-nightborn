package com.grahambartley.nightborn.guild.build;

import static com.grahambartley.nightborn.guild.build.HunterBuildFixtures.FAITHFUL;
import static com.grahambartley.nightborn.guild.build.HunterBuildFixtures.SEEKER;
import static com.grahambartley.nightborn.guild.build.HunterBuildFixtures.SLAYER;
import static com.grahambartley.nightborn.guild.build.HunterBuildFixtures.TRAPPER;
import static com.grahambartley.nightborn.guild.build.HunterBuildFixtures.id;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.skill.SkillBranch;
import com.grahambartley.nightborn.skill.SkillTree;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class PathPickTest {

  private static final Map<Identifier, SkillBranch> BRANCHES = HunterBuildFixtures.branches();

  @ParameterizedTest(name = "{0} tree")
  @EnumSource(SkillTree.class)
  @DisplayName("the paths on offer are the learnable lines of the tree, in id order")
  void pathsAreTheLearnableLines(final SkillTree tree) {
    final List<Identifier> expected =
        tree == SkillTree.HUNTER
            ? List.of(FAITHFUL, SEEKER, SLAYER, TRAPPER)
            : List.of(id("beast"), id("noble"), id("occult"));
    assertEquals(expected, PathPick.paths(BRANCHES, tree));
  }

  @ParameterizedTest(name = "rolls {0} and {1} -> {2} and {3}")
  @CsvSource({
    "0.0, 0.0, faithful, seeker",
    "0.99, 0.99, trapper, slayer",
    "0.3, 0.0, seeker, faithful",
    "0.6, 0.5, slayer, seeker"
  })
  @DisplayName("with no leanings, two different paths are picked evenly from the four")
  void picksTwoDifferentPaths(
      final double first, final double second, final String one, final String two) {
    final Iterator<Double> rolls = List.of(first, second).iterator();
    assertEquals(
        List.of(id(one), id(two)),
        PathPick.pick(BRANCHES, SkillTree.HUNTER, List.of(), rolls::next));
  }

  @ParameterizedTest(name = "seed {0}")
  @ValueSource(longs = {1L, 2L, 3L, 42L, 1234L})
  @DisplayName("never more than two paths, never the same path twice, never the core")
  void neverMoreThanTwo(final long seed) {
    final List<Identifier> picked =
        PathPick.pick(BRANCHES, SkillTree.HUNTER, List.of(), HunterBuildFixtures.rolls(seed));
    assertEquals(2, picked.size());
    assertEquals(2, new HashSet<>(picked).size());
    assertTrue(Set.of(FAITHFUL, SEEKER, SLAYER, TRAPPER).containsAll(picked));
  }

  @ParameterizedTest(name = "seed {0}")
  @ValueSource(longs = {7L, 99L})
  @DisplayName("a tree with one line gives only that line")
  void fewerLinesThanTheLimit(final long seed) {
    assertEquals(
        List.of(SLAYER),
        PathPick.pick(
            Map.of(SLAYER, new SkillBranch(SkillTree.HUNTER, true, 1)),
            SkillTree.HUNTER,
            List.of(),
            HunterBuildFixtures.rolls(seed)));
  }

  @ParameterizedTest(name = "rolls {0} and {1} -> {2} and {3}")
  @CsvSource({
    "0.0, 0.0, slayer, seeker",
    "0.49, 0.99, slayer, seeker",
    "0.5, 0.0, trapper, seeker",
    "0.99, 0.5, trapper, seeker"
  })
  @DisplayName(
      "each pick follows the weights its slot gives, and an unweighted path is never picked")
  void followsSlotWeights(
      final double first, final double second, final String one, final String two) {
    final Iterator<Double> rolls = List.of(first, second).iterator();
    assertEquals(
        List.of(id(one), id(two)),
        PathPick.pick(
            BRANCHES,
            SkillTree.HUNTER,
            List.of(Map.of(SLAYER, 1, TRAPPER, 1, FAITHFUL, 0), Map.of(SEEKER, 1)),
            rolls::next));
  }

  @ParameterizedTest(name = "rolls {0} and {1} -> {2} and {3}")
  @CsvSource({
    "0.0, 0.0, slayer, faithful",
    "0.74, 0.0, slayer, faithful",
    "0.75, 0.0, trapper, faithful",
    "0.0, 0.99, slayer, trapper"
  })
  @DisplayName("a heavier path is picked more often than a lighter one in the same slot")
  void heavierPathsWinMoreRolls(
      final double first, final double second, final String one, final String two) {
    final Iterator<Double> rolls = List.of(first, second).iterator();
    assertEquals(
        List.of(id(one), id(two)),
        PathPick.pick(
            BRANCHES,
            SkillTree.HUNTER,
            List.of(Map.of(SLAYER, 3, TRAPPER, 1), Map.of(FAITHFUL, 1, TRAPPER, 1)),
            rolls::next));
  }

  @ParameterizedTest(name = "second roll {0} -> {1}")
  @CsvSource({"0.0, faithful", "0.34, seeker", "0.67, trapper"})
  @DisplayName("a slot the kit leaves out is picked evenly from the paths still open")
  void unlistedSlotsPickEvenly(final double second, final String two) {
    final Iterator<Double> rolls = List.of(0.5, second).iterator();
    assertEquals(
        List.of(SLAYER, id(two)),
        PathPick.pick(BRANCHES, SkillTree.HUNTER, List.of(Map.of(SLAYER, 1)), rolls::next));
  }

  @ParameterizedTest(name = "{0} may hold {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "crossbow | slayer,trapper,seeker",
        "stake | slayer,faithful,trapper",
        "silver_stake | faithful,slayer",
        "raid_silver | faithful,slayer",
        "raid_silver_holy_water | faithful,slayer",
        "master_silver | faithful,slayer"
      })
  @DisplayName("out of 100 hunters of a shipped kit, none holds a path its kit rules out")
  void shippedKitsHoldOnlyTheirPaths(final String kit, final String allowed) {
    final Set<Identifier> may =
        Arrays.stream(allowed.split(",")).map(HunterBuildFixtures::id).collect(Collectors.toSet());
    final List<Map<Identifier, Integer>> leanings =
        HunterBuildFixtures.build().paths(Optional.of(id(kit)));
    for (long seed = 0; seed < 100; seed++) {
      final List<Identifier> picked =
          PathPick.pick(BRANCHES, SkillTree.HUNTER, leanings, HunterBuildFixtures.rolls(seed));
      assertEquals(2, picked.size(), picked.toString());
      assertTrue(may.containsAll(picked), kit + " held " + picked);
    }
  }
}
