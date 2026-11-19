package com.grahambartley.nightborn.seeker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GraveListenerTest {

  @ParameterizedTest(name = "sneaking {0}, vampire {1}, range {2} -> {3}")
  @CsvSource({
    "true, false, 12, true",
    "false, false, 12, false",
    "true, true, 12, false",
    "true, false, 0, false"
  })
  @DisplayName("a human who has the skill listens only while sneaking")
  void listens(
      final boolean sneaking, final boolean vampire, final float range, final boolean listens) {
    assertEquals(listens, GraveListener.listens(sneaking, vampire, range));
  }

  @ParameterizedTest(name = "vampire {0}, asleep {1} -> {2}")
  @CsvSource({"true, true, true", "true, false, false", "false, true, false"})
  @DisplayName("only a sleeping vampire is heard")
  void hears(final boolean vampire, final boolean asleep, final boolean heard) {
    assertEquals(heard, GraveListener.hears(vampire, asleep));
  }
}
