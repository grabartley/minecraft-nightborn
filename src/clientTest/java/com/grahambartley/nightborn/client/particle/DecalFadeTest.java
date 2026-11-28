package com.grahambartley.nightborn.client.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class DecalFadeTest {

  private static final float SHADER_DISCARD = 0.1F;

  private static float faintestShipped;

  @BeforeAll
  static void readShippedSprites() throws IOException {
    faintestShipped = 1.0F;
    for (final String name :
        new String[] {
          "blood_splat_0",
          "blood_splat_1",
          "blood_splat_2",
          "blood_splat_3",
          "blood_splat_body_0",
          "blood_splat_body_1",
          "blood_splat_body_2",
          "blood_splat_body_3",
          "vomit_splash_0",
          "vomit_splash_1",
          "vomit_splash_2",
          "vomit_splash_body_0",
          "vomit_splash_body_1",
          "vomit_splash_body_2"
        }) {
      try (InputStream in =
          DecalFadeTest.class.getResourceAsStream(
              "/assets/nightborn/textures/particle/" + name + ".png")) {
        final BufferedImage image = ImageIO.read(in);
        for (int y = 0; y < image.getHeight(); y++) {
          for (int x = 0; x < image.getWidth(); x++) {
            final int alpha = image.getRGB(x, y) >>> 24;
            if (alpha > 0) {
              faintestShipped = Math.min(faintestShipped, alpha / 255.0F);
            }
          }
        }
      }
    }
  }

  @ParameterizedTest(name = "age {0}: wetness {1}")
  @CsvSource({"-10, 1.0", "0, 1.0", "600, 1.0", "900, 0.75", "1200, 0.5", "1800, 0.0", "9000, 0.0"})
  @DisplayName("a decal is wet for half a minute, then dries over a minute")
  void wetness(final int age, final float expected) {
    assertEquals(expected, DecalFade.wetness(age), 1.0e-4F);
  }

  @ParameterizedTest(name = "age {0} of {1}, fade {2}: alpha {3}")
  @CsvSource({
    "0, 1000, 100, 1.0",
    "900, 1000, 100, 1.0",
    "950, 1000, 100, 0.15573",
    "1000, 1000, 100, 0.096226"
  })
  @DisplayName("alpha stays full until the fade, then falls")
  void alpha(final int age, final int maxAge, final int fade, final float expected) {
    assertEquals(expected, DecalFade.alpha(age, maxAge, fade), 1.0e-4F);
  }

  @ParameterizedTest(name = "age {0}")
  @ValueSource(ints = {0, 500, 899, 900})
  @DisplayName("every shipped pixel shows until the fade starts")
  void keepsEveryPixelBeforeTheFade(final int age) {
    assertTrue(faintestShipped * DecalFade.alpha(age, 1000, 100) >= SHADER_DISCARD);
  }

  @ParameterizedTest(name = "step {0} of the fade")
  @ValueSource(ints = {10, 30, 50, 70})
  @DisplayName("the fade drops the faintest pixels first while solid ones stay")
  void dissolvesFromTheFaintestPixel(final int step) {
    final float alpha = DecalFade.alpha(900 + step, 1000, 100);
    assertTrue(alpha >= SHADER_DISCARD, "a solid pixel stays");
    assertFalse(faintestShipped * alpha >= SHADER_DISCARD, "the faintest pixel is gone");
  }

  @ParameterizedTest(name = "age {0} of 1000")
  @ValueSource(ints = {996, 1000})
  @DisplayName("solid pixels go before the last tick, so nothing pops")
  void nothingIsLeftAtTheEnd(final int age) {
    assertFalse(DecalFade.alpha(age, 1000, 100) >= SHADER_DISCARD);
  }
}
