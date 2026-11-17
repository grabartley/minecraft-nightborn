package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.grahambartley.nightborn.gramophone.GramophoneClips;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import javax.imageio.ImageIO;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GramophoneRendererTest {

  private static final Path GEO = GeoAssets.file(FurnitureArt.geo(GramophoneRenderer.ART));
  private static final Path CLIPS = GeoAssets.file(FurnitureArt.animation(GramophoneRenderer.ART));

  @Test
  @DisplayName("the gramophone's sheet ships at the size its geometry maps")
  void sheetFitsTheGeometry() throws IOException {
    final JsonObject description = GeoAssets.geometry(GEO).getAsJsonObject("description");
    final Path texture =
        GeoAssets.mainFile(FurnitureArt.texture(Identifier.of("nightborn", "gramophone")));
    assertTrue(Files.exists(texture));
    final BufferedImage image = ImageIO.read(texture.toFile());
    assertEquals(description.get("texture_width").getAsInt(), image.getWidth());
    assertEquals(description.get("texture_height").getAsInt(), image.getHeight());
  }

  @ParameterizedTest(name = "{0} loops {1}")
  @CsvSource({
    GramophoneClips.IDLE + ", true",
    GramophoneClips.LOADED + ", true",
    GramophoneClips.PLAYING + ", true",
    GramophoneClips.STOP + ", false"
  })
  @DisplayName("every clip the gramophone asks for exists and only moves bones the model has")
  void everyClipExists(final String name, final boolean loops) throws IOException {
    final JsonObject clip = GeoAssets.animations(CLIPS).getAsJsonObject(name);
    assertTrue(clip != null, name);
    final boolean looping =
        clip.get("loop").getAsJsonPrimitive().isBoolean() && clip.get("loop").getAsBoolean();
    assertEquals(loops, looping);
    assertTrue(GeoAssets.boneNames(GEO).containsAll(clip.getAsJsonObject("bones").keySet()));
  }

  @ParameterizedTest(name = "{0} shows the disc: {1}")
  @CsvSource({
    GramophoneClips.IDLE + ", false",
    GramophoneClips.LOADED + ", true",
    GramophoneClips.PLAYING + ", true",
    GramophoneClips.STOP + ", true"
  })
  @DisplayName("the disc is on the platter from the first frame of a clip, or not there at all")
  void theDiscShowsOrHidesAtOnce(final String name, final boolean shown) throws IOException {
    final JsonObject record =
        GeoAssets.animations(CLIPS)
            .getAsJsonObject(name)
            .getAsJsonObject("bones")
            .getAsJsonObject("record");
    assertTrue(!record.has("position"), name);
    final JsonObject scale = record.getAsJsonObject("scale");
    assertEquals(Set.of("0.0"), scale.keySet());
    assertEquals(shown ? 1.0 : 0.0, scale.getAsJsonArray("0.0").get(0).getAsDouble(), name);
  }
}
