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
    assertEquals(
        barred, BlowReading.barred(Cutting.WEAPON, false, neverSevers, bolt, fist, struck, blast));
  }

  @ParameterizedTest(name = "{0}, marked {1}, blast {2}: barred {3}")
  @CsvSource({
    "MARKED, false, false, true",
    "MARKED, true, false, false",
    "MARKED, false, true, true",
    "ALL, false, false, false",
    "WEAPON, false, false, false",
    "WEAPON, true, false, false"
  })
  @DisplayName("a mob that cuts only with marked attacks never severs with any other blow")
  void barredByCutting(
      final Cutting cutting, final boolean marked, final boolean blast, final boolean barred) {
    assertEquals(barred, BlowReading.barred(cutting, marked, false, false, false, !blast, blast));
  }
}
