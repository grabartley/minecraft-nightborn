package com.grahambartley.nightborn.age;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AgeOutlookTest {

  @ParameterizedTest(name = "rank {0}, {1} nights, {2} blood, deeds {3}")
  @CsvSource({
    "0, 0, 0, 0, 1, 10, 100, false, false",
    "0, 4, 37, 2, 1, 6, 63, true, false",
    "0, 10, 100, 0, 1, 0, 0, false, true",
    "0, 500, 5000, 0, 1, 0, 0, false, true",
    "1, 12, 480, 4, 2, 28, 20, true, false",
    "2, 99, 1500, 0, 3, 1, 0, false, false"
  })
  @DisplayName(
      "the next stage shows the nights and blood still needed and whether its deed is done")
  void looksAhead(
      final int rank,
      final int nights,
      final int blood,
      final int deeds,
      final int next,
      final int nightsLeft,
      final int bloodLeft,
      final boolean deedDone,
      final boolean opened) {
    final AgeOutlook outlook =
        AgeOutlook.of(new AgeProgress(rank, nights, blood, deeds), 4, AgeRulesTest.NEEDS)
            .orElseThrow();
    assertEquals(new AgeOutlook(next, nightsLeft, bloodLeft, deedDone), outlook);
    assertEquals(opened, outlook.opened());
  }

  @Test
  @DisplayName("the oldest stage has nothing ahead of it")
  void nothingPastTheTop() {
    assertEquals(
        Optional.empty(), AgeOutlook.of(new AgeProgress(3, 0, 0, 0), 4, AgeRulesTest.NEEDS));
    assertEquals(
        Optional.empty(), AgeOutlook.of(new AgeProgress(1, 0, 0, 0), 2, AgeRulesTest.NEEDS));
    assertTrue(AgeOutlook.of(AgeProgress.EMPTY, 2, AgeRulesTest.NEEDS).isPresent());
    assertFalse(AgeOutlook.of(AgeProgress.EMPTY, 1, AgeRulesTest.NEEDS).isPresent());
  }
}
