package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MobLevitationTest {

  @ParameterizedTest(name = "levitating {0}, fighting {1}, roll {2}: {3}")
  @CsvSource({
    "false, true, 0.9, true",
    "true, true, 0.0, true",
    "false, false, 0.0, true",
    "false, false, 0.5, false",
    "true, false, 0.0, false",
    "true, false, 0.5, true"
  })
  @DisplayName("a vampire levitates in a fight, and now and then as it moves about")
  void wants(
      final boolean levitating, final boolean fighting, final float roll, final boolean wants) {
    assertEquals(wants, MobLevitation.wants(levitating, fighting, roll));
  }
}
