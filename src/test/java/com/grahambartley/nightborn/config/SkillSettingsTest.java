package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.blood.BloodValue;
import com.grahambartley.nightborn.challenge.TrialSize;
import com.grahambartley.nightborn.skill.SkillEarning;
import com.grahambartley.nightborn.skill.TreeProgress;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class SkillSettingsTest {

  static final SkillSettings DEFAULTS = NightbornConfig.DEFAULTS.skills();
  static final int FLEDGLING = 0;
  static final int ADULT = 1;
  static final int ELDER = 2;
  static final float BLOOD_TAKEN_PER_NIGHT = 30.0F;

  static Stream<Setting<?>> settings() {
    return SkillSettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every setting in the section belongs to it and reads its default")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(SkillSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @Test
  @DisplayName("the constructor clamps values into bounds")
  void clamps() {
    assertEquals(
        new SkillSettings(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        new SkillSettings(0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1));
    assertEquals(
        new SkillSettings(100000, 100, 1000, 10000, 10000, 10000, 100, 10000, 1000, 1000, 1000, 60),
        new SkillSettings(
            100001, 101, 1001, 10001, 10001, 10001, 101, 10001, 1001, 1001, 1001, 61));
  }

  @ParameterizedTest(name = "Hunter {0}, Veteran {1}, Master {2}")
  @CsvSource({"5, 20, 45", "0, 0, 0", "10, 30, 90"})
  @DisplayName("the rank thresholds read Hunter, Veteran then Master")
  void rankNeedsInOrder(final int hunter, final int veteran, final int master) {
    final SkillSettings settings =
        new SkillSettings(100, 3, 5, 100, 300, 200, 10, 500, hunter, veteran, master, 3);
    assertEquals(List.of(hunter, veteran, master), settings.rankNeeds().points());
  }

  @ParameterizedTest(name = "{1} stakings of stage {0} pay {2} points")
  @CsvSource({"0, 1, 0", "0, 2, 1", "0, 5, 2", "1, 1, 1", "2, 1, 1", "3, 1, 2"})
  @DisplayName("the default curve pays the staking pace the PRD sets")
  void stakingPace(final int stage, final int stakings, final int points) {
    TreeProgress progress = TreeProgress.EMPTY;
    for (int staking = 0; staking < stakings; staking++) {
      progress = add(progress, SkillEarning.huntExperience(DEFAULTS, stage));
    }
    assertEquals(points, progress.earned());
  }

  @ParameterizedTest(name = "week {0}: {1} points")
  @CsvSource({"1, 11", "5, 49", "10, 83", "13, 100", "15, 110"})
  @DisplayName("an ordinary hunting week fills the core in five weeks and the build in fifteen")
  void huntingWeeks(final int weeks, final int points) {
    assertEquals(points, huntingWeeks(weeks).earned());
  }

  @ParameterizedTest(name = "rank {0} arrives in week {1}")
  @CsvSource({"1, 1", "2, 5", "3, 13"})
  @DisplayName("each guild rank arrives in step with the points of an ordinary hunting week")
  void ranksInStep(final int rank, final int week) {
    final int needs = DEFAULTS.rankNeeds().pointsFor(rank).orElseThrow();
    assertEquals(
        week,
        IntStream.rangeClosed(1, 52)
            .filter(weeks -> huntingWeeks(weeks).earned() >= needs)
            .findFirst()
            .orElseThrow());
  }

  @ParameterizedTest(name = "night {0}: {1} points")
  @CsvSource({"1, 1", "7, 12", "35, 50", "70, 84", "100, 107"})
  @DisplayName("an ordinary feeding night fills a vampire's build in about a hundred nights")
  void feedingNights(final int nights, final int points) {
    final float worth =
        BloodValue.worth(
            BLOOD_TAKEN_PER_NIGHT,
            BloodQuality.VILLAGER.worthPercent(NightbornConfig.DEFAULTS),
            BloodValue.FRESH_PERCENT);
    TreeProgress progress = TreeProgress.EMPTY;
    for (int night = 1; night <= nights; night++) {
      progress = add(progress, SkillEarning.bloodExperience(worth, DEFAULTS));
      progress = add(progress, DEFAULTS.drainBonusExperience());
      if (night % 7 == 0) {
        progress = trials(progress);
      }
    }
    assertEquals(points, progress.earned());
  }

  static TreeProgress huntingWeeks(final int weeks) {
    TreeProgress progress = TreeProgress.EMPTY;
    for (int week = 0; week < weeks; week++) {
      for (final int stage :
          new int[] {FLEDGLING, FLEDGLING, FLEDGLING, ADULT, ADULT, ADULT, ELDER, ELDER}) {
        progress = add(progress, SkillEarning.huntExperience(DEFAULTS, stage));
      }
      progress = add(progress, DEFAULTS.contractExperience());
      progress = add(progress, DEFAULTS.contractExperience());
      progress = trials(progress);
    }
    return progress;
  }

  static TreeProgress trials(final TreeProgress progress) {
    TreeProgress paid = progress;
    for (final TrialSize size : new TrialSize[] {TrialSize.SMALL, TrialSize.MEDIUM}) {
      paid =
          add(
              paid,
              SkillEarning.trialExperience(
                  paid.earned(),
                  size.share(),
                  DEFAULTS.firstPointExperience(),
                  DEFAULTS.pointCostRise()));
    }
    return paid;
  }

  static TreeProgress add(final TreeProgress progress, final float experience) {
    return progress.addExperience(
        experience, DEFAULTS.firstPointExperience(), DEFAULTS.pointCostRise());
  }
}
