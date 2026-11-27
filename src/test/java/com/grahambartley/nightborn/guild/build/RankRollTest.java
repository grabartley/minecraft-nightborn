package com.grahambartley.nightborn.guild.build;

import static com.grahambartley.nightborn.guild.build.HunterBuildFixtures.NEEDS;
import static com.grahambartley.nightborn.guild.build.HunterBuildFixtures.id;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.rank.RankLadder;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class RankRollTest {

  private static final RankLadder LADDER = HunterBuildFixtures.ladder();

  @ParameterizedTest(name = "roll {0} -> rank {1}")
  @CsvSource({
    "0.0, 0",
    "0.49, 0",
    "0.5, 1",
    "0.84, 1",
    "0.85, 2",
    "0.96, 2",
    "0.97, 3",
    "0.9999, 3"
  })
  @DisplayName("ranks are rolled by their weights, most often the lowest")
  void rollsByWeight(final double roll, final int rank) {
    assertEquals(
        rank,
        RankRoll.rank(
            LADDER,
            Map.of(id("initiate"), 50, id("hunter"), 35, id("veteran"), 12, id("master"), 3),
            roll));
  }

  @ParameterizedTest(name = "roll {0} -> rank {1}")
  @CsvSource({"0.0, 2", "0.49, 2", "0.5, 3", "0.9999, 3"})
  @DisplayName("a rank with no weight is never rolled, and an unknown rank is ignored")
  void skipsUnweightedRanks(final double roll, final int rank) {
    assertEquals(
        rank,
        RankRoll.rank(LADDER, Map.of(id("veteran"), 1, id("master"), 1, id("baron"), 1000), roll));
  }

  @ParameterizedTest(name = "roll {0}")
  @CsvSource({"0.0", "0.5", "0.9999"})
  @DisplayName("with no weights at all every hunter is an Initiate")
  void noWeightsIsTheFirstRank(final double roll) {
    assertEquals(0, RankRoll.rank(LADDER, Map.of(), roll));
  }

  @ParameterizedTest(name = "rank {0}, roll {1} -> {2} points")
  @CsvSource({
    "0, 0.0, 0",
    "0, 0.9999, 4",
    "1, 0.0, 5",
    "1, 0.9999, 19",
    "2, 0.0, 20",
    "2, 0.5, 32",
    "2, 0.9999, 44",
    "3, 0.0, 45",
    "3, 0.9999, 55"
  })
  @DisplayName(
      "a hunter starts with the points of its rank and fewer than the next, or a spread past the"
          + " top")
  void pointsFitTheRank(final int rank, final double roll, final int points) {
    assertEquals(points, RankRoll.points(NEEDS, rank, 10, roll));
  }

  @ParameterizedTest(name = "fixed {0}, raid odds {1}, roll {2} -> rank {3}")
  @CsvSource({
    "veteran, false, 0.0, 2",
    "master, true, 0.0, 3",
    "baron, false, 0.0, 0",
    "none, true, 0.0, 2",
    "none, false, 0.0, 0",
    "none, false, 0.9999, 1"
  })
  @DisplayName(
      "a fixed rank wins, then a raiding party's odds, then the build's own weights, and an"
          + " unknown fixed rank falls back to a roll")
  void choosesFixedThenOddsThenDefaults(
      final String fixed, final boolean raid, final double roll, final int rank) {
    assertEquals(
        rank,
        RankRoll.choose(
            LADDER,
            fixed.equals("none") ? Optional.empty() : Optional.of(id(fixed)),
            raid ? Map.of(id("veteran"), 1) : Map.of(),
            Map.of(id("initiate"), 1, id("hunter"), 1),
            roll));
  }
}
