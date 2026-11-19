package com.grahambartley.nightborn.raid;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TrailRulesTest {

  @ParameterizedTest(name = "wounded at world tick {0}, time of day {1}: lasts until {2}")
  @CsvSource({
    "1000, 14000, 10000",
    "50000, 22999, 50001",
    "50000, 23000, 74000",
    "0, 0, 23000",
    "100, 48100, 23000",
    "5000, 18000, 10000"
  })
  @DisplayName("a trail runs out at the next dawn")
  void untilDawn(final long time, final long timeOfDay, final long until) {
    assertEquals(until, TrailRules.until(time, timeOfDay));
  }

  @ParameterizedTest(name = "now {0}, until {1}: over {2}")
  @CsvSource({"9999, 10000, false", "10000, 10000, true", "12000, 10000, true"})
  @DisplayName("the trail is gone from dawn on")
  void over(final long now, final long until, final boolean over) {
    assertEquals(over, TrailRules.over(now, until));
  }

  @ParameterizedTest(name = "ends at death {0}, quarry died {1} -> ends {2}")
  @CsvSource({
    "true, true, true",
    "true, false, false",
    "false, true, false",
    "false, false, false"
  })
  @DisplayName("a marked quarry's trail ends at its death, and a wounded vampire's leads home")
  void endsWith(final boolean endsAtDeath, final boolean died, final boolean ends) {
    assertEquals(ends, TrailRules.endsWith(endsAtDeath, died));
  }
}
