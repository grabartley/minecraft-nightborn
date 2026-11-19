package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class NetRendererTest {

  static List<Path> art() {
    final Identifier rig = NetRenderer.RIG;
    final List<Path> files = new ArrayList<>();
    files.add(
        GeoAssets.file(
            Identifier.of(rig.getNamespace(), "geo/entity/" + rig.getPath() + ".geo.json")));
    files.add(
        GeoAssets.mainFile(
            Identifier.of(rig.getNamespace(), "textures/entity/" + rig.getPath() + ".png")));
    if (false) {
      files.add(
          GeoAssets.mainFile(
              Identifier.of(
                  rig.getNamespace(), "textures/entity/" + rig.getPath() + "_glowmask.png")));
    }
    return files;
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("art")
  @DisplayName("a thrown net ships its model and skin")
  void shipsArt(final Path file) {
    assertTrue(Files.exists(file), file.toString());
  }

  @Test
  @DisplayName("a thrown net ships the clip it loops")
  void shipsClip() throws IOException {
    assertTrue(GeoAssets.animations(GeoAssets.clips(NetRenderer.RIG.getPath())).has("fly"));
  }
}
