package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VampireFeudTest {

  @ParameterizedTest(name = "vampire {0}, fighting {1} -> {2}")
  @CsvSource({"true, false, false", "true, true, true", "false, false, true", "false, true, true"})
  @DisplayName("a vampire mob keeps a vampire player from resting only while it fights them")
  void angryAt(final boolean vampire, final boolean fighting, final boolean expected) {
    assertEquals(expected, VampireFeud.angryAt(vampire, fighting));
  }

  @ParameterizedTest(name = "quarry {0}, vampire target {1}, struck by it {2} -> {3}")
  @CsvSource({
    "false, false, false, true",
    "false, true, false, false",
    "false, true, true, true",
    "true, true, false, true",
    "true, false, false, true"
  })
  @DisplayName("a vampire fights any human, any vampire that strikes it, and its nemesis quarry")
  void fights(
      final boolean quarry, final boolean vampire, final boolean struck, final boolean expected) {
    assertEquals(expected, VampireFeud.fights(quarry, vampire, struck));
  }
}
