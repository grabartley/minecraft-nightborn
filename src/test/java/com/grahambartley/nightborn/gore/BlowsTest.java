package com.grahambartley.nightborn.gore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BlowsTest {

  @ParameterizedTest(name = "stake {0}, never severs {1}, struck {2}, blast {3}: barred {4}")
  @CsvSource({
    "false, false, true, false, false",
    "false, false, false, true, false",
    "true, false, true, false, true",
    "false, true, true, false, true",
    "false, false, false, false, true",
    "true, true, false, true, true"
  })
  @DisplayName("stakes, listed damage and harm with no attacker or blast never sever")
  void barred(
      final boolean stake,
      final boolean neverSevers,
      final boolean struck,
      final boolean blast,
      final boolean barred) {
    assertEquals(barred, Blows.barred(stake, neverSevers, struck, blast));
  }
}
