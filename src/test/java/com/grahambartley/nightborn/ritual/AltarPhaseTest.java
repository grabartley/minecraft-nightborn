package com.grahambartley.nightborn.ritual;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AltarPhaseTest {

  @ParameterizedTest(name = "active={0} placed={1} -> {2}")
  @CsvSource({
    "false, 0, IDLE, idle, 0",
    "false, 1, RELICS_PLACED, relics_placed, 0",
    "false, 4, RELICS_PLACED, relics_placed, 0",
    "true, 4, RITUAL_ACTIVE, ritual_active, 10",
    "true, 0, RITUAL_ACTIVE, ritual_active, 10"
  })
  @DisplayName("the altar's phase follows its relics and its ritual, and names its clip")
  void of(
      final boolean active,
      final int placed,
      final AltarPhase phase,
      final String clip,
      final int light) {
    assertEquals(phase, AltarPhase.of(active, placed));
    assertEquals(clip, phase.clip());
    assertEquals(clip, phase.asString());
    assertEquals(light, phase.light());
  }
}
