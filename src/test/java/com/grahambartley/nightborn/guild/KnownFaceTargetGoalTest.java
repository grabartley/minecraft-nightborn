package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class KnownFaceTargetGoalTest {

  @ParameterizedTest(name = "known in the guarded village {0}, vampire {1}: hunted first {2}")
  @CsvSource({
    "true, true, true",
    "true, false, false",
    "false, true, false",
    "false, false, false"
  })
  @DisplayName("guards go first for a known face who is still a vampire")
  void hunts(final boolean known, final boolean vampire, final boolean hunts) {
    assertEquals(hunts, KnownFaceTargetGoal.hunts(known, vampire));
  }
}
