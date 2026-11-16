package com.grahambartley.nightborn.client.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AmbienceMixTest {

  @ParameterizedTest(name = "volume {0} towards {1} gives {2}")
  @CsvSource({
    "0.0, 1.0, 0.025",
    "1.0, 0.0, 0.975",
    "0.5, 0.51, 0.51",
    "0.5, 0.49, 0.49",
    "0.3, 0.3, 0.3",
    "0.0, 0.0, 0.0"
  })
  @DisplayName("the loop fades a fortieth of full volume a tick and never overshoots")
  void approaches(final float volume, final float target, final float expected) {
    assertEquals(expected, AmbienceMix.approach(volume, target), 0.0001F);
  }

  @ParameterizedTest(name = "chance {0}, closeness {1}, roll {2}: {3}")
  @CsvSource({
    "0.0111, 0.5, 0.005, true",
    "0.0111, 0.5, 0.02, false",
    "0.0111, 0.0, 0.005, false",
    "1.0, 0.01, 0.999, true",
    "0.0, 1.0, 0.0, false"
  })
  @DisplayName("one-shots play only inside a zone, as often as the data's tick chance says")
  void addsOneShot(
      final double chance, final float closeness, final double roll, final boolean plays) {
    assertEquals(plays, AmbienceMix.addsOneShot(chance, closeness, roll));
  }

  @ParameterizedTest(name = "closeness {0} gives volume {1}")
  @CsvSource({"0.0, 0.35", "0.5, 0.675", "1.0, 1.0", "1.5, 1.0", "-0.5, 0.35"})
  @DisplayName("one-shots grow louder towards the heart")
  void additionVolume(final float closeness, final float expected) {
    assertEquals(expected, AmbienceMix.additionVolume(closeness), 0.0001F);
  }
}
