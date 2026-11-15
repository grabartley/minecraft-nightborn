package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class DecorAssetsTest {

  private static final Path ASSETS = Path.of("src/main/resources/assets/nightborn");
  private static final Path CLIENT_ASSETS = Path.of("src/client/resources/assets/nightborn");
  private static final Path DATA = Path.of("src/main/resources/data/nightborn");
  private static final List<String> FURNITURE =
      List.of(
          "candelabra",
          "chandelier",
          "burgundy_velvet_drape",
          "emerald_velvet_drape",
          "gothic_stained_glass",
          "gothic_stained_glass_pane",
          "ornate_chair",
          "ornate_table");

  static Stream<String> decorIds() {
    return Stream.concat(
        BuildingFamily.ALL.stream().flatMap(family -> family.ids().stream()), FURNITURE.stream());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("decorIds")
  void everyDecorBlockHasItsStateModelItemLootAndName(final String id) throws IOException {
    assertTrue(Files.exists(ASSETS.resolve("blockstates/" + id + ".json")), "blockstate");
    assertTrue(Files.exists(ASSETS.resolve("models/item/" + id + ".json")), "item model");
    assertTrue(Files.exists(DATA.resolve("loot_table/blocks/" + id + ".json")), "loot table");
    assertTrue(lang("en_us").has("block.nightborn." + id), "english name");
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"de_de", "es_es", "fr_fr", "pt_br", "ru_ru", "zh_cn"})
  void everyLanguageCarriesTheEnglishKeys(final String language) throws IOException {
    assertEquals(lang("en_us").keySet(), lang(language).keySet());
  }

  @ParameterizedTest(name = "{0} uses {1}")
  @CsvSource({
    "candelabra, candelabra, true",
    "chandelier, chandelier, true",
    "burgundy_velvet_drape, velvet_drape, false",
    "emerald_velvet_drape, velvet_drape, false"
  })
  void everyAnimatedPieceHasItsGeometryClipsTextureAndGlow(
      final String id, final String art, final boolean glows) throws IOException {
    assertTrue(Files.exists(CLIENT_ASSETS.resolve("geo/block/" + art + ".geo.json")));
    assertTrue(Files.exists(ASSETS.resolve("textures/block/" + id + ".png")));
    assertEquals(glows, Files.exists(ASSETS.resolve("textures/block/" + id + "_glowmask.png")));
    final JsonObject clips =
        JsonParser.parseString(
                Files.readString(
                    CLIENT_ASSETS.resolve("animations/block/" + art + ".animation.json")))
            .getAsJsonObject()
            .getAsJsonObject("animations");
    assertEquals(
        Set.of(
            ToggleClips.ACTIVATE, ToggleClips.ACTIVE, ToggleClips.DEACTIVATE, ToggleClips.INACTIVE),
        clips.keySet());
  }

  private static JsonObject lang(final String language) throws IOException {
    return JsonParser.parseString(Files.readString(ASSETS.resolve("lang/" + language + ".json")))
        .getAsJsonObject();
  }
}
