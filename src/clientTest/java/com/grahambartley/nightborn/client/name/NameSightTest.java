package com.grahambartley.nightborn.client.name;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NameSightTest {

  @ParameterizedTest(name = "shown={0} bearer={1} visible={2} inSight={3}: {4}")
  @CsvSource({
    "true, true, true, true, true",
    "true, true, true, false, false",
    "true, true, false, true, false",
    "false, true, true, true, false",
    "true, false, false, false, true",
    "false, false, true, true, false"
  })
  @DisplayName(
      "a bearer's name shows only in clear sight and never while invisible; others keep vanilla's call")
  void shows(
      final boolean shown,
      final boolean bearer,
      final boolean visible,
      final boolean inSight,
      final boolean expected) {
    assertEquals(expected, NameSight.shows(shown, bearer, visible, inSight));
  }

  @ParameterizedTest(name = "{0} blocks, range {1}: shows {2}")
  @CsvSource({"4.0, 8.0, true", "8.0, 8.0, true", "8.5, 8.0, false", "60.0, 0.0, true"})
  @DisplayName("a name kept close shows only within its range, and every other name as before")
  void withinRange(final double distance, final double range, final boolean shows) {
    assertEquals(shows, NameSight.withinRange(distance * distance, range));
  }
}
