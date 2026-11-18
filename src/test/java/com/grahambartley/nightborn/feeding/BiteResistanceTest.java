package com.grahambartley.nightborn.feeding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BiteResistanceTest {

  @ParameterizedTest(name = "{0} with {1} resistance: {2}")
  @CsvSource({
    "1000, 0.0, 1000",
    "1000, 0.35, 650",
    "1000, 0.7, 300",
    "1000, 1.0, 0",
    "1000, 1.5, 0",
    "1000, -0.5, 1000",
    "1, 0.35, 1",
    "1, 0.7, 0",
    "0, 0.35, 0"
  })
  @DisplayName("bite resistance lowers the chance a bite starts the sickness")
  void sicknessChance(final int chance, final double resistance, final int left) {
    assertEquals(left, BiteResistance.sicknessChance(chance, resistance));
  }

  @ParameterizedTest(name = "{0} with {1} resistance: {2}")
  @CsvSource({
    "0.1, 0.0, 0.1",
    "0.1, 0.5, 0.15",
    "0.1, 1.0, 0.2",
    "0.6, 1.0, 1.0",
    "0.1, -1.0, 0.1",
    "0.0, 1.0, 0.0"
  })
  @DisplayName("bite resistance lets a victim break free of a feeding bite sooner")
  void breakFreeChance(final float chance, final double resistance, final float raised) {
    assertEquals(raised, BiteResistance.breakFreeChance(chance, resistance), 1.0E-6F);
  }

  @ParameterizedTest(name = "{0}s with {1} resistance: {2}s")
  @CsvSource({
    "30, 0.0, 30",
    "30, 0.5, 15",
    "30, 0.35, 20",
    "30, 1.0, 0",
    "30, 2.0, 0",
    "0, 0.5, 0"
  })
  @DisplayName("bite resistance shortens the Weakness after a bite")
  void weaknessSeconds(final int seconds, final double resistance, final int left) {
    assertEquals(left, BiteResistance.weaknessSeconds(seconds, resistance));
  }
}
