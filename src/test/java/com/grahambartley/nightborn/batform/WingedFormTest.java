package com.grahambartley.nightborn.batform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WingedFormTest {

  private static Optional<WingedState> form(final boolean winged) {
    return winged
        ? Optional.of(new WingedState(0L, 400L, 0.5F, 3.0F, Optional.empty(), List.of()))
        : Optional.empty();
  }

  @ParameterizedTest(name = "winged {0}: scale {1}")
  @CsvSource({"true, 3.0", "false, 1.0"})
  @DisplayName("a winged form grows by its scale and anything else keeps its size")
  void scale(final boolean winged, final float scale) {
    assertEquals(scale, WingedForm.scale(form(winged)));
  }

  @ParameterizedTest(name = "winged {0} at {1}: takes {2}")
  @CsvSource({"true, 100, 0.5", "true, 400, 1.0", "false, 100, 1.0"})
  @DisplayName("a winged form cuts damage only until it ends")
  void damageTaken(final boolean winged, final long now, final float taken) {
    assertEquals(taken, WingedForm.damageTaken(form(winged), now));
  }

  @ParameterizedTest(name = "listed {0}, player {1}, vampire {2}: hears {3}")
  @CsvSource({
    "true, false, false, true",
    "false, true, false, true",
    "false, true, true, false",
    "false, false, false, false"
  })
  @DisplayName("the roar staggers listed mobs and human players, never another vampire player")
  void hears(
      final boolean listed, final boolean player, final boolean vampire, final boolean hears) {
    assertEquals(hears, WingedForm.hears(listed, player, vampire));
  }
}
