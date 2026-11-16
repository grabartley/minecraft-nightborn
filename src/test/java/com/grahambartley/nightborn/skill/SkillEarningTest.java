package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.config.SkillSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SkillEarningTest {

  @ParameterizedTest(name = "{0} per kill, {1} per stage, rank {2}: {3}")
  @CsvSource({
    "3, 2, 0, 3",
    "3, 2, 1, 5",
    "3, 2, 3, 9",
    "3, 0, 3, 3",
    "0, 2, 2, 4",
    "0, 0, 3, 0",
    "3, 2, -1, 3"
  })
  @DisplayName("an older vampire is worth more hunter points")
  void huntPoints(final int perKill, final int perStage, final int rank, final int points) {
    assertEquals(
        points, SkillEarning.huntPoints(new SkillSettings(20, 1, perKill, perStage, 10, 5), rank));
  }
}
