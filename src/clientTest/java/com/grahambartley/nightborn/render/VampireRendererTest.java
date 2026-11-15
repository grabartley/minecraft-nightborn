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
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class VampireRendererTest {

  private static final Path TEXTURES =
      Path.of("src/main/resources/assets/nightborn/textures/entity/vampire");
  private static final Path GEO = Path.of("src/client/resources/assets/nightborn/geo/entity");
  private static final Path CLIPS =
      Path.of("src/client/resources/assets/nightborn/animations/entity/vampire.animation.json");

  static Stream<Arguments> sheets() throws IOException {
    try (Stream<Path> files = Files.list(TEXTURES)) {
      return files
          .map(file -> file.getFileName().toString())
          .map(name -> Arguments.of(name, name.startsWith("female") ? "female" : "male"))
          .toList()
          .stream();
    }
  }

  @ParameterizedTest(name = "{0} on the {1} rig")
  @MethodSource("sheets")
  @DisplayName("every look and eye sheet ships at the size its rig maps")
  void sheetMatchesRig(final String sheet, final String rig) throws IOException {
    final JsonObject description = geometry(rig).getAsJsonObject("description");
    final BufferedImage image = ImageIO.read(TEXTURES.resolve(sheet).toFile());
    assertEquals(description.get("texture_width").getAsInt(), image.getWidth());
    assertEquals(description.get("texture_height").getAsInt(), image.getHeight());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"male", "female"})
  @DisplayName("every bone a clip moves is in each rig, the jaw included")
  void everyAnimatedBoneIsInTheRig(final String rig) throws IOException {
    final Set<String> bones = new HashSet<>();
    geometry(rig)
        .getAsJsonArray("bones")
        .forEach(bone -> bones.add(bone.getAsJsonObject().get("name").getAsString()));
    final JsonObject clips =
        JsonParser.parseString(Files.readString(CLIPS))
            .getAsJsonObject()
            .getAsJsonObject("animations");
    for (final String clip : clips.keySet()) {
      final JsonObject animated = clips.getAsJsonObject(clip).getAsJsonObject("bones");
      if (animated != null) {
        assertTrue(bones.containsAll(animated.keySet()), clip + " on " + rig);
      }
    }
    assertTrue(
        clips.has("hiss") && clips.getAsJsonObject("hiss").getAsJsonObject("bones").has("jaw"));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"male_corvin.png"})
  @DisplayName("the fallback look for a vampire whose look was removed ships with the mod")
  void fallbackShips(final String sheet) {
    assertTrue(VampireModel.FALLBACK_TEXTURE.getPath().endsWith(sheet));
    assertTrue(Files.exists(TEXTURES.resolve(sheet)));
  }

  private static JsonObject geometry(final String rig) throws IOException {
    return JsonParser.parseString(Files.readString(GEO.resolve("vampire_" + rig + ".geo.json")))
        .getAsJsonObject()
        .getAsJsonArray("minecraft:geometry")
        .get(0)
        .getAsJsonObject();
  }
}
