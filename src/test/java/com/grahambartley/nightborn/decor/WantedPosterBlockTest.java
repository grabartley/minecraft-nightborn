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

  @ParameterizedTest(name = "portrait {0}")
  @ValueSource(ints = {0, 1, 2})
  void everyPortraitHasItsOwnTexture(final int portrait) {
    assertTrue(WantedPosterBlock.PORTRAIT.getValues().contains(portrait));
    assertTrue(Files.exists(TEXTURES.resolve("wanted_poster_" + portrait + ".png")));
  }

  @ParameterizedTest(name = "{0} portraits")
  @ValueSource(ints = {3})
  void offersThreeFaces(final int portraits) {
    assertEquals(portraits, WantedPosterBlock.PORTRAITS);
    assertEquals(portraits, WantedPosterBlock.PORTRAIT.getValues().size());
  }
}
