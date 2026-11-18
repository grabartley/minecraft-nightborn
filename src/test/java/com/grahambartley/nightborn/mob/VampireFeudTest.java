package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VampireFeudTest {

  @ParameterizedTest(name = "struck {0}, striker {1}, own {2}, answers all {3} -> {4}")
  @CsvSource({
    "true, 3, 3, false, true",
    "true, 5, 3, false, true",
    "true, 2, 3, false, false",
    "false, 9, 3, false, false",
    "true, 0, 9, true, true",
    "false, 0, 9, true, false"
  })
  @DisplayName("a vampire fights back against one of its own stage or older, a lord against any")
  void answers(
      final boolean struck,
      final int striker,
      final int own,
      final boolean everyStriker,
      final boolean expected) {
    assertEquals(expected, VampireFeud.answers(struck, striker, own, everyStriker));
  }

  @ParameterizedTest(name = "vampire {0}, fighting {1} -> {2}")
  @CsvSource({"true, false, false", "true, true, true", "false, false, true", "false, true, true"})
  @DisplayName("a vampire mob keeps a vampire player from resting only while it fights them")
  void angryAt(final boolean vampire, final boolean fighting, final boolean expected) {
    assertEquals(expected, VampireFeud.angryAt(vampire, fighting));
  }
}
