package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class GeoAssetsTest {

  static Stream<Path> entityAndArmourGeo() throws IOException {
    return Stream.concat(
        GeoAssets.geoFiles(GeoAssets.ENTITY_GEO).stream(),
        GeoAssets.geoFiles(GeoAssets.ARMOR_GEO).stream());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("entityAndArmourGeo")
  @DisplayName("every geo file lists as geometry with named bones")
  void readsEveryGeoFile(final Path file) throws IOException {
    assertTrue(file.toString().endsWith(".geo.json"));
    assertFalse(GeoAssets.boneNames(file).isEmpty(), file.toString());
    assertEquals(GeoAssets.bones(file).size(), GeoAssets.boneNames(file).size());
  }

  static Stream<Path> clipFiles() throws IOException {
    return GeoAssets.clipFiles().stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("clipFiles")
  @DisplayName("every entity clip file reads as named animations")
  void readsEveryClipFile(final Path file) throws IOException {
    assertFalse(GeoAssets.animations(file).isEmpty());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "nightborn:geo/entity/vampire_male.geo.json, src/client/resources/assets/nightborn/geo/entity/vampire_male.geo.json",
    "minecraft:textures/block/oak_planks.png, src/client/resources/assets/minecraft/textures/block/oak_planks.png"
  })
  @DisplayName("an identifier resolves under the client assets folder")
  void resolvesIdentifiers(final String id, final String path) {
    assertEquals(Path.of(path), GeoAssets.file(Identifier.of(id)));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "nightborn:textures/entity/vampire_bat.png, src/main/resources/assets/nightborn/textures/entity/vampire_bat.png"
  })
  @DisplayName("an identifier resolves under the main assets folder")
  void resolvesMainIdentifiers(final String id, final String path) {
    assertEquals(Path.of(path), GeoAssets.mainFile(Identifier.of(id)));
  }
}
