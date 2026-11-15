package com.grahambartley.nightborn.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SilverOreGenerationTest {

  private static final Path WORLDGEN = Path.of("src/main/resources/data/nightborn/worldgen");

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"nightborn:silver_ore", "nightborn:deepslate_silver_ore"})
  @DisplayName("the placed feature the code adds exists and places both silver ores")
  void placesBothOres(final String ore) throws IOException {
    final String path = SilverOreGeneration.ORE_SILVER.getValue().getPath();
    final JsonObject placed = read(WORLDGEN.resolve("placed_feature/" + path + ".json"));
    final String feature = placed.get("feature").getAsString();
    assertEquals(SilverOreGeneration.ORE_SILVER.getValue().toString(), feature);
    final JsonObject configured =
        read(WORLDGEN.resolve("configured_feature/" + feature.split(":")[1] + ".json"));
    assertTrue(configured.toString().contains("\"" + ore + "\""));
  }

  private static JsonObject read(final Path file) throws IOException {
    return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
  }
}
