package com.grahambartley.nightborn.client.sun;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ParasolRendererTest {

  @ParameterizedTest(name = "{0} bone, open {1}: hidden {2}")
  @CsvSource({
    "open, true, false",
    "open, false, true",
    "closed, true, true",
    "closed, false, false",
    "cane, true, false",
    "cane, false, false"
  })
  @DisplayName("an open parasol shows its canopy and a closed one its furled cloth")
  void hides(final String bone, final boolean open, final boolean hidden) {
    assertEquals(hidden, ParasolRenderer.hides(bone, open));
  }
}
