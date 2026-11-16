package com.grahambartley.nightborn.mind;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CharmTest {

  @ParameterizedTest(name = "mesmerised {0}, charmed {1}, charm {2}, sneaking {3} -> {4}")
  @CsvSource({
    "true, false, 1, false, true",
    "true, false, 3, false, true",
    "false, false, 1, false, false",
    "true, true, 1, false, false",
    "true, false, 0, false, false",
    "true, false, 1, true, false"
  })
  @DisplayName("talking to a villager you mesmerised charms it once you know how")
  void charms(
      final boolean mesmerised,
      final boolean charmed,
      final int charm,
      final boolean sneaking,
      final boolean expected) {
    assertEquals(expected, Charm.charms(mesmerised, charmed, charm, sneaking));
  }
}
