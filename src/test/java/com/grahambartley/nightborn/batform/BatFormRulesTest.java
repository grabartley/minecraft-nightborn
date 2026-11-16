package com.grahambartley.nightborn.batform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.weakened.WeakenedSources;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BatFormRulesTest {

  @ParameterizedTest(name = "sun {0}, downed {1}, warded {2}, blood {3} -> forced out {4}")
  @CsvSource({
    "false, false, false, 10, false",
    "true, false, false, 10, true",
    "false, true, false, 10, true",
    "false, false, true, 10, true",
    "false, false, false, 0, true",
    "false, false, false, 1, false"
  })
  @DisplayName("sun, being downed, garlic or holy water, or an empty blood bar force a bat back")
  void forcedOut(
      final boolean sun,
      final boolean downed,
      final boolean warded,
      final int blood,
      final boolean expected) {
    assertEquals(expected, BatFormRules.forcedOut(sun, downed, warded, blood));
  }

  @ParameterizedTest(name = "{0} at {1} -> {2}")
  @CsvSource({
    "garlic_strings, 1, true",
    "garlic_touch, 2, true",
    "holy_water, 1, true",
    "crucifix, 3, false",
    "starving, 2, false",
    "garlic_strings, 0, false"
  })
  @DisplayName("only garlic and holy water count as warding a bat out of its form")
  void warded(final String source, final int strength, final boolean expected) {
    assertEquals(expected, BatFormRules.warded(WeakenedSources.NONE.with(source, strength, 20)));
  }

  @ParameterizedTest(name = "{0} damage against {1} -> {2}")
  @CsvSource({"5.9, 6, false", "6.0, 6, true", "12.0, 6, true", "1.0, 1, true", "0.5, 1, false"})
  @DisplayName("a blow at or above the heavy damage setting forces a bat back")
  void heavyHit(final float damage, final int heavy, final boolean expected) {
    assertEquals(expected, BatFormRules.heavyHit(damage, heavy));
  }
}
