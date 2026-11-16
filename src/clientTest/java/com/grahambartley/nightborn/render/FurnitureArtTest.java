package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    assertTrue(Files.exists(GeoAssets.file(FurnitureArt.geo(art))));
    final Path texture = GeoAssets.mainFile(FurnitureArt.texture(FurnitureArt.id(block)));
    assertTrue(Files.exists(texture));
    assertEquals(glows, Files.exists(texture.resolveSibling(block + "_glowmask.png")));
    assertEquals(
        ToggleClips.FURNITURE.all(),
        GeoAssets.animations(GeoAssets.file(FurnitureArt.animation(art))).keySet());
  }
}
