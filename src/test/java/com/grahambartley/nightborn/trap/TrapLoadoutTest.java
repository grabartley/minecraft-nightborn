package com.grahambartley.nightborn.trap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TrapLoadoutTest {

  @ParameterizedTest(name = "cut {0}, seen {1}, extra {2}, damage {3}")
  @CsvSource({"0.0, 6, 0, 0", "0.25, 4, 1, 2", "0.75, 2, 3, 6"})
  @DisplayName("a trap is set with its power's numbers and the setter's gifts at that moment")
  void loads(final double cut, final float seen, final int extra, final float damage) {
    final TrapPower power =
        TrapPowerTest.read(
            "\"range\":4,\"duration_ticks\":6000,\"limit\":2,\"arm_ticks\":40,\"seen_within\":6,"
                + "\"stepped\":true,\"damage_type\":\"nightborn:silver\",\"look\":\"nightborn:snare\"");
    final TrapLoadout loadout =
        TrapLoadout.of(
            power,
            List.of(
                new TrapGift(Optional.empty(), cut, Optional.of(seen), extra, damage, false, 0.0)));
    assertEquals((int) Math.round(40 * (1.0 - cut)), loadout.armTicks());
    assertEquals(seen, loadout.seenWithin());
    assertEquals(2 + extra, loadout.limit());
    assertEquals(damage, loadout.extraDamage());
  }
}
