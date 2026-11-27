package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.guild.party.PartySchedule.Step;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PartyScheduleTest {

  @ParameterizedTest(name = "time {0}, pitched {1}, stay over {2}, tried {3}: {4}")
  @CsvSource({
    "6000, false, false, -1, NONE",
    "12000, false, false, -1, PITCH",
    "12000, false, true, -1, PITCH",
    "12000, false, false, 0, NONE",
    "36500, false, false, 0, PITCH",
    "18000, true, false, 0, SLEEP",
    "24500, true, false, 0, WAKE",
    "24500, true, true, 0, STRIKE",
    "18000, true, true, 0, STRIKE",
    "500000, true, true, 0, STRIKE"
  })
  void stepsThroughTheStay(
      final long time,
      final boolean pitched,
      final boolean stayOver,
      final long tried,
      final Step step) {
    assertEquals(step, PartySchedule.step(time, pitched, stayOver, tried));
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
