package com.grahambartley.nightborn.client.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

  @ParameterizedTest(name = "life {0} at age {1} with fade {2}: life {3}, fade {4}")
  @CsvSource({
    "6000, 100, 600, 160, 60",
    "6000, 5700, 600, 5760, 120",
    "6000, 5550, 600, 5610, 80",
    "6000, 5950, 600, 6000, 600",
    "90, 10, 25, 70, 60",
    "90, 70, 25, 90, 25"
  })
  @DisplayName("a retired decal fades out quickly, carrying on from where its fade was")
  void retired(final int maxAge, final int age, final int fade, final int life, final int newFade) {
    assertEquals(new DecalTiming.Retirement(life, newFade), DecalTiming.retired(maxAge, age, fade));
  }

  @ParameterizedTest(name = "life {0} at age {1} with fade {2}")
  @CsvSource({
    "6000, 100, 600",
    "6000, 5500, 600",
    "6000, 5700, 600",
    "6000, 5880, 600",
    "90, 10, 25"
  })
  @DisplayName("retiring never brings faded pixels back")
  void retiringNeverBringsPixelsBack(final int maxAge, final int age, final int fade) {
    final DecalTiming.Retirement retired = DecalTiming.retired(maxAge, age, fade);
    assertTrue(
        DecalFade.alpha(age, retired.maxAge(), retired.fadeTicks())
            <= DecalFade.alpha(age, maxAge, fade) + 2.0e-3F);
  }
}
