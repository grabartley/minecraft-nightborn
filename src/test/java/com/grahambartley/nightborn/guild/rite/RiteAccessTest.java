package com.grahambartley.nightborn.guild.rite;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RiteAccessTest {

  @ParameterizedTest(name = "vampire {0}, hunter {1}, oath needed {2} -> offered {3}")
  @CsvSource({
    "false, true, true, true",
    "false, true, false, true",
    "false, false, true, false",
    "false, false, false, true",
    "true, false, true, false",
    "true, false, false, false",
    "true, true, true, false",
    "true, true, false, false"
  })
  @DisplayName(
      "the rite is for sworn hunters, for any human where the oath is waived, never a vampire")
  void offered(
      final boolean vampire, final boolean hunter, final boolean needsOath, final boolean offered) {
    assertEquals(offered, RiteAccess.offered(vampire, hunter, needsOath));
  }

  @ParameterizedTest(name = "now {0}, ready at {1} -> wait {2}s")
  @CsvSource({
    "0, 0, 0",
    "500, 0, 0",
    "500, 500, 0",
    "500, 499, 0",
    "500, 501, 1",
    "500, 520, 1",
    "500, 521, 2",
    "0, 12000, 600"
  })
  @DisplayName("a hunter waits in whole seconds, rounded up, and not at all once ready")
  void waitSeconds(final long now, final long readyAt, final long seconds) {
    assertEquals(seconds, RiteAccess.waitSeconds(now, readyAt));
  }

  @ParameterizedTest(name = "now {0} with a {1}s wait -> ready at {2}")
  @CsvSource({"0, 600, 12000", "100, 0, 100", "100, 1, 120", "24000, 7200, 168000"})
  @DisplayName("the next rite is ready a cooldown after this one")
  void readyAt(final long now, final int cooldown, final long readyAt) {
    assertEquals(readyAt, RiteAccess.readyAt(now, cooldown));
    assertEquals(cooldown, RiteAccess.waitSeconds(now, readyAt));
  }

  @ParameterizedTest(name = "sworn {0}, rank {1}, {2} emeralds, wait {3}s -> {4}")
  @CsvSource({
    "true, 1, 16, 0, NONE",
    "true, 3, 64, 0, NONE",
    "true, 0, 64, 0, RANK",
    "true, 0, 0, 600, RANK",
    "true, 1, 15, 0, PRICE",
    "true, 1, 0, 0, PRICE",
    "true, 1, 16, 1, WAIT",
    "true, 1, 0, 30, WAIT",
    "false, 0, 16, 0, NONE",
    "false, 0, 15, 0, PRICE",
    "false, 0, 16, 5, WAIT"
  })
  @DisplayName("a hunter needs the rank, then the wait to be over, then the tithe in hand")
  void refusal(
      final boolean sworn,
      final int rank,
      final int emeralds,
      final long wait,
      final RiteAccess.Refusal refusal) {
    assertEquals(
        refusal,
        RiteAccess.refusal(
            new RiteAccess.Standing(sworn, rank, emeralds, wait), new RiteAccess.Terms(1, 16)));
  }

  @ParameterizedTest(name = "a free rite at rank {0} with {1} emeralds -> {2}")
  @CsvSource({"0, 0, NONE", "3, 0, NONE"})
  @DisplayName("with no rank and no price asked, nothing is refused")
  void freeRite(final int rank, final int emeralds, final RiteAccess.Refusal refusal) {
    assertEquals(
        refusal,
        RiteAccess.refusal(
            new RiteAccess.Standing(true, rank, emeralds, 0), new RiteAccess.Terms(0, 0)));
  }

  @ParameterizedTest(name = "{0}s reads {1}")
  @CsvSource({
    "0, 0:00",
    "5, 0:05",
    "59, 0:59",
    "60, 1:00",
    "600, 10:00",
    "1199, 19:59",
    "7200, 120:00"
  })
  @DisplayName("the wait is told in minutes and seconds")
  void clock(final long seconds, final String told) {
    assertEquals(told, RiteAccess.clock(seconds));
  }
}
