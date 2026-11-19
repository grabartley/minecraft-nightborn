package com.grahambartley.nightborn.seeker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VeilsTest {

  @ParameterizedTest(name = "veil {0}, mist {1}, glamour {2} -> {3}")
  @CsvSource({
    "false, false, false, false",
    "true, false, false, true",
    "false, true, false, true",
    "false, false, true, true",
    "true, true, true, true"
  })
  @DisplayName("Shadow Veil, Mist Form and Glamour each hide a vampire from a mark")
  void veils(final boolean veil, final boolean mist, final boolean glamour, final boolean hides) {
    assertEquals(hides, Veils.veils(veil, mist, glamour));
  }

  @ParameterizedTest(name = "veiled {0}, vanished {1} -> hidden {2}")
  @CsvSource({"false, false, false", "true, false, true", "false, true, true", "true, true, true"})
  @DisplayName("a vampire is hidden by a veil or by Vanish")
  void hides(final boolean veiled, final boolean vanished, final boolean hidden) {
    assertEquals(hidden, Veils.hides(veiled, vanished));
  }
}
