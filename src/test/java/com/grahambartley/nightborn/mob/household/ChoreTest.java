package com.grahambartley.nightborn.mob.household;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ChoreTest {

  @ParameterizedTest(name = "{0}, lit {1}: {2}")
  @CsvSource({
    "GATHER, false, LIGHT",
    "GATHER, true, ",
    "WIND_DOWN, true, DOUSE",
    "WIND_DOWN, false, ",
    "PASTIME, false, ",
    "DAY, true, "
  })
  @DisplayName("candles are lit at dusk and put out before dawn")
  void light(final NightPhase phase, final boolean lit, final Chore chore) {
    assertEquals(Optional.ofNullable(chore), Chore.forLight(phase, lit));
  }

  @ParameterizedTest(name = "{0}, open {1}: {2}")
  @CsvSource({
    "GATHER, false, OPEN_DRAPES",
    "GATHER, true, ",
    "WIND_DOWN, true, CLOSE_DRAPES",
    "WIND_DOWN, false, ",
    "PASTIME, false, ",
    "DAY, true, "
  })
  @DisplayName("drapes are opened at dusk and drawn before dawn")
  void drape(final NightPhase phase, final boolean open, final Chore chore) {
    assertEquals(Optional.ofNullable(chore), Chore.forDrape(phase, open));
  }

  @ParameterizedTest(name = "{0}: lights {1}, opens {2}")
  @CsvSource({
    "LIGHT, true, false",
    "DOUSE, false, false",
    "OPEN_DRAPES, false, true",
    "CLOSE_DRAPES, false, false"
  })
  @DisplayName("each chore knows which way it turns its furniture")
  void direction(final Chore chore, final boolean lights, final boolean opens) {
    assertEquals(lights, chore.lights());
    assertEquals(opens, chore.opens());
  }
}
