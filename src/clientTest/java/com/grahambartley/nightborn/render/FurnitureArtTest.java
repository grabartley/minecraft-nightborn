package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.decor.ToggleClips;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FurnitureArtTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "candelabra, nightborn:geo/block/candelabra.geo.json, nightborn:animations/block/candelabra.animation.json",
    "velvet_drape, nightborn:geo/block/velvet_drape.geo.json, nightborn:animations/block/velvet_drape.animation.json"
  })
  void pointsGeometryAndClipsAtTheSharedArtModel(
      final String art, final String geo, final String clips) {
    assertEquals(Identifier.of(geo), FurnitureArt.geo(art));
    assertEquals(Identifier.of(clips), FurnitureArt.animation(art));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "nightborn:emerald_velvet_drape, nightborn:textures/block/emerald_velvet_drape.png",
    "nightborn:chandelier, nightborn:textures/block/chandelier.png"
  })
  void paintsEachBlockWithItsOwnTexture(final String block, final String texture) {
    assertEquals(Identifier.of(texture), FurnitureArt.texture(Identifier.of(block)));
  }

  @ParameterizedTest(name = "{0} uses {1}")
  @CsvSource({
    "candelabra, candelabra, true",
    "chandelier, chandelier, true",
    "burgundy_velvet_drape, velvet_drape, false",
    "emerald_velvet_drape, velvet_drape, false"
  })
  void everyArtPathResolvesToAShippedFileWithTheFourClips(
      final String block, final String art, final boolean glows) throws IOException {
    assertTrue(Files.exists(file(FurnitureArt.geo(art), "src/client/resources")));
    final Path texture = file(FurnitureArt.texture(FurnitureArt.id(block)), "src/main/resources");
    assertTrue(Files.exists(texture));
    assertEquals(glows, Files.exists(texture.resolveSibling(block + "_glowmask.png")));
    assertEquals(
        ToggleClips.FURNITURE.all(),
        JsonParser.parseString(
                Files.readString(file(FurnitureArt.animation(art), "src/client/resources")))
            .getAsJsonObject()
            .getAsJsonObject("animations")
            .keySet());
  }

  private static Path file(final Identifier id, final String root) {
    return Path.of(root, "assets", id.getNamespace(), id.getPath());
  }
}
