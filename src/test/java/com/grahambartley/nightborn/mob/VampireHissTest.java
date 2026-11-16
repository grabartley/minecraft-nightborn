package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VampireHissTest {

  @ParameterizedTest(
      name = "client {0}, alive {1}, downed {2}, rising {3}, at {4} next {5}: hisses {6}")
  @CsvSource({
    "false, true, false, false, 100, 100, true",
    "false, true, false, false, 99, 100, false",
    "true, true, false, false, 100, 0, false",
    "false, false, false, false, 100, 0, false",
    "false, true, true, false, 100, 0, false",
    "false, true, false, true, 100, 0, false"
  })
  @DisplayName("only a living, standing vampire on the server hisses, and not before its wait")
  void mayHiss(
      final boolean client,
      final boolean alive,
      final boolean downed,
      final boolean rising,
      final long now,
      final long next,
      final boolean hisses) {
    assertEquals(hisses, VampireHiss.mayHiss(client, alive, downed, rising, now, next));
  }

  @ParameterizedTest(name = "warded now {0}, before {1}: hisses {2}")
  @CsvSource({
    "true, false, true",
    "true, true, false",
    "false, true, false",
    "false, false, false"
  })
  @DisplayName("a vampire hisses the moment garlic or a crucifix first wards it")
  void newlyWarded(final boolean now, final boolean before, final boolean hisses) {
    assertEquals(hisses, VampireHiss.newlyWarded(now, before));
  }
}
