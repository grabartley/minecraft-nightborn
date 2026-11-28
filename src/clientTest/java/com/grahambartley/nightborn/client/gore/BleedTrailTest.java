package com.grahambartley.nightborn.client.gore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.client.state.GoreLevel;
import net.minecraft.client.option.ParticlesMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BleedTrailTest {

  @ParameterizedTest(name = "gore {0}, particles {1}, walked {2}: splat {3}")
  @CsvSource({
    "FULL, ALL, 0.9, false",
    "FULL, ALL, 1.0, true",
    "FULL, DECREASED, 1.4, false",
    "FULL, DECREASED, 1.5, true",
    "REDUCED, ALL, 1.9, false",
    "REDUCED, ALL, 2.0, true",
    "REDUCED, DECREASED, 3.0, true",
    "OFF, ALL, 50, false",
    "FULL, MINIMAL, 50, false"
  })
  @DisplayName("a bleeding body leaves a splat each stretch it walks, further apart on less gore")
  void due(
      final GoreLevel gore, final ParticlesMode particles, final double walked, final boolean due) {
    assertEquals(due, BleedTrail.due(gore, particles, walked * walked));
  }

  @ParameterizedTest(name = "gore {0}, particles {1}: drawn {2}")
  @CsvSource({
    "FULL, ALL, true",
    "REDUCED, DECREASED, true",
    "OFF, ALL, false",
    "FULL, MINIMAL, false"
  })
  @DisplayName("no trail is drawn with gore off or particles at minimal")
  void drawn(final GoreLevel gore, final ParticlesMode particles, final boolean drawn) {
    assertEquals(drawn, BleedTrail.drawn(gore, particles));
  }
}
