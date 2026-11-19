package com.grahambartley.nightborn.faith;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FlameTest {

  @ParameterizedTest(name = "until {0}, now {1} -> burning {2}, {3} left")
  @CsvSource({"100, 0, true, 100", "100, 99, true, 1", "100, 100, false, 0", "100, 150, false, 0"})
  @DisplayName("a flame burns until its time and no longer")
  void burns(final long until, final long now, final boolean burning, final long left) {
    final Flame flame = new Flame(until, 3.0F, 100);
    assertEquals(burning, flame.burning(now));
    assertEquals(left, flame.ticksLeft(now));
  }

  @ParameterizedTest(name = "until {0}, damage {1}, burn {2}")
  @CsvSource({"1800, 3, 100", "0, 0, 0"})
  @DisplayName("a flame survives being saved and loaded")
  void roundTrips(final long until, final float damage, final int burn) {
    final Flame flame = new Flame(until, damage, burn);
    assertEquals(
        flame,
        Flame.CODEC
            .parse(JsonOps.INSTANCE, Flame.CODEC.encodeStart(JsonOps.INSTANCE, flame).getOrThrow())
            .getOrThrow());
  }
}
