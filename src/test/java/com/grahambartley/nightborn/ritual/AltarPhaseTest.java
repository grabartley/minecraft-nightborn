package com.grahambartley.nightborn.ritual;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AltarPhaseTest {

  @ParameterizedTest(name = "active={0} ready={1} -> {2}")
  @CsvSource({
    "false, false, IDLE, idle, 0",
    "false, true, READY, relics_placed, 0",
    "true, true, RITUAL_ACTIVE, ritual_active, 10",
    "true, false, RITUAL_ACTIVE, ritual_active, 10"
  })
  @DisplayName("the altar's phase follows its full chalice and its ritual, and names its clip")
  void of(
      final boolean active,
      final boolean ready,
      final AltarPhase phase,
      final String clip,
      final int light) {
    assertEquals(phase, AltarPhase.of(active, ready));
    assertEquals(clip, phase.clip());
    assertEquals(clip, phase.asString());
    assertEquals(light, phase.light());
  }
}
