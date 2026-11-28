package com.grahambartley.nightborn.client.gore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.client.state.GoreLevel;
import net.minecraft.client.option.ParticlesMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WoundPulseTest {

  @ParameterizedTest(name = "gore {0}, particles {1}, {2} ticks after the cut: {3} drops, {4}")
  @CsvSource({
    "FULL, ALL, 0, 4, FRESH",
    "FULL, ALL, 16, 4, FRESH",
    "FULL, ALL, 17, 0, FRESH",
    "FULL, DECREASED, 32, 2, FRESH",
    "REDUCED, ALL, 48, 2, DRIED",
    "REDUCED, DECREASED, 64, 1, DRIED",
    "OFF, ALL, 16, 0, HIDDEN",
    "FULL, MINIMAL, 16, 0, FRESH",
    "FULL, ALL, -16, 0, FRESH"
  })
  @DisplayName("a wound beats out drops on its pulse, fewer and darker as gore is turned down")
  void beats(
      final GoreLevel gore,
      final ParticlesMode particles,
      final long sinceCut,
      final int drops,
      final SplatterLook look) {
    assertEquals(new WoundPulse(drops, look), WoundPulse.of(gore, particles, sinceCut));
  }

  @ParameterizedTest(name = "{0} ticks after the cut: drip {1}")
  @CsvSource({"0, false", "39, false", "40, true", "80, true", "81, false"})
  @DisplayName("a bleeding body drips on a slow, steady beat after the cut")
  void drips(final long sinceCut, final boolean drip) {
    assertEquals(drip, WoundPulse.drips(sinceCut));
  }

  @ParameterizedTest(name = "{0} spent, {1} wanted: {2} allowed")
  @CsvSource({"0, 4, 4", "94, 4, 2", "96, 4, 0", "200, 4, 0"})
  @DisplayName("a crowd of wounds shares one drop budget a tick")
  void allowed(final int spent, final int drops, final int allowed) {
    assertEquals(allowed, WoundPulse.allowed(spent, drops));
  }

  @ParameterizedTest(name = "invisible {0}, own first person view {1}: shown {2}")
  @CsvSource({
    "false, false, true",
    "true, false, false",
    "false, true, false",
    "true, true, false"
  })
  @DisplayName("wound blood is not drawn on a hidden body or at the bleeding player's own camera")
  void shown(final boolean invisible, final boolean ownFirstPerson, final boolean shown) {
    assertEquals(shown, WoundPulse.shown(invisible, ownFirstPerson));
  }
}
