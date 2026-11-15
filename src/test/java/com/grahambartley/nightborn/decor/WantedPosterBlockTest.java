package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class WantedPosterBlockTest {

  private static final Path TEXTURES =
      Path.of("src/main/resources/assets/nightborn/textures/block");

  @ParameterizedTest(name = "face {0}")
  @ValueSource(ints = {0, 1, 2})
  void everyFaceHasItsOwnPortrait(final int face) {
    assertTrue(WantedPosterBlock.FACE.getValues().contains(face));
    assertTrue(Files.exists(TEXTURES.resolve("wanted_poster_" + face + ".png")));
  }

  @ParameterizedTest(name = "{0} faces")
  @ValueSource(ints = {3})
  void offersThreeFaces(final int faces) {
    assertEquals(faces, WantedPosterBlock.FACES);
    assertEquals(faces, WantedPosterBlock.FACE.getValues().size());
  }
}
