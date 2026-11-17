package com.grahambartley.nightborn.age;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AgeRulesTest {

  static final List<AgeNeeds> NEEDS =
      List.of(new AgeNeeds(10, 100), new AgeNeeds(40, 500), new AgeNeeds(100, 1500));

  @ParameterizedTest(name = "rank {0}, {1} nights, {2} blood, deeds {3} -> rank {4}")
  @CsvSource({
    "0, 0, 0, 0, 0",
    "0, 10, 100, 0, 0",
    "0, 9, 100, 2, 0",
    "0, 10, 99, 2, 0",
    "0, 10, 100, 2, 1",
    "0, 100, 1500, 4, 0",
    "0, 100, 1500, 12, 0",
    "0, 100, 1500, 10, 1",
    "0, 100, 1500, 6, 2",
    "0, 100, 1500, 14, 3",
    "1, 40, 500, 4, 2",
    "2, 0, 0, 0, 2",
    "3, 0, 0, 0, 3"
  })
  @DisplayName("a stage is reached with its nights, blood and deed, in order and never skipped")
  void advances(
      final int rank, final int nights, final int blood, final int deeds, final int reached) {
    assertEquals(
        reached, AgeRules.advance(new AgeProgress(rank, nights, blood, deeds), 4, NEEDS).rank());
  }

  @ParameterizedTest(name = "{0} rungs -> top {1}")
  @CsvSource({"0, 0", "1, 0", "2, 1", "4, 3", "9, 3"})
  @DisplayName("the ladder stops at its last rung and at the last stage with settings")
  void tops(final int rungs, final int top) {
    assertEquals(top, AgeRules.top(rungs, NEEDS));
    assertEquals(
        top, AgeRules.advance(new AgeProgress(0, 1000, 100000, 65535), rungs, NEEDS).rank());
  }

  @Test
  @DisplayName("nothing reaches the first rung or a rung past the settings")
  void reachesOnlyRealRungs() {
    final AgeProgress all = new AgeProgress(0, 1000, 100000, 65535);
    assertFalse(AgeRules.reaches(all, 0, NEEDS));
    assertFalse(AgeRules.reaches(all, 4, NEEDS));
    assertTrue(AgeRules.reaches(all, 3, NEEDS));
  }
}
