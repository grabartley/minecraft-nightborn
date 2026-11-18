package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ZoneHitsTest {

  @ParameterizedTest(name = "vampire {0}, weakening {1}: weakens {2}")
  @CsvSource({
    "true, true, true",
    "true, false, false",
    "false, true, false",
    "false, false, false"
  })
  @DisplayName("a mark weakens only vampires, and only when it carries a weakening")
  void weakens(final boolean vampire, final boolean weakening, final boolean weakens) {
    final ZoneHit hit =
        new ZoneHit(
            Optional.empty(),
            List.of(),
            weakening ? Optional.of(new ZoneWeaken("holy_water", 2, 100)) : Optional.empty(),
            0.0F,
            0);
    assertEquals(weakens, ZoneHits.weakens(vampire, hit));
  }
}
