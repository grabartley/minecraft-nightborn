package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class CirclePowerTest {

  @ParameterizedTest(name = "{0}: refused {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"radius\":4,\"duration_ticks\":600,\"damage\":1,\"stops_prayers\":true}; false",
        "{\"radius\":4,\"duration_ticks\":600}; false",
        "{\"radius\":0,\"duration_ticks\":600}; true",
        "{\"radius\":4,\"duration_ticks\":10}; true",
        "{\"radius\":4,\"duration_ticks\":600,\"damage\":-1}; true"
      })
  @DisplayName("a circle needs a radius and a time of at least a second")
  void reads(final String json, final boolean refused) {
    assertEquals(
        refused, CirclePower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }

  @ParameterizedTest(name = "{0}: look {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"radius\":4,\"duration_ticks\":600,\"look\":\"nightborn:blood_ward\"}; nightborn:blood_ward",
        "{\"radius\":4,\"duration_ticks\":600}; "
      })
  @DisplayName("a circle draws the mark it names on the ground, or none")
  void look(final String json, final String look) {
    assertEquals(
        Optional.ofNullable(look).map(Identifier::of),
        CirclePower.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
            .getOrThrow()
            .look());
  }
}
