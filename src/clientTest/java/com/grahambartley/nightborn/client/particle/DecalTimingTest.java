package com.grahambartley.nightborn.client.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DecalTimingTest {

  @ParameterizedTest(name = "gore share {0}, pool {1}, roll {2}: {3} ticks")
  @CsvSource({
    "1.0, false, 0.0, 5400",
    "1.0, false, 1.0, 7200",
    "1.0, false, 0.5, 6300",
    "1.0, true, 0.0, 6210",
    "1.0, true, 1.0, 8280",
    "0.5, false, 0.0, 2700",
    "0.5, true, 1.0, 4140",
    "0.0, false, 0.5, 0",
    "1.0, false, -1.0, 5400",
    "1.0, false, 2.0, 7200"
  })
  @DisplayName("blood lasts four and a half to six minutes, pools longer, reduced gore half")
  void bloodLife(final float share, final boolean pool, final float roll, final int expected) {
    assertEquals(expected, DecalTiming.bloodLife(share, pool, roll));
  }

  @ParameterizedTest(name = "roll {0}: {1} ticks")
  @CsvSource({"0.0, 70", "0.5, 90", "1.0, 110"})
  @DisplayName("vomit keeps its own short life")
  void vomitLife(final float roll, final int expected) {
    assertEquals(expected, DecalTiming.vomitLife(roll));
  }

  @ParameterizedTest(name = "life {0} at age {1} joined by life {2}: {3}")
  @CsvSource({"6000, 1000, 5500, 5500", "6000, 100, 5500, 5900", "6000, 0, 0, 6000"})
  @DisplayName("a joined patch lives as long as the longer of what it had left and the new splat")
  void joinedLife(final int maxAge, final int age, final int incoming, final int expected) {
    assertEquals(expected, DecalTiming.joinedLife(maxAge, age, incoming));
  }

  @ParameterizedTest(name = "life {0} at age {1} with fade {2}: {3}")
  @CsvSource({"6000, 100, 600, 700", "6000, 5800, 600, 6000", "90, 10, 25, 35"})
  @DisplayName("a retired decal fades out from where it is, never living longer")
  void retiredLife(final int maxAge, final int age, final int fade, final int expected) {
    assertEquals(expected, DecalTiming.retiredLife(maxAge, age, fade));
  }

  @ParameterizedTest(name = "now {0}, last ticked {1}: stale {2}")
  @CsvSource({"100, 100, false", "140, 100, false", "141, 100, true", "100, 141, true"})
  @DisplayName("a decal that stopped ticking is treated as gone")
  void stale(final long now, final long lastTicked, final boolean expected) {
    assertEquals(expected, DecalTiming.stale(now, lastTicked));
  }
}
