package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.grahambartley.nightborn.oldblood.FontClips;
import com.grahambartley.nightborn.oldblood.FontLevel;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class OldBloodFontRendererTest {

  private static final Path GEO = GeoAssets.file(FurnitureArt.geo(OldBloodFontRenderer.ART));
  private static final Path CLIPS =
      GeoAssets.file(FurnitureArt.animation(OldBloodFontRenderer.ART));
  private static final Identifier SHEET =
      FurnitureArt.texture(Identifier.of("nightborn", OldBloodFontRenderer.ART));

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {".png", "_glowmask.png"})
  @DisplayName("the font's sheet and glow mask ship at the size its geometry maps")
  void sheetFitsTheGeometry(final String suffix) throws IOException {
    final JsonObject description = GeoAssets.geometry(GEO).getAsJsonObject("description");
    final Path texture = Path.of(GeoAssets.mainFile(SHEET).toString().replace(".png", suffix));
    assertTrue(Files.exists(texture), texture.toString());
    final BufferedImage image = ImageIO.read(texture.toFile());
    assertEquals(description.get("texture_width").getAsInt(), image.getWidth());
    assertEquals(description.get("texture_height").getAsInt(), image.getHeight());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(FontLevel.class)
  @DisplayName("every fill level has a looping clip that only moves bones the model has")
  void everyLevelHasItsLoop(final FontLevel level) throws IOException {
    final JsonObject clip = GeoAssets.animations(CLIPS).getAsJsonObject(level.clip());
    assertNotNull(clip, level.clip());
    assertTrue(clip.get("loop").getAsBoolean());
    assertTrue(GeoAssets.boneNames(GEO).containsAll(clip.getAsJsonObject("bones").keySet()));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {FontClips.DRINK, FontClips.REFILL})
  @DisplayName("the drink and refill clips play once and only move bones the model has")
  void changesPlayOnce(final String name) throws IOException {
    final JsonObject clip = GeoAssets.animations(CLIPS).getAsJsonObject(name);
    assertNotNull(clip, name);
    assertTrue(!clip.has("loop") || !clip.get("loop").getAsBoolean());
    assertTrue(GeoAssets.boneNames(GEO).containsAll(clip.getAsJsonObject("bones").keySet()));
  }
}
