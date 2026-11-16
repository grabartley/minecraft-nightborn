package com.grahambartley.nightborn.mob.household;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HouseholdCalmTest {

  @ParameterizedTest(
      name = "sleeping {0}, hunting {1}, downed {2}, bat {3}, out {4}, bedtime {5}: {6}")
  @CsvSource({
    "false, false, false, false, false, false, true",
    "true, false, false, false, false, false, false",
    "false, true, false, false, false, false, false",
    "false, false, true, false, false, false, false",
    "false, false, false, true, false, false, false",
    "false, false, false, false, true, false, false",
    "false, false, false, false, false, true, false"
  })
  @DisplayName(
      "household life waits while a vampire sleeps, fights, is downed, flies, feeds or heads to bed")
  void calm(
      final boolean sleeping,
      final boolean hunting,
      final boolean downed,
      final boolean bat,
      final boolean out,
      final boolean bedtime,
      final boolean calm) {
    assertEquals(calm, HouseholdCalm.calm(sleeping, hunting, downed, bat, out, bedtime));
  }

  @ParameterizedTest(name = "calm {0}, busy {1}, grieving {2}: {3}")
  @CsvSource({
    "true, false, false, true",
    "false, false, false, false",
    "true, true, false, false",
    "true, false, true, false"
  })
  @DisplayName("only a calm, idle housemate can be drawn into a conversation")
  void free(final boolean calm, final boolean busy, final boolean grieving, final boolean free) {
    assertEquals(free, HouseholdCalm.free(calm, busy, grieving));
  }
}
