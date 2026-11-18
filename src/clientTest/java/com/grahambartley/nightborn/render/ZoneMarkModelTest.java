package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class ZoneMarkModelTest {

  private static final Path GEO = GeoAssets.file(ZoneMarkModel.GEO);
  private static final Path CLIPS = GeoAssets.file(ZoneMarkModel.ANIMATION);
  private static final Path ATTACKS =
      Path.of("src/main/resources/data/nightborn/nightborn/boss_attack");

  @ParameterizedTest(name = "line {0}")
  @ValueSource(booleans = {false, true})
  @DisplayName("a circle and a line each have their own sheet, at the size the geometry maps")
  void sheetFitsTheGeometry(final boolean line) throws IOException {
    final JsonObject description = GeoAssets.geometry(GEO).getAsJsonObject("description");
    final Path texture = GeoAssets.mainFile(ZoneMarkModel.texture(line, Optional.empty()));
    assertTrue(Files.exists(texture), texture.toString());
    assertEquals(line, texture.getFileName().toString().contains("line"));
    final BufferedImage image = ImageIO.read(texture.toFile());
    assertEquals(description.get("texture_width").getAsInt(), image.getWidth());
    assertEquals(description.get("texture_height").getAsInt(), image.getHeight());
  }

  static Stream<String> marks() throws IOException {
    try (Stream<Path> files = Files.list(ATTACKS)) {
      return files
          .map(ZoneMarkModelTest::attack)
          .filter(attack -> attack.has("zone"))
          .map(attack -> attack.getAsJsonObject("zone"))
          .filter(zone -> zone.has("mark"))
          .map(zone -> zone.get("mark").getAsString())
          .distinct()
          .toList()
          .stream();
    }
  }

  private static JsonObject attack(final Path file) {
    try {
      return GeoAssets.read(file);
    } catch (final IOException unread) {
      throw new UncheckedIOException(unread);
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("marks")
  @DisplayName("every mark an attack names has its own sheet, at the size the geometry maps")
  void namedMarksFitTheGeometry(final String mark) throws IOException {
    final JsonObject description = GeoAssets.geometry(GEO).getAsJsonObject("description");
    final Path texture =
        GeoAssets.mainFile(ZoneMarkModel.texture(false, Optional.of(Identifier.of(mark))));
    assertTrue(Files.exists(texture), texture.toString());
    final BufferedImage image = ImageIO.read(texture.toFile());
    assertEquals(description.get("texture_width").getAsInt(), image.getWidth());
    assertEquals(description.get("texture_height").getAsInt(), image.getHeight());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"tell", "linger"})
  @DisplayName("the mark has a looping clip for its warning and for lingering")
  void hasItsClips(final String clip) throws IOException {
    final JsonObject shipped = GeoAssets.animations(CLIPS).getAsJsonObject(clip);
    assertTrue(shipped != null, clip + " is missing");
    assertTrue(shipped.get("loop").getAsBoolean());
  }
}
