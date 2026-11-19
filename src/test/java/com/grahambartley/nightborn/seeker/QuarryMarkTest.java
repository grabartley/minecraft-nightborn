package com.grahambartley.nightborn.seeker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class QuarryMarkTest {

  private static final UUID QUARRY = new UUID(0L, 1L);
  private static final UUID OTHER = new UUID(0L, 2L);

  @ParameterizedTest(name = "until {0}, now {1}: lasts {2}")
  @CsvSource({"1200, 0, true", "1200, 1199, true", "1200, 1200, false", "1200, 5000, false"})
  @DisplayName("a mark lasts until its time runs out")
  void lasts(final long until, final long now, final boolean lasts) {
    assertEquals(lasts, new QuarryMark(QUARRY, until, 0.15, 0).lasts(now));
  }

  @ParameterizedTest(name = "on the quarry {0}")
  @CsvSource({"true", "false"})
  @DisplayName("a mark is on its quarry only")
  void on(final boolean quarry) {
    assertEquals(quarry, new QuarryMark(QUARRY, 1200L, 0.15, 0).on(quarry ? QUARRY : OTHER));
  }
}
