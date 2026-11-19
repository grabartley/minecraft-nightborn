package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonArray;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ZoneMarkRendererTest {

  @ParameterizedTest(name = "line {0}, radius {1}, length {2}: {3} wide, {4} long")
  @CsvSource({
    "false, 3.0, 0.0, 6.0, 6.0",
    "false, 2.5, 0.0, 5.0, 5.0",
    "true, 1.25, 10.0, 2.5, 10.0",
    "true, 2.0, 18.0, 4.0, 18.0"
  })
  @DisplayName("the mark is stretched to cover exactly the ground the attack will hit")
  void covers(
      final boolean line,
      final float radius,
      final float length,
      final float wide,
      final float longways) {
    final float model = ZoneMarkRenderer.MODEL_HALF_WIDTH * 2.0F;
    assertEquals(wide, ZoneMarkRenderer.across(radius) * model, 1.0E-6F);
    assertEquals(longways, ZoneMarkRenderer.along(line, radius, length) * model, 1.0E-6F);
  }

  @Test
  @DisplayName("the shipped model is as wide as the renderer assumes")
  void modelWidth() throws IOException {
    final JsonArray size =
        GeoAssets.geometry(GeoAssets.file(ZoneMarkModel.GEO))
            .getAsJsonArray("bones")
            .get(0)
            .getAsJsonObject()
            .getAsJsonArray("cubes")
            .get(0)
            .getAsJsonObject()
            .getAsJsonArray("size");
    assertEquals(ZoneMarkRenderer.MODEL_HALF_WIDTH * 2.0F * 16.0F, size.get(0).getAsFloat());
    assertEquals(ZoneMarkRenderer.MODEL_HALF_WIDTH * 2.0F * 16.0F, size.get(2).getAsFloat());
  }

  @ParameterizedTest(name = "a pillar {0} blocks tall")
  @CsvSource({"8.0", "12.0", "2.0"})
  @DisplayName("the pillar of light is stretched to the height of the prayer's reach")
  void rises(final float height) {
    assertEquals(
        height,
        ZoneMarkRenderer.rise(height) * ZoneMarkRenderer.PILLAR_HEIGHT * ZoneMarkRenderer.THIN,
        1.0E-6F);
  }

  @Test
  @DisplayName("the shipped pillar is as tall as the renderer assumes")
  void pillarHeight() throws IOException {
    for (final var bone : GeoAssets.bones(GeoAssets.file(ZoneMarkModel.GEO))) {
      if (ZoneMarkRenderer.PILLAR.equals(bone.getAsJsonObject().get("name").getAsString())) {
        for (final var cube : bone.getAsJsonObject().getAsJsonArray("cubes")) {
          assertEquals(
              ZoneMarkRenderer.PILLAR_HEIGHT * 16.0F,
              cube.getAsJsonObject().getAsJsonArray("size").get(1).getAsFloat());
        }
        return;
      }
    }
    throw new AssertionError("no pillar bone");
  }

  @ParameterizedTest(name = "radius {0}, length {1}, rise {2}: {3} across, {4} tall")
  @CsvSource({"2.0, 0.0, 8.0, 4.0, 8.0", "10.0, 0.0, 0.0, 20.0, 1.0", "1.25, 18.0, 0.0, 18.0, 1.0"})
  @DisplayName("a mark stays drawn while any part of it, pillar included, is in view")
  void reach(
      final float radius,
      final float length,
      final float rise,
      final double across,
      final double tall) {
    final var box = ZoneMarkRenderer.reach(0.0, 64.0, 0.0, radius, length, rise);
    assertEquals(across, box.getLengthX(), 1.0E-6);
    assertEquals(across, box.getLengthZ(), 1.0E-6);
    assertEquals(tall, box.getLengthY(), 1.0E-6);
    assertEquals(64.0, box.minY, 1.0E-6);
  }
}
