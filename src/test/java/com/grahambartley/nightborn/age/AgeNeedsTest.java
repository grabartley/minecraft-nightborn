package com.grahambartley.nightborn.age;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AgeNeedsTest {

  @ParameterizedTest(name = "{0} nights, {1} blood -> {2}")
  @CsvSource({"0, 0, false", "10, 99, false", "9, 100, false", "10, 100, true", "50, 900, true"})
  @DisplayName("a stage opens only with both its nights and its blood")
  void opens(final int nights, final int blood, final boolean opened) {
    assertEquals(opened, new AgeNeeds(10, 100).opened(new AgeProgress(0, nights, blood, 0)));
  }

  @Test
  @DisplayName("a stage that needs nothing is open from the start")
  void freeStage() {
    assertTrue(new AgeNeeds(0, 0).opened(AgeProgress.EMPTY));
    assertFalse(new AgeNeeds(1, 0).opened(AgeProgress.EMPTY));
  }
}
