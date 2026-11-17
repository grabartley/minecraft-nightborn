package com.grahambartley.nightborn.age;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NightWatchTest {

  @ParameterizedTest(name = "night {0}, {1} ticks, counted {2}")
  @CsvSource({"0, 0, false", "12, 5001, true", "-3, 9999, false"})
  @DisplayName("the night being lived is saved unchanged")
  void roundTrips(final long night, final int ticks, final boolean counted) {
    final NightWatch watch = new NightWatch(night, ticks, counted);
    assertEquals(
        watch,
        NightWatch.CODEC
            .parse(
                JsonOps.INSTANCE,
                NightWatch.CODEC.encodeStart(JsonOps.INSTANCE, watch).getOrThrow())
            .getOrThrow());
  }

  @Test
  @DisplayName("ticks lived are never negative")
  void clampsTicks() {
    assertEquals(0, new NightWatch(1, -40, false).ticks());
    assertFalse(NightWatch.NONE.counted());
    assertTrue(NightWatch.NONE.night() == NightWatch.NO_NIGHT);
  }
}
