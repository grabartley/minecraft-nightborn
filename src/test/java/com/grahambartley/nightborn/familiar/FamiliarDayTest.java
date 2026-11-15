package com.grahambartley.nightborn.familiar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.config.FamiliarSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FamiliarDayTest {

  private static final FamiliarSettings SETTINGS =
      new FamiliarSettings(60, 1, 7, 5, 15, 25, 10, 25, 30, 8, 32);

  @ParameterizedTest(name = "loyalty {0}, paid {1}, roll {2}: loyalty {3}, betrays {4}")
  @CsvSource({
    "60, true, 0, 65, false",
    "98, true, 0, 100, false",
    "60, false, 0, 45, false",
    "30, false, 29, 15, true",
    "30, false, 30, 15, false",
    "20, true, 0, 25, false",
    "19, true, 0, 24, true",
    "5, false, 99, 0, false"
  })
  @DisplayName("a paid day raises loyalty, an unpaid one lowers it, and low loyalty may betray")
  void pass(
      final int loyalty,
      final boolean paid,
      final int roll,
      final int after,
      final boolean betrays) {
    assertEquals(
        new FamiliarDay.Outcome(after, betrays), FamiliarDay.pass(loyalty, paid, SETTINGS, roll));
  }

  @ParameterizedTest(name = "roll {0}")
  @CsvSource({"0", "50", "99"})
  @DisplayName("a betrayal chance of 0 never betrays")
  void neverBetrays(final int roll) {
    final FamiliarSettings never = new FamiliarSettings(60, 1, 7, 5, 15, 25, 10, 25, 0, 8, 32);
    assertEquals(false, FamiliarDay.pass(0, false, never, roll).betrays());
  }
}
