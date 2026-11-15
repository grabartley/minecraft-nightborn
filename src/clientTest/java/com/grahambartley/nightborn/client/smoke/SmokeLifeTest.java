package com.grahambartley.nightborn.client.smoke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.math.random.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class SmokeLifeTest {

  @ParameterizedTest(name = "age {0} of {1}: scale {2}")
  @CsvSource({
    "0, 40, 0.6",
    "8, 40, 0.8",
    "16, 40, 1.0",
    "40, 40, 1.0",
    "60, 40, 1.0",
    "-4, 40, 0.6",
    "0, 0, 1.0"
  })
  @DisplayName("a puff swells to full size early in its life, then holds")
  void scale(final float age, final int maxAge, final float expected) {
    assertEquals(expected, SmokeLife.scale(age, maxAge), 1.0e-4F);
  }

  @ParameterizedTest(name = "age {0} of {1}: alpha {2}")
  @CsvSource({
    "0, 40, 0.8",
    "26, 40, 0.8",
    "33, 40, 0.4",
    "40, 40, 0.0",
    "60, 40, 0.0",
    "0, 0, 0.0"
  })
  @DisplayName("a puff stays thick, then thins away over the last third of its life")
  void alpha(final float age, final int maxAge, final float expected) {
    assertEquals(expected, SmokeLife.alpha(age, maxAge), 1.0e-4F);
  }

  @ParameterizedTest(name = "seed {0}")
  @ValueSource(longs = {0L, 1L, 42L, 1234L, -7L})
  @DisplayName("each puff rolls a life, size and shade inside their bounds")
  void rolls(final long seed) {
    final Random random = Random.create(seed);
    final int maxAge = SmokeLife.maxAge(random);
    final float size = SmokeLife.fullSize(random);
    final float shade = SmokeLife.shade(random);
    assertTrue(maxAge >= 30 && maxAge < 50, "life " + maxAge);
    assertTrue(size >= 0.4F && size < 0.65F, "size " + size);
    assertTrue(shade >= 0.8F && shade <= 1.0F, "shade " + shade);
  }
}
