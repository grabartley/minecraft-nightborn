package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BossWardTest {

  @ParameterizedTest(name = "tagged as a keeper when loaded: {0}")
  @ValueSource(booleans = {true, false})
  @DisplayName("a keeper loaded from a save has no ward left to keep, so it goes")
  void orphaned(final boolean tagged) {
    assertEquals(tagged, BossWard.orphaned(tagged));
  }
}
