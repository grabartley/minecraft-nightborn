package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class PickUnownedLootFunctionTest {

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({
    "'{\"items\":[\"nightborn:ring_of_the_court\",\"nightborn:ring_of_the_beast\"]}', true",
    "'{\"items\":[]}', false",
    "'{}', false"
  })
  @DisplayName("a loot table names the pieces to pick from, and never none")
  void reads(final String json, final boolean reads) {
    assertEquals(
        reads,
        PickUnownedLootFunction.CODEC
            .codec()
            .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
            .isSuccess());
  }
}
