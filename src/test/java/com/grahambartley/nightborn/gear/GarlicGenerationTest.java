package com.grahambartley.nightborn.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class GarlicGenerationTest {

  private static final Path DATA = Path.of("src/main/resources/data/nightborn");

  @Test
  @DisplayName("the placed feature the code adds places fully grown garlic on grass")
  void placesGrownGarlicOnGrass() throws IOException {
    final String path = GarlicGeneration.PATCH_GARLIC.getValue().getPath();
    final JsonObject placed = read(DATA.resolve("worldgen/placed_feature/" + path + ".json"));
    final String feature = placed.get("feature").getAsString();
    assertEquals(GarlicGeneration.PATCH_GARLIC.getValue().toString(), feature);
    final JsonObject patch =
        read(DATA.resolve("worldgen/configured_feature/" + feature.split(":")[1] + ".json"))
            .getAsJsonObject("config")
            .getAsJsonObject("feature");
    final JsonObject state =
        patch
            .getAsJsonObject("feature")
            .getAsJsonObject("config")
            .getAsJsonObject("to_place")
            .getAsJsonObject("state");
    assertEquals("nightborn:garlic", state.get("Name").getAsString());
    assertEquals("7", state.getAsJsonObject("Properties").get("age").getAsString());
    assertTrue(patch.getAsJsonArray("placement").toString().contains("minecraft:grass_block"));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "minecraft:plains",
        "minecraft:sunflower_plains",
        "minecraft:meadow",
        "#minecraft:has_structure/village_plains",
        "#minecraft:has_structure/village_savanna",
        "#minecraft:has_structure/village_taiga"
      })
  @DisplayName("garlic grows wild in plains and where villages stand")
  void growsInPlainsAndVillageLands(final String biome) throws IOException {
    final String path = GarlicGeneration.GROWS_WILD_GARLIC.id().getPath();
    final JsonArray values =
        read(DATA.resolve("tags/worldgen/biome/" + path + ".json")).getAsJsonArray("values");
    assertTrue(values.toString().contains("\"" + biome + "\""), biome);
  }

  private static JsonObject read(final Path file) throws IOException {
    return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
  }
}
