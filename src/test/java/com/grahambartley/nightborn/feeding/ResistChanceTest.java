package com.grahambartley.nightborn.feeding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ResistChanceTest {

  @ParameterizedTest(name = "{0}/{1} health at {2}%: {3}")
  @CsvSource({
    "20, 20, 10, 0.10",
    "10, 20, 10, 0.05",
    "10, 10, 10, 0.05",
    "40, 40, 10, 0.20",
    "1, 20, 10, 0.005",
    "100, 100, 50, 1.0",
    "20, 20, 0, 0.0"
  })
  @DisplayName("resisting grows with maximum health and falls as health drops")
  void scales(final float health, final float max, final int percent, final float expected) {
    assertEquals(expected, ResistChance.of(health, max, percent, false), 0.0001F);
  }

  @ParameterizedTest(name = "chance {0} against a grip of {1}: {2}")
  @CsvSource({"0.1, 1.0, 0.1", "0.1, 0.9, 0.09", "0.1, 0.729, 0.0729", "0.5, 0, 0", "0.5, 3, 0.5"})
  @DisplayName("a stronger grip lowers the chance to break free by a share of what it is")
  void held(final float chance, final double struggle, final float expected) {
    assertEquals(expected, ResistChance.held(chance, struggle), 0.0001F);
  }

  @Test
  @DisplayName("a bigger creature resists more than a smaller one at the same share of health")
  void biggerResistsMore() {
    assertTrue(ResistChance.of(40, 40, 10, false) > ResistChance.of(10, 10, 10, false));
  }

  @ParameterizedTest(name = "health {0}")
  @CsvSource({"20", "1"})
  @DisplayName("a sleeping victim never resists")
  void sleepingNeverResists(final float health) {
    assertEquals(0.0F, ResistChance.of(health, 20, 100, true));
  }
}
