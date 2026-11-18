package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.config.SkillSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SkillEarningTest {

  static SkillSettings settings(final int perBlood, final int perKill, final int perStage) {
    return new SkillSettings(100, 3, perBlood, 100, perKill, perStage, 10, 500, 5, 20, 45, 2, 5);
  }

  @ParameterizedTest(name = "{0} per kill, {1} per stage, rank {2}: {3}")
  @CsvSource({
    "300, 200, 0, 300",
    "300, 200, 1, 500",
    "300, 200, 3, 900",
    "300, 0, 3, 300",
    "0, 200, 2, 400",
    "0, 0, 3, 0",
    "300, 200, -1, 300"
  })
  @DisplayName("an older vampire is worth more hunter experience")
  void huntExperience(final int perKill, final int perStage, final int rank, final int experience) {
    assertEquals(experience, SkillEarning.huntExperience(settings(5, perKill, perStage), rank));
  }

  @ParameterizedTest(name = "sworn {0}, {1} experience -> paid {2}")
  @CsvSource({"true, 300, true", "true, 0, false", "true, -5, false", "false, 300, false"})
  @DisplayName("only a sworn hunter earns hunter experience, and only when there is some to earn")
  void paysHunter(final boolean sworn, final int experience, final boolean paid) {
    assertEquals(paid, SkillEarning.paysHunter(sworn, experience));
  }

  @ParameterizedTest(name = "{0} blood at {1} each: {2}")
  @CsvSource({"1, 5, 5", "0.5, 5, 2.5", "4, 0, 0", "0, 5, 0", "-2, 5, 0", "20, 5, 100"})
  @DisplayName("blood pays experience by its worth, and never less than nothing")
  void bloodExperience(final float worth, final int perBlood, final float experience) {
    assertEquals(
        experience, SkillEarning.bloodExperience(worth, settings(perBlood, 300, 200)), 0.0001F);
  }
}
