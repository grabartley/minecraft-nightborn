package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.grahambartley.nightborn.ritual.AltarPhase;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class RitualAltarModelTest {

  private static final Path GEO = GeoAssets.file(FurnitureArt.geo(RitualAltarModel.ART));
  private static final Path CLIPS = GeoAssets.file(FurnitureArt.animation(RitualAltarModel.ART));

  @Test
  @DisplayName("the altar's sheet ships at the size its geometry maps")
  void sheetFitsTheGeometry() throws IOException {
    final JsonObject description = GeoAssets.geometry(GEO).getAsJsonObject("description");
    final Path texture =
        GeoAssets.mainFile(FurnitureArt.texture(Identifier.of("nightborn", "ritual_altar")));
    assertTrue(Files.exists(texture));
    final BufferedImage image = ImageIO.read(texture.toFile());
    assertEquals(description.get("texture_width").getAsInt(), image.getWidth());
    assertEquals(description.get("texture_height").getAsInt(), image.getHeight());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(AltarPhase.class)
  @DisplayName("every phase has a looping clip that only moves bones the model has")
  void everyPhaseHasItsClip(final AltarPhase phase) throws IOException {
    final JsonObject clip = GeoAssets.animations(CLIPS).getAsJsonObject(phase.clip());
    assertTrue(clip != null, phase.clip());
    assertTrue(clip.get("loop").getAsBoolean());
    assertTrue(GeoAssets.boneNames(GEO).containsAll(clip.getAsJsonObject("bones").keySet()));
  }
}
