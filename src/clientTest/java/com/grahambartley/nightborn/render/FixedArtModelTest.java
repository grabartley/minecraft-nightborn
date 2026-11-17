package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FixedArtModelTest {

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {RitualAltarRenderer.ART, GramophoneRenderer.ART})
  @DisplayName("every block drawn from one fixed model ships its geometry and its clips")
  void shipsItsArt(final String art) {
    assertTrue(Files.exists(GeoAssets.file(FurnitureArt.geo(art))));
    assertTrue(Files.exists(GeoAssets.file(FurnitureArt.animation(art))));
  }
}
