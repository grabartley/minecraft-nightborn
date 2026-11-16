package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class BatFormRendererTest {

  private static final Path CLIPS = GeoAssets.clips("vampire_bat");

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "src/client/resources/assets/nightborn/geo/entity/vampire_bat.geo.json",
        "src/main/resources/assets/nightborn/textures/entity/vampire_bat.png",
        "src/main/resources/assets/nightborn/textures/entity/vampire_bat_glowmask.png"
      })
  @DisplayName("bat form draws the vampire bat's own rig, texture and glowing eyes")
  void sharesTheVampireBatArt(final String file) {
    assertTrue(Files.exists(Path.of(file)), file);
  }

  @ParameterizedTest(name = "swing {0}")
  @CsvSource({"0.0", "1.0"})
  @DisplayName("every clip bat form plays ships in the vampire bat's animations")
  void playsShippedClips(final float swing) throws IOException {
    final String clip = BatFormAnimatable.pose(swing).animation();
    assertTrue(GeoAssets.animations(CLIPS).has(clip), clip);
  }
}
