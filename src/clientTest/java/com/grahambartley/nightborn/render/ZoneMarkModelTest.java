package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ZoneMarkModelTest {

  private static final Path GEO = GeoAssets.file(ZoneMarkModel.GEO);
  private static final Path CLIPS = GeoAssets.file(ZoneMarkModel.ANIMATION);

  @ParameterizedTest(name = "line {0}")
  @ValueSource(booleans = {false, true})
  @DisplayName("a circle and a line each have their own sheet, at the size the geometry maps")
  void sheetFitsTheGeometry(final boolean line) throws IOException {
    final JsonObject description = GeoAssets.geometry(GEO).getAsJsonObject("description");
    final Path texture = GeoAssets.mainFile(ZoneMarkModel.texture(line));
    assertTrue(Files.exists(texture), texture.toString());
    assertEquals(line, texture.getFileName().toString().contains("line"));
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
