package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FreePowerTest {

  @ParameterizedTest(name = "{0}: refused {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"skills\":[\"nightborn:occult/shadow_step\"],\"duration_ticks\":300}; false",
        "{\"skills\":[],\"duration_ticks\":300}; false",
        "{\"skills\":[\"nightborn:occult/shadow_step\"]}; true",
        "{\"skills\":[\"nightborn:occult/shadow_step\"],\"duration_ticks\":0}; true"
      })
  @DisplayName("a free spell names the skills it frees and for how long")
  void reads(final String json, final boolean refused) {
    assertEquals(
        refused, FreePower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
