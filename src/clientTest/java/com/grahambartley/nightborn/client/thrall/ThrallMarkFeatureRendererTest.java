package com.grahambartley.nightborn.client.thrall;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ThrallMarkFeatureRendererTest {

  private static final Path MARKS =
      Path.of("src/main/resources/assets/nightborn/textures/entity/thrall");

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"desert", "jungle", "plains", "savanna", "snow", "swamp", "taiga"})
  @DisplayName("each thrall mark is a see-through overlay on the 64x64 villager layout")
  void overlaysTheVillager(final String type) throws IOException {
    final BufferedImage mark = ImageIO.read(MARKS.resolve(type + ".png").toFile());
    assertEquals(64, mark.getWidth());
    assertEquals(64, mark.getHeight());
    assertTrue(mark.getColorModel().hasAlpha(), type);
    assertEquals(0, mark.getRGB(0, 0) >>> 24, type);
  }
}
