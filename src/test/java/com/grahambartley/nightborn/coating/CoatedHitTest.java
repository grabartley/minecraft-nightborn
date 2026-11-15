package com.grahambartley.nightborn.coating;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CoatedHitTest {

  @ParameterizedTest(
      name = "vampire {0}, blocked {1}, coated ammo {2}, coated weapon {3}, weapon strike {4}: {5}")
  @CsvSource({
    "true, false, true, false, false, AMMO",
    "true, false, true, true, true, AMMO",
    "true, false, true, false, true, AMMO",
    "true, false, false, true, true, WEAPON",
    "true, false, false, true, false, NONE",
    "true, false, false, false, true, NONE",
    "true, true, true, true, true, NONE",
    "false, false, true, true, true, NONE"
  })
  @DisplayName(
      "a hit spends the coating on the ammo or the weapon that struck it, and only on vampires")
  void decides(
      final boolean vampire,
      final boolean blocked,
      final boolean ammo,
      final boolean weapon,
      final boolean strike,
      final CoatedHit hit) {
    assertEquals(hit, CoatedHit.of(vampire, blocked, ammo, weapon, strike));
  }
}
