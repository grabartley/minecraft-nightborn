package com.grahambartley.nightborn.seeker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SeeingTest {

  private static final SightPower POWER = new SightPower(24.0F, 120, 0, List.of());

  @ParameterizedTest(name = "cast at {0}, now {1}: lasts {2}")
  @CsvSource({"0, 0, true", "0, 119, true", "0, 120, false", "100, 219, true"})
  @DisplayName("true sight lasts its time from the cast")
  void lasts(final long cast, final long now, final boolean lasts) {
    assertEquals(lasts, Seeing.of(cast, POWER).lasts(now));
  }

  @ParameterizedTest(name = "seen {0} times before -> first {1}")
  @CsvSource({"0, true", "1, false", "3, false"})
  @DisplayName("a vampire is told it is seen only the first time")
  void firstSight(final int before, final boolean first) {
    final Seeing seeing = Seeing.of(0L, POWER);
    final UUID vampire = new UUID(0L, 7L);
    for (int i = 0; i < before; i++) {
      seeing.firstSight(vampire);
    }
    assertEquals(first, seeing.firstSight(vampire));
  }
}
