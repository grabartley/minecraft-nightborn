package com.grahambartley.nightborn.guild.rite;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SanctuaryTest {

  @ParameterizedTest(name = "sheltered {0}, harm that passes all shelter {1} -> shielded {2}")
  @CsvSource({
    "true, false, true",
    "true, true, false",
    "false, false, false",
    "false, true, false"
  })
  @DisplayName("sanctuary stops every harm but the kind nothing stops")
  void shields(final boolean sheltered, final boolean bypasses, final boolean shielded) {
    assertEquals(shielded, Sanctuary.shields(sheltered, bypasses));
  }

  @ParameterizedTest(name = "attacker sheltered {0}, strikes another {1} -> shelter ends {2}")
  @CsvSource({
    "true, true, true",
    "true, false, false",
    "false, true, false",
    "false, false, false"
  })
  @DisplayName("striking anything else gives the shelter up")
  void forfeits(final boolean sheltered, final boolean strikesAnother, final boolean ends) {
    assertEquals(ends, Sanctuary.forfeits(sheltered, strikesAnother));
  }

  @ParameterizedTest(name = "{0} seconds of shelter last {1} ticks")
  @CsvSource({"0, 0", "1, 20", "3, 60", "30, 600", "-2, 0"})
  @DisplayName("the shelter lasts its setting in seconds, and none at all at zero")
  void ticks(final int seconds, final int ticks) {
    assertEquals(ticks, Sanctuary.ticks(seconds));
  }
}
