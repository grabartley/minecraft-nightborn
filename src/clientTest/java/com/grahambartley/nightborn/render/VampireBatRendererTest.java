package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VampireBatRendererTest {

  private static final Path TEXTURES =
      Path.of("src/main/resources/assets/nightborn/textures/entity");
  private static final Path GEO =
      Path.of("src/client/resources/assets/nightborn/geo/entity/vampire_bat.geo.json");
  private static final Path CLIPS =
      Path.of("src/client/resources/assets/nightborn/animations/entity/vampire_bat.animation.json");

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"vampire_bat.png", "vampire_bat_glowmask.png"})
  void eachSheetShipsAtTheSizeTheGeometryMaps(final String sheet) throws IOException {
    final JsonObject description = geometry().getAsJsonObject("description");
    final BufferedImage image = ImageIO.read(TEXTURES.resolve(sheet).toFile());
    assertEquals(description.get("texture_width").getAsInt(), image.getWidth());
    assertEquals(description.get("texture_height").getAsInt(), image.getHeight());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"hang", "fly", "swoop", "bite"})
  void everyBoneAClipMovesIsInTheRig(final String clip) throws IOException {
    final Set<String> bones = new HashSet<>();
    geometry()
        .getAsJsonArray("bones")
        .forEach(bone -> bones.add(bone.getAsJsonObject().get("name").getAsString()));
    final JsonObject animated =
        JsonParser.parseString(Files.readString(CLIPS))
            .getAsJsonObject()
            .getAsJsonObject("animations")
            .getAsJsonObject(clip)
            .getAsJsonObject("bones");
    assertTrue(bones.containsAll(animated.keySet()), clip);
  }

  private static JsonObject geometry() throws IOException {
    return JsonParser.parseString(Files.readString(GEO))
        .getAsJsonObject()
        .getAsJsonArray("minecraft:geometry")
        .get(0)
        .getAsJsonObject();
  }
}
