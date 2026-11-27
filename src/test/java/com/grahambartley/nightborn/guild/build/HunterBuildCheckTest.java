package com.grahambartley.nightborn.guild.build;

import static com.grahambartley.nightborn.guild.build.HunterBuildFixtures.id;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class HunterBuildCheckTest {

  private static final Set<net.minecraft.util.Identifier> RANKS =
      Set.of(id("initiate"), id("hunter"), id("veteran"), id("master"));
  private static final Set<net.minecraft.util.Identifier> HUNTER_SKILLS =
      HunterBuildFixtures.treeOf(HunterBuildFixtures.hunterBranches());
  private static final Set<net.minecraft.util.Identifier> KITS =
      Set.of(
          id("crossbow"),
          id("stake"),
          id("silver_stake"),
          id("raid_wooden"),
          id("raid_silver"),
          id("raid_silver_holy_water"),
          id("master_silver"));
  private static final Map<net.minecraft.util.Identifier, Integer> MASTER_KITS =
      Map.of(id("master_silver"), 1);

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"hunter"})
  @DisplayName("the shipped build names only ranks, kits and skills that exist")
  void shippedIsClean(final String build) {
    assertEquals(
        List.of(),
        HunterBuildCheck.problems(HunterBuildFixtures.build(), RANKS, HUNTER_SKILLS, KITS));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "baron, master, crossbow, slayer/quick_shot, rank weight names nightborn:baron",
    "master, baron, crossbow, slayer/quick_shot, think ticks name nightborn:baron",
    "master, master, sling, slayer/quick_shot, kit leanings name nightborn:sling",
    "master, master, crossbow, slayer/fireball, nightborn:crossbow leans toward nightborn:slayer"
        + "/fireball",
    "master, master, crossbow, core/blood_rush, nightborn:crossbow leans toward nightborn:core"
        + "/blood_rush"
  })
  @DisplayName("a build that names something unknown is reported")
  void reportsUnknownNames(
      final String weighted,
      final String thinking,
      final String kit,
      final String skill,
      final String problem) {
    final HunterBuild build =
        new HunterBuild(
            Map.of(id(weighted), 1),
            Map.of(id(thinking), 30),
            0,
            1,
            1,
            Map.of(id(kit), Map.of(id(skill), 4)),
            0,
            1200,
            0,
            MASTER_KITS);
    final List<String> problems = HunterBuildCheck.problems(build, RANKS, HUNTER_SKILLS, KITS);
    assertEquals(1, problems.size(), problems.toString());
    assertEquals(true, problems.getFirst().startsWith(problem), problems.toString());
  }

  @ParameterizedTest(name = "weight {0}")
  @ValueSource(ints = {0})
  @DisplayName("a build where no rank can be rolled is reported")
  void reportsNoRollableRank(final int weight) {
    final HunterBuild build =
        new HunterBuild(
            Map.of(id("master"), weight), Map.of(), 0, 1, 1, Map.of(), 0, 1200, 0, MASTER_KITS);
    assertEquals(
        List.of("no rank has a weight above zero"),
        HunterBuildCheck.problems(build, RANKS, Set.of(), KITS));
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({
    "nightborn:elder, veteran, 0",
    "nightborn:elder, baron, 1",
    "nightborn:veteran, master, 0"
  })
  @DisplayName("a raiding party or veteran that names an unknown rank is reported")
  void reportsUnknownRankNames(final String owner, final String rank, final int problems) {
    assertEquals(problems, HunterBuildCheck.unknownRanks(owner, Set.of(id(rank)), RANKS).size());
  }

  @ParameterizedTest(name = "core weight {0}, path weight {1}: reported {2}")
  @CsvSource({"0, 0, true", "1, 0, false", "0, 1, false", "2, 3, false"})
  @DisplayName("a build that weighs neither core nor path skills is reported")
  void reportsNoBaseWeight(final int core, final int path, final boolean reported) {
    final HunterBuild build =
        new HunterBuild(
            Map.of(id("master"), 1), Map.of(), 0, core, path, Map.of(), 0, 1200, 0, MASTER_KITS);
    assertEquals(
        reported ? 1 : 0, HunterBuildCheck.problems(build, RANKS, HUNTER_SKILLS, KITS).size());
  }

  @ParameterizedTest(name = "master kits {0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "master_silver=1 | ''",
        "master_silver=0 | no Guild Master kit has a weight above zero",
        "'' | no Guild Master kit has a weight above zero",
        "master_silver=1,sling=2 | master kits name nightborn:sling",
        "stake=1,silver_stake=3 | ''"
      })
  @DisplayName("a build whose Guild Master kits are missing or unknown is reported")
  void reportsMasterKits(final String kits, final String problem) {
    final Map<net.minecraft.util.Identifier, Integer> masterKits =
        kits.isEmpty()
            ? Map.of()
            : java.util.Arrays.stream(kits.split(","))
                .map(entry -> entry.split("="))
                .collect(
                    java.util.stream.Collectors.toMap(
                        entry -> id(entry[0]), entry -> Integer.parseInt(entry[1])));
    final HunterBuild build =
        new HunterBuild(
            Map.of(id("master"), 1), Map.of(), 0, 1, 1, Map.of(), 0, 1200, 0, masterKits);
    final List<String> problems = HunterBuildCheck.problems(build, RANKS, HUNTER_SKILLS, KITS);
    assertEquals(problem.isEmpty() ? 0 : 1, problems.size(), problems.toString());
    assertEquals(
        true, problem.isEmpty() || problems.getFirst().startsWith(problem), problems.toString());
  }
}
