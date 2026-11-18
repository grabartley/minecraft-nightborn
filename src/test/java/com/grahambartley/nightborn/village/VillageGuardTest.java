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
}
