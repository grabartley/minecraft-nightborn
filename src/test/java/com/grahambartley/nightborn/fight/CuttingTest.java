package com.grahambartley.nightborn.fight;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CuttingTest {

  @ParameterizedTest(name = "{0}, marked {1}, edged {2}, melee {3}: severing {4}")
  @CsvSource({
    "WEAPON, false, false, false, false",
    "WEAPON, false, true, true, true",
    "WEAPON, false, false, true, false",
    "WEAPON, true, false, false, true",
    "ALL, false, false, true, true",
    "ALL, false, false, false, false",
    "ALL, true, false, false, true",
    "MARKED, false, true, true, false",
    "MARKED, true, false, false, true"
  })
  @DisplayName("a mob cuts with blades, with every melee blow, or only with marked attacks")
  void severing(
      final Cutting cutting,
      final boolean marked,
      final boolean edged,
      final boolean melee,
      final boolean severing) {
    assertEquals(severing, cutting.severing(marked, edged, melee));
  }

  @ParameterizedTest(name = "{0}, marked {1}: barred {2}")
  @CsvSource({
    "WEAPON, false, false",
    "WEAPON, true, false",
    "ALL, false, false",
    "MARKED, false, true",
    "MARKED, true, false"
  })
  @DisplayName("an unmarked blow from a mob that cuts only with marked attacks never severs")
  void bars(final Cutting cutting, final boolean marked, final boolean barred) {
    assertEquals(barred, cutting.bars(marked));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"weapon, WEAPON", "all, ALL", "marked, MARKED"})
  @DisplayName("each way of cutting reads from its data name")
  void parses(final String name, final Cutting cutting) {
    assertEquals(
        cutting, Cutting.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(name)).getOrThrow());
  }
}
