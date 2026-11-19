package com.grahambartley.nightborn.client.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BossSetTooltipTest {

  @ParameterizedTest(name = "{0} {1}, blade {2}, armour {3}: own {4}, shared {5}")
  @CsvSource({
    "eldest_blade, eldest_set, true, false, true, true",
    "eldest_blade, eldest_set, false, true, false, true",
    "eldest_blade, eldest_set, false, false, false, false",
    "inquisitor_greatsword, inquisitor_set, true, true, true, true",
    "inquisitor_greatsword, inquisitor_set, false, true, false, true",
    "inquisitor_greatsword, inquisitor_set, false, false, false, false"
  })
  @DisplayName("each set's weapon explains its own power, and every piece explains the set bonus")
  void lines(
      final String bladeKey,
      final String setKey,
      final boolean blade,
      final boolean armor,
      final boolean own,
      final boolean shared) {
    final BossSetTooltip.BossSet set = new BossSetTooltip.BossSet(null, null, bladeKey, setKey);
    assertEquals(own ? Optional.of(bladeKey) : Optional.empty(), set.own(blade));
    assertEquals(shared ? Optional.of(setKey) : Optional.empty(), set.shared(blade, armor));
  }
}
