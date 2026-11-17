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
}
