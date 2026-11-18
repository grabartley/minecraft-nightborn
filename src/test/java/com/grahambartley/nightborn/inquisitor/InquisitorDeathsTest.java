package com.grahambartley.nightborn.inquisitor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class InquisitorDeathsTest {

  @ParameterizedTest(name = "notoriety {0}, losing {1}%: keeps {2}")
  @CsvSource({"40, 50, 20", "40, 0, 40", "40, 100, 0", "33, 50, 16.5", "40, 150, 0", "40, -5, 40"})
  @DisplayName("a vampire who dies in his fight loses a share of their notoriety")
  void kept(final double notoriety, final int loss, final double kept) {
    assertEquals(kept, InquisitorDeaths.kept(notoriety, loss), 1.0E-9);
  }
}
