package com.grahambartley.nightborn.client.vomit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.client.state.GoreLevel;
import net.minecraft.client.option.ParticlesMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VomitBurstTest {

  @ParameterizedTest(name = "gore {0}, particles {1}: {2} drops, splashes {3}")
  @CsvSource({
    "FULL, ALL, 14, true",
    "FULL, DECREASED, 5, false",
    "FULL, MINIMAL, 0, false",
    "REDUCED, ALL, 5, false",
    "REDUCED, DECREASED, 5, false",
    "REDUCED, MINIMAL, 0, false",
    "OFF, ALL, 0, false",
    "OFF, DECREASED, 0, false",
    "OFF, MINIMAL, 0, false"
  })
  @DisplayName("the gore option and the vanilla particles setting both cut the burst down")
  void follows(
      final GoreLevel gore,
      final ParticlesMode particles,
      final int drops,
      final boolean splashes) {
    final VomitBurst burst = VomitBurst.of(gore, particles);
    assertEquals(drops, burst.drops());
    assertEquals(splashes, burst.splashes());
  }
}
