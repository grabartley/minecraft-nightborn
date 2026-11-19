package com.grahambartley.nightborn.trap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class BaitPowerTest {

  @ParameterizedTest(name = "{0}: refused {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"range\":4,\"duration_ticks\":1200,\"lure_range\":24,\"scent_range\":8,\"look\":\"nightborn:bait\"}; false",
        "{\"range\":4,\"duration_ticks\":1200,\"lure_range\":24,\"scent_range\":8,\"size\":0.6,\"look\":\"nightborn:bait\"}; false",
        "{\"range\":4,\"duration_ticks\":1200,\"lure_range\":0,\"scent_range\":8,\"look\":\"nightborn:bait\"}; true",
        "{\"range\":4,\"duration_ticks\":10,\"lure_range\":24,\"scent_range\":8,\"look\":\"nightborn:bait\"}; true",
        "{\"range\":4,\"duration_ticks\":1200,\"lure_range\":24,\"scent_range\":8}; true"
      })
  @DisplayName("bait needs a reach, a life, a lure range, a scent range and a look")
  void reads(final String json, final boolean refused) {
    assertEquals(
        refused, BaitPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
