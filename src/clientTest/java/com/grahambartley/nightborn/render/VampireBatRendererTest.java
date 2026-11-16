package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import javax.imageio.ImageIO;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VampireBatRendererTest {

  private static final Path TEXTURES =
      Path.of("src/main/resources/assets/nightborn/textures/entity");
  private static final Path GEO = GeoAssets.ENTITY_GEO.resolve("vampire_bat.geo.json");
  private static final Path CLIPS = GeoAssets.ENTITY_CLIPS.resolve("vampire_bat.animation.json");

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"vampire_bat.png", "vampire_bat_glowmask.png"})
  void eachSheetShipsAtTheSizeTheGeometryMaps(final String sheet) throws IOException {
    final JsonObject description = GeoAssets.geometry(GEO).getAsJsonObject("description");
    final BufferedImage image = ImageIO.read(TEXTURES.resolve(sheet).toFile());
    assertEquals(description.get("texture_width").getAsInt(), image.getWidth());
    assertEquals(description.get("texture_height").getAsInt(), image.getHeight());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"hang", "fly", "swoop", "bite"})
  void everyBoneAClipMovesIsInTheRig(final String clip) throws IOException {
    final Set<String> bones = GeoAssets.boneNames(GEO);
    final JsonObject animated =
        GeoAssets.animations(CLIPS).getAsJsonObject(clip).getAsJsonObject("bones");
    assertTrue(bones.containsAll(animated.keySet()), clip);
  }
}
