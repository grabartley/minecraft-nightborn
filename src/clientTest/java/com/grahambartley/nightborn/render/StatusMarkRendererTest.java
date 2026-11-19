package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StatusMarkRendererTest {

  private static final Path TEXTURES =
      Path.of("src/main/resources/assets/nightborn/textures/entity");

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({
    "net_drape, net_drape.png",
    "hex_mark, hex_mark.png",
    "hex_mark, hex_mark_glowmask.png",
    "quarry_mark, quarry_mark.png",
    "quarry_mark, quarry_mark_glowmask.png"
  })
  @DisplayName("each mark's sheets ship at the size its geometry maps")
  void eachSheetShipsAtTheSizeTheGeometryMaps(final String rig, final String sheet)
      throws IOException {
    final JsonObject description =
        GeoAssets.geometry(GeoAssets.ENTITY_GEO.resolve(rig + ".geo.json"))
            .getAsJsonObject("description");
    final BufferedImage image = ImageIO.read(TEXTURES.resolve(sheet).toFile());
    assertEquals(description.get("texture_width").getAsInt(), image.getWidth());
    assertEquals(description.get("texture_height").getAsInt(), image.getHeight());
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({"net_drape, settle", "hex_mark, turn", "quarry_mark, pulse"})
  @DisplayName("every bone a mark's clip moves is in its rig")
  void everyBoneAClipMovesIsInTheRig(final String rig, final String clip) throws IOException {
    final Set<String> bones = GeoAssets.boneNames(GeoAssets.ENTITY_GEO.resolve(rig + ".geo.json"));
    final JsonObject animated =
        GeoAssets.animations(GeoAssets.clips(rig)).getAsJsonObject(clip).getAsJsonObject("bones");
    assertTrue(bones.containsAll(animated.keySet()), clip);
  }
}
