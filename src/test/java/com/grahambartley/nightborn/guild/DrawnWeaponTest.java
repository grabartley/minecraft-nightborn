package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DrawnWeaponTest {

  @ParameterizedTest(name = "hunting {0} ticks then idle {1} ticks: drawn {2}")
  @CsvSource({
    "0, 0, false",
    "0, 50, false",
    "1, 0, true",
    "1, 99, true",
    "1, 100, false",
    "40, 99, true",
    "40, 100, false",
    "40, 500, false"
  })
  void staysDrawnWhileHuntingAndForAWhileAfter(
      final int hunting, final int idle, final boolean drawn) {
    final DrawnWeapon weapon = new DrawnWeapon();
    boolean last = false;
    for (int tick = 0; tick < hunting; tick++) {
      last = weapon.tick(true);
    }
    for (int tick = 0; tick < idle; tick++) {
      last = weapon.tick(false);
    }
    assertEquals(drawn, last);
  }

  @ParameterizedTest(name = "hunting {0} ticks, sheathed, then idle 1 tick: drawn false")
  @CsvSource({"1", "40", "500"})
  void sheathingPutsTheWeaponAwayAtOnce(final int hunting) {
    final DrawnWeapon weapon = new DrawnWeapon();
    for (int tick = 0; tick < hunting; tick++) {
      weapon.tick(true);
    }
    weapon.sheathe();
    assertEquals(false, weapon.tick(false));
  }
}
