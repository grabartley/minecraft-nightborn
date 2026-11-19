package com.grahambartley.nightborn.trap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FieldRulesTest {

  @ParameterizedTest(name = "listed {0}, vampire {1}, inside {2}, reaches {3} -> {4}")
  @CsvSource({
    "true, true, true, true, true",
    "false, true, true, true, false",
    "true, false, true, true, false",
    "true, true, false, true, false",
    "true, true, true, false, false"
  })
  @DisplayName("a killing ground bars only its listed powers, for a vampire inside it may reach")
  void bars(
      final boolean listed,
      final boolean vampire,
      final boolean inside,
      final boolean reaches,
      final boolean barred) {
    assertEquals(barred, FieldRules.bars(listed, vampire, inside, reaches));
  }

  @ParameterizedTest(name = "distance² {0}, radius {1} -> {2}")
  @CsvSource({"0, 10, true", "100, 10, true", "100.5, 10, false"})
  @DisplayName("the ground covers its radius and no further")
  void inside(final double distance, final float radius, final boolean inside) {
    assertEquals(inside, FieldRules.inside(distance, radius));
  }

  @ParameterizedTest(name = "now {0}, until {1} -> {2}")
  @CsvSource({"0, 400, true", "399, 400, true", "400, 400, false"})
  @DisplayName("the ground lasts until its time is up")
  void lasts(final long now, final long until, final boolean lasts) {
    assertEquals(lasts, FieldRules.lasts(now, until));
  }
}
