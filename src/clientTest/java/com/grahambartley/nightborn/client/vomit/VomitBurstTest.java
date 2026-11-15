package com.grahambartley.nightborn.client.vomit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.client.state.GoreLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VomitBurstTest {

  @ParameterizedTest(name = "{0}: splashes {1}")
  @CsvSource({"FULL, true", "REDUCED, false", "OFF, false"})
  @DisplayName("full gore splashes, reduced draws less and leaves no splash, off draws nothing")
  void followsGore(final GoreLevel gore, final boolean splashes) {
    assertEquals(splashes, VomitBurst.of(gore).splashes());
    assertTrue(VomitBurst.of(GoreLevel.FULL).drops() > VomitBurst.of(GoreLevel.REDUCED).drops());
    assertEquals(0, VomitBurst.of(GoreLevel.OFF).drops());
  }
}
