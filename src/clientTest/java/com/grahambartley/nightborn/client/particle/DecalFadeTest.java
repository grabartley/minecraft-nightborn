package com.grahambartley.nightborn.client.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DecalFadeTest {

  private static final float FAINTEST = 64 / 255.0F;

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
    "1000, 1000, 100, 0.102",
    "950, 1000, 100, 0.1632",
    "0, 0, 0, 0.102"
  })
  @DisplayName("alpha stays full until the fade, then falls to the cutoff")
  void alpha(final int age, final int maxAge, final int fade, final float expected) {
    assertEquals(expected, DecalFade.alpha(age, maxAge, fade), 1.0e-4F);
  }

  @ParameterizedTest(name = "{0} of the way through the fade")
  @CsvSource({"1", "25", "50", "75", "99"})
  @DisplayName("the fade drops faint pixels first and keeps solid ones to the end")
  void dissolvesFromTheFaintestPixel(final int step) {
    final float alpha = DecalFade.alpha(900 + step, 1000, 100);
    assertTrue(1.0F * alpha >= DecalFade.DISCARD_BELOW, "a solid pixel stays");
    final float keptFrom = DecalFade.DISCARD_BELOW / alpha;
    assertTrue(keptFrom > FAINTEST - 1.0e-3F && keptFrom <= 1.0F);
  }

  @ParameterizedTest(name = "age {0}")
  @CsvSource({"0", "500", "899", "900"})
  @DisplayName("every pixel shows until the fade starts")
  void keepsEveryPixelBeforeTheFade(final int age) {
    assertTrue(FAINTEST * DecalFade.alpha(age, 1000, 100) >= DecalFade.DISCARD_BELOW);
  }
}
