package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GuildRevengeGoalTest {

  @ParameterizedTest(name = "self {0}, guild {1}, busy {2}, teammate {3}: answers {4}")
  @CsvSource({
    "false, true, false, false, true",
    "true, true, false, false, false",
    "false, false, false, false, false",
    "false, true, true, false, false",
    "false, true, false, true, false"
  })
  @DisplayName("any idle guild fighter nearby answers a call for help, whatever its kind")
  void idleGuildFightersAnswer(
      final boolean self,
      final boolean guild,
      final boolean busy,
      final boolean teammate,
      final boolean answers) {
    assertEquals(answers, GuildRevengeGoal.answers(self, guild, busy, teammate));
  }
}
