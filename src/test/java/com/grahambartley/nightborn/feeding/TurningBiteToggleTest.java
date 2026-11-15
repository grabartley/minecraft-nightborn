package com.grahambartley.nightborn.feeding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TurningBiteToggleTest {

  @ParameterizedTest(name = "on {0}: {1}")
  @CsvSource({"true, nightborn.turning_bite.on", "false, nightborn.turning_bite.off"})
  @DisplayName("each switch is confirmed with its own line")
  void messages(final boolean on, final String key) {
    assertEquals(key, TurningBiteToggle.messageKey(on));
  }
}
