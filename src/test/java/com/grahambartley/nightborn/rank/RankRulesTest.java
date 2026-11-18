package com.grahambartley.nightborn.rank;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.faction.Oath;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RankRulesTest {

  @ParameterizedTest(name = "rank {0}, earned {1}, deeds {2}, vampire {3} -> {4}")
  @CsvSource({
    "0, 0, 0, false, 0",
    "0, 100, 0, false, 0",
    "0, 4, 2, false, 0",
    "0, 5, 2, false, 1",
    "0, 100, 2, false, 1",
    "0, 100, 6, false, 2",
    "0, 100, 14, false, 3",
    "0, 19, 14, false, 1",
    "0, 44, 14, false, 2",
    "0, 100, 12, false, 0",
    "1, 100, 4, false, 2",
    "3, 0, 0, false, 3",
    "0, 100, 14, true, 0"
  })
  @DisplayName(
      "a hunter rises only with both the points and the deed, a deed done early counts, a rank is"
          + " never lost and a vampire never rises")
  void advance(
      final int rank,
      final int earned,
      final int deeds,
      final boolean vampire,
      final int expected) {
    final Oath oath = new Oath(true, true, rank, deeds);
    assertEquals(
        expected,
        RankRules.advance(oath, vampire, earned, RankFixtures.LADDER, RankFixtures.NEEDS).rank());
  }

  @ParameterizedTest(name = "proven {0}")
  @CsvSource({"false", "true"})
  @DisplayName("a human who has not joined the guild holds no rank, whatever they have done")
  void notSworn(final boolean proven) {
    final Oath oath = new Oath(proven, false, 0, 14);
    assertEquals(
        oath, RankRules.advance(oath, false, 999, RankFixtures.LADDER, RankFixtures.NEEDS));
  }

  @ParameterizedTest(name = "ladder of {0}, {1} thresholds -> top {2}")
  @CsvSource({"4, 3, 3", "4, 2, 2", "2, 3, 1", "1, 3, 0", "0, 3, 0"})
  @DisplayName("the top rank is the lower of the ladder and the thresholds set")
  void top(final int rungs, final int thresholds, final int top) {
    final RankLadder ladder = new RankLadder(RankFixtures.LADDER.rungs().subList(0, rungs));
    final RankNeeds needs = new RankNeeds(RankFixtures.NEEDS.points().subList(0, thresholds));
    assertEquals(top, RankRules.top(ladder, needs));
  }

  @ParameterizedTest(name = "{0} at stage {1} -> deeds {2}")
  @CsvSource({
    "STAKE, 0, 0",
    "STAKE, 1, 2",
    "STAKE, 4, 2",
    "'STAKE, CLEAR_HOUSEHOLD', 0, 4",
    "'STAKE, CLEAR_HOUSEHOLD', 2, 6",
    "END_ELDEST, 5, 8",
    "'STAKE, END_ELDEST', 5, 10"
  })
  @DisplayName("each act marks the deed of every rank it fulfils")
  void did(final String acts, final int stageRank, final int deeds) {
    final List<RankDeed.Act> done =
        Arrays.stream(acts.split(", ")).map(RankDeed.Act::valueOf).toList();
    assertEquals(
        deeds, RankRules.did(Oath.NONE.swear(), RankFixtures.LADDER, done, stageRank).deeds());
  }

  @ParameterizedTest(name = "rank {0} with no deed in the data, earned {1} -> {2}")
  @CsvSource({"1, 4, false", "1, 5, true", "2, 20, false"})
  @DisplayName("a rank whose deed was removed by a data pack opens on points alone")
  void deedlessRank(final int rank, final int earned, final boolean reaches) {
    final RankLadder ladder =
        new RankLadder(
            List.of(
                RankFixtures.LADDER.rungs().get(0),
                RankFixtures.rung("hunter", 1, java.util.Optional.empty()),
                RankFixtures.LADDER.rungs().get(2)));
    assertEquals(
        reaches, RankRules.reaches(Oath.NONE.swear(), earned, rank, ladder, RankFixtures.NEEDS));
  }
}
