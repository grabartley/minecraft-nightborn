package com.grahambartley.nightborn.village;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VillageGuardTest {

  @ParameterizedTest(name = "guarded {0}, sent tonight {1}, spawning {2}: sends {3}")
  @CsvSource({
    "false, false, true, true",
    "true, false, true, false",
    "false, true, true, false",
    "false, false, false, false"
  })
  @DisplayName("a hunted village sends for the guild once a night while nobody guards it")
  void sendsForTheGuild(
      final boolean guarded, final boolean sent, final boolean spawning, final boolean sends) {
    assertEquals(sends, VillageGuard.sends(guarded, sent, spawning));
  }

  @ParameterizedTest(name = "bell loaded {0}, standing {1}, {2}: keeps guarding {3}")
  @CsvSource({
    "true, true, HUNTED, true",
    "true, true, WARDED, false",
    "true, true, CALM, false",
    "true, false, HUNTED, false",
    "false, false, CALM, true"
  })
  @DisplayName("a party guards only while its village stands and is hunted")
  void keepsGuarding(
      final boolean loaded, final boolean stands, final VillageStep step, final boolean keeps) {
    assertEquals(keeps, VillageGuard.keepsGuarding(loaded, stands, step));
  }
}
