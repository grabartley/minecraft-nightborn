package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HouseholdArrivalTest {

  @ParameterizedTest(name = "{0}")
  @ValueSource(ints = {0, 1, 40})
  @DisplayName("an arrival keeps its own copy of the household's stage odds")
  void copies(final int weight) {
    final Map<Identifier, Integer> odds = new HashMap<>();
    odds.put(Identifier.of("nightborn", "elder"), weight);
    final HouseholdArrival arrival = new HouseholdArrival(odds, false);
    odds.clear();
    assertEquals(Map.of(Identifier.of("nightborn", "elder"), weight), arrival.stages());
    assertThrows(UnsupportedOperationException.class, () -> arrival.stages().clear());
  }
}
