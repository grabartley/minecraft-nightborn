package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.skill.SkillCurve;
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
  static final float FRESH_BLOOD_PER_NIGHT = 30.0F;
  static final float SMALL_TRIAL = 0.25F;
  static final float MEDIUM_TRIAL = 0.5F;

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
        new SkillSettings(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        new SkillSettings(0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1));
    assertEquals(
        new SkillSettings(
            100000, 100, 1000, 10000, 10000, 10000, 100, 10000, 1000, 1000, 1000, 40, 100, 60),
        new SkillSettings(
            100001, 101, 1001, 10001, 10001, 10001, 101, 10001, 1001, 1001, 1001, 41, 101, 61));
  }

  @ParameterizedTest(name = "Hunter {0}, Veteran {1}, Master {2}")
  @CsvSource({"5, 20, 45", "0, 0, 0", "10, 30, 90"})
  @DisplayName("the rank thresholds read Hunter, Veteran then Master")
  void rankNeedsInOrder(final int hunter, final int veteran, final int master) {
    final SkillSettings settings =
        new SkillSettings(100, 3, 5, 100, 300, 200, 10, 500, hunter, veteran, master, 2, 5, 3);
    assertEquals(List.of(hunter, veteran, master), settings.rankNeeds().points());
  }

  @ParameterizedTest(name = "{1} stakings of stage {0} pay {2} points")
  @CsvSource({
    "0, 1, 0",
    "0, 2, 1",
    "0, 5, 2",
    "0, 10, 4",
    "0, 50, 22",
    "1, 1, 1",
    "1, 5, 6",
    "1, 10, 11",
    "2, 1, 1",
    "2, 5, 9",
    "2, 10, 18",
    "3, 1, 2",
    "3, 5, 12",
    "3, 10, 24"
  })
  @DisplayName("the default curve pays the staking pace the PRD sets")
  void stakingPace(final int stage, final int stakings, final int points) {
    SkillCurve.Gain gain = new SkillCurve.Gain(0, 0.0F);
    for (int staking = 0; staking < stakings; staking++) {
      gain = add(gain, SkillEarning.huntExperience(DEFAULTS, stage));
    }
    assertEquals(points, gain.earned());
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
    SkillCurve.Gain gain = new SkillCurve.Gain(0, 0.0F);
    for (int night = 1; night <= nights; night++) {
      gain = add(gain, SkillEarning.bloodExperience(FRESH_BLOOD_PER_NIGHT, DEFAULTS));
      gain = add(gain, DEFAULTS.drainBonusExperience());
      if (night % 7 == 0) {
        gain = trials(gain);
      }
    }
    assertEquals(points, gain.earned());
  }

  static SkillCurve.Gain huntingWeeks(final int weeks) {
    SkillCurve.Gain gain = new SkillCurve.Gain(0, 0.0F);
    for (int week = 0; week < weeks; week++) {
      for (final int stage : new int[] {FLEDGLING, FLEDGLING, FLEDGLING, ADULT, ADULT, ADULT}) {
        gain = add(gain, SkillEarning.huntExperience(DEFAULTS, stage));
      }
      gain = add(gain, SkillEarning.huntExperience(DEFAULTS, ELDER));
      gain = add(gain, SkillEarning.huntExperience(DEFAULTS, ELDER));
      gain = add(gain, DEFAULTS.contractExperience());
      gain = add(gain, DEFAULTS.contractExperience());
      gain = trials(gain);
    }
    return gain;
  }

  static SkillCurve.Gain trials(final SkillCurve.Gain gain) {
    SkillCurve.Gain paid = gain;
    for (final float share : new float[] {SMALL_TRIAL, MEDIUM_TRIAL}) {
      paid =
          add(
              paid,
              SkillEarning.trialExperience(
                  paid.earned(), share, DEFAULTS.firstPointExperience(), DEFAULTS.pointCostRise()));
    }
    return paid;
  }

  static SkillCurve.Gain add(final SkillCurve.Gain gain, final float experience) {
    return SkillCurve.add(
        gain.earned(),
        gain.experience(),
        experience,
        DEFAULTS.firstPointExperience(),
        DEFAULTS.pointCostRise(),
        TreeProgress.MAX_POINTS);
  }
}
