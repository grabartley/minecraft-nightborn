package com.grahambartley.nightborn.village;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VillageGuardTest {

  @ParameterizedTest(name = "{0}, guarded {1}, sent tonight {2}, spawning {3}: sends {4}")
  @CsvSource({
    "HUNTED, false, false, true, true",
    "HUNTED, true, false, true, false",
    "HUNTED, false, true, true, false",
    "HUNTED, false, false, false, false",
    "WARDED, false, false, true, false",
    "CALM, false, false, true, false"
  })
  @DisplayName("a hunted village sends for the guild once a night while nobody guards it")
  void sendsForTheGuild(
      final VillageStep step,
      final boolean guarded,
      final boolean sent,
      final boolean spawning,
      final boolean sends) {
    assertEquals(sends, VillageGuard.sends(step, guarded, sent, spawning));
  }
}
