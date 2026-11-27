package com.grahambartley.nightborn.fight;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BlowReadingTest {

  @ParameterizedTest(
      name = "never severs {0}, bolt {1}, fist {2}, struck {3}, blast {4}: barred {5}")
  @CsvSource({
    "false, false, false, true, false, false",
    "false, false, false, false, true, false",
    "true, false, false, true, false, true",
    "false, true, false, true, false, true",
    "false, false, true, true, false, true",
    "false, false, false, false, false, true",
    "true, false, false, false, true, true"
  })
  @DisplayName("stakes, listed damage, bolts, fists and harm with no attacker or blast never sever")
  void barred(
      final boolean neverSevers,
      final boolean bolt,
      final boolean fist,
      final boolean struck,
      final boolean blast,
      final boolean barred) {
    assertEquals(barred, BlowReading.barred(neverSevers, bolt, fist, struck, blast));
  }
}
