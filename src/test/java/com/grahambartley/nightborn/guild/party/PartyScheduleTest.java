package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.guild.party.PartySchedule.Step;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PartyScheduleTest {

  @ParameterizedTest(name = "time {0}, pitched {1}, camp day {2}, {3} nights, tried {4}: {5}")
  @CsvSource({
    "6000, false, 0, 0, -1, NONE",
    "12000, false, 0, 0, -1, PITCH",
    "12000, false, 0, 0, 0, NONE",
    "18000, true, 0, 1, 0, SLEEP",
    "24500, true, 0, 1, 0, STRIKE",
    "24500, true, 0, 2, 0, WAKE",
    "42000, true, 0, 2, 0, SLEEP",
    "48500, true, 0, 2, 0, STRIKE",
    "500000, true, 0, 3, 0, STRIKE",
    "36500, false, 0, 0, 0, PITCH"
  })
  void stepsThroughTheStay(
      final long time,
      final boolean pitched,
      final long campDay,
      final int nights,
      final long tried,
      final Step step) {
    assertEquals(step, PartySchedule.step(time, pitched, campDay, nights, tried));
  }

  @ParameterizedTest(name = "pitched {0}, {1} ticks since a player was near, leaves {2}")
  @CsvSource({"false, 5999, false", "false, 6000, true", "true, 999999, false"})
  void onlyAPartyOnTheMoveLeavesTheWorld(
      final boolean pitched, final long since, final boolean leaves) {
    assertEquals(leaves, PartySchedule.leaves(pitched, 10000 + since, 10000, 6000));
  }

  @ParameterizedTest(name = "time {0} is night {1}")
  @CsvSource({"12999, false", "13000, true", "22999, true", "23000, false", "37000, true"})
  void knowsTheNight(final long time, final boolean night) {
    assertEquals(night, PartySchedule.night(time));
  }
}
