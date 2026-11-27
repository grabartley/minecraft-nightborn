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
}
