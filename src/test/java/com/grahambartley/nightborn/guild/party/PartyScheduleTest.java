package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.guild.party.PartySchedule.Step;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PartyScheduleTest {

  @ParameterizedTest(
      name = "time {0}, pitched {1}, camp day {2}, {3} nights, tried {4}, guarding {5}: {6}")
  @CsvSource({
    "6000, false, 0, 0, -1, false, NONE",
    "12000, false, 0, 0, -1, false, PITCH",
    "12000, false, 0, 0, 0, false, NONE",
    "18000, true, 0, 1, 0, false, SLEEP",
    "24500, true, 0, 1, 0, false, STRIKE",
    "24500, true, 0, 2, 0, false, WAKE",
    "42000, true, 0, 2, 0, false, SLEEP",
    "48500, true, 0, 2, 0, false, STRIKE",
    "500000, true, 0, 3, 0, false, STRIKE",
    "36500, false, 0, 0, 0, false, PITCH",
    "24500, true, 0, 1, 0, true, WAKE",
    "504500, true, 0, 3, 0, true, WAKE",
    "498000, true, 0, 3, 0, true, SLEEP",
    "12000, false, 0, 0, -1, true, PITCH"
  })
  void stepsThroughTheStay(
      final long time,
      final boolean pitched,
      final long campDay,
      final int nights,
      final long tried,
      final boolean guarding,
      final Step step) {
    assertEquals(step, PartySchedule.step(time, pitched, campDay, nights, tried, guarding));
  }

  @ParameterizedTest(
      name = "pitched {0}, guarding {1}, {2} ticks since a player was near, leaves {3}")
  @CsvSource({
    "false, false, 5999, false",
    "false, false, 6000, true",
    "true, false, 999999, false",
    "false, true, 999999, false"
  })
  void onlyAPartyOnTheMoveLeavesTheWorld(
      final boolean pitched, final boolean guarding, final long since, final boolean leaves) {
    assertEquals(leaves, PartySchedule.leaves(pitched, guarding, 10000 + since, 10000, 6000));
  }

  @ParameterizedTest(name = "time {0} is night {1}")
  @CsvSource({"12999, false", "13000, true", "22999, true", "23000, false", "37000, true"})
  void knowsTheNight(final long time, final boolean night) {
    assertEquals(night, PartySchedule.night(time));
  }
}
