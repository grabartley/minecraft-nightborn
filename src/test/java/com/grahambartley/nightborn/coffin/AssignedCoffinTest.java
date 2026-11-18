package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AssignedCoffinTest {

  @ParameterizedTest(name = "spawn point {0}, same world {1}, coffin there {2} -> {3}")
  @CsvSource({
    "true, true, true, true",
    "false, true, true, false",
    "true, false, true, false",
    "true, true, false, false"
  })
  @DisplayName("a player's coffin is their spawn point, in this world, while a coffin stands there")
  void counts(
      final boolean spawn, final boolean sameWorld, final boolean coffin, final boolean counts) {
    assertEquals(counts, AssignedCoffin.counts(spawn, sameWorld, coffin));
  }
}
