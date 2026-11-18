package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.OptionalLong;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ChallengeClockTest {

  @ParameterizedTest(name = "time {0} every {1} days is period {2}")
  @CsvSource({"0, 7, 0", "167999, 7, 0", "168000, 7, 1", "48000, 1, 2", "100, 0, 0"})
  void countsWholeSpansOfDays(final long time, final int days, final long period) {
    assertEquals(period, ChallengeClock.period(time, days));
  }

  @ParameterizedTest(name = "time {0} is night {1}")
  @CsvSource({"12999, -1", "13000, 0", "22999, 0", "23000, -1", "37000, 1", "6000, -1"})
  void namesTheNightOnlyAtNight(final long time, final long night) {
    assertEquals(
        night < 0 ? OptionalLong.empty() : OptionalLong.of(night), ChallengeClock.night(time));
  }
}
