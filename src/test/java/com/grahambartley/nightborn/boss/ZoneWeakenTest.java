package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ZoneWeakenTest {

  @ParameterizedTest(name = "{0} at {1} for {2} ticks: loads {3}")
  @CsvSource({
    "holy_water, 2, 100, true",
    "consecrated, 1, 1, true",
    "garlic_bomb, 0, 100, false",
    "holy_water, 2, 0, false"
  })
  @DisplayName("a mark weakens with a named source, a strength and a time, all above nothing")
  void parses(final String source, final int strength, final int ticks, final boolean loads) {
    final var parsed =
        ZoneWeaken.CODEC.parse(
            JsonOps.INSTANCE,
            JsonParser.parseString(
                "{\"source\":\""
                    + source
                    + "\",\"strength\":"
                    + strength
                    + ",\"ticks\":"
                    + ticks
                    + "}"));
    assertEquals(loads, parsed.isSuccess());
    if (loads) {
      assertEquals(new ZoneWeaken(source, strength, ticks), parsed.getOrThrow());
    }
  }
}
