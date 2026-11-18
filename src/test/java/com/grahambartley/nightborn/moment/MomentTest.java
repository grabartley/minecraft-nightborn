package com.grahambartley.nightborn.moment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MomentTest {

  @ParameterizedTest(name = "{0}, vampire viewer {1}, {2} blocks squared -> {3}")
  @CsvSource({
    "TURNING, true, 100, true",
    "TURNING, false, 100, false",
    "TURNING, true, 1025, false",
    "OATH, false, 100, true",
    "OATH, true, 1024, true",
    "OATH, false, 1025, false"
  })
  @DisplayName("players near see a ceremony, but a human never sees a turning")
  void seenBy(
      final Moment moment, final boolean vampire, final double distanceSq, final boolean seen) {
    assertEquals(seen, moment.seenBy(vampire, distanceSq, 32));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"TURNING, nightborn.moment.vampire", "OATH, nightborn.moment.hunter"})
  @DisplayName("the card names what the player has become")
  void cardKey(final Moment moment, final String key) {
    assertEquals(key, moment.cardKey());
  }
}
