package com.grahambartley.nightborn.mind;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DreadPowerTest {

  @ParameterizedTest(name = "{0}: refused {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"radius\":8,\"duration_ticks\":240,\"flees\":\"nightborn:dread/flees\",\"cowers\":\"nightborn:dread/cowers\",\"falters\":\"nightborn:dread/falters\",\"falter_damage\":0.8}; false",
        "{\"radius\":8,\"duration_ticks\":240,\"flees\":\"nightborn:dread/flees\",\"cowers\":\"nightborn:dread/cowers\",\"falters\":\"nightborn:dread/falters\",\"falter_damage\":0.1}; true",
        "{\"radius\":8,\"duration_ticks\":240,\"flees\":\"nightborn:dread/flees\",\"cowers\":\"nightborn:dread/cowers\",\"falter_damage\":0.8}; true",
        "{\"radius\":8,\"duration_ticks\":240,\"flees\":\"nightborn:dread/flees\",\"cowers\":\"nightborn:dread/cowers\",\"falters\":\"nightborn:dread/falters\",\"falter_damage\":0.8,\"player_effects\":[{\"id\":\"minecraft:slowness\",\"duration\":20}]}; false"
      })
  @DisplayName("a dread names who flees, cowers and falters, and never cuts damage past the cap")
  void reads(final String json, final boolean refused) {
    assertEquals(
        refused, DreadPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
