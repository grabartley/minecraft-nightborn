package com.grahambartley.nightborn.client.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ParticleLifeTest {

  @ParameterizedTest(name = "age {0} of {1}: {2}")
  @CsvSource({
    "0, 20, 0.0",
    "10, 20, 0.5",
    "20, 20, 1.0",
    "30, 20, 1.0",
    "-5, 20, 0.0",
    "3, 0, 1.0"
  })
  @DisplayName(
      "a particle's life runs from 0 at birth to 1 at its last tick, and never leaves that range")
  void fraction(final float age, final int maxAge, final float expected) {
    assertEquals(expected, ParticleLife.fraction(age, maxAge), 1.0e-6F);
  }

  @ParameterizedTest(name = "from {0} to {1}, age {2} of {3}: {4}")
  @CsvSource({
    "1.0, 0.5, 0, 10, 1.0",
    "1.0, 0.5, 5, 10, 0.75",
    "1.0, 0.5, 10, 10, 0.5",
    "0.2, 1.0, 15, 10, 1.0"
  })
  @DisplayName("a particle eases from one value to another over its life, and stops at the end")
  void lerp(
      final float start, final float end, final float age, final int maxAge, final float expected) {
    assertEquals(expected, ParticleLife.lerp(start, end, age, maxAge), 1.0e-6F);
  }
}
