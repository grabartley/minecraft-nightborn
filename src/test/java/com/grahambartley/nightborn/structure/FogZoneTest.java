package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.math.BlockBox;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FogZoneTest {

  private static final BlockBox HEART = new BlockBox(0, 60, 0, 9, 69, 9);
  private static final BlockBox WHOLE = new BlockBox(-40, 60, 0, 9, 69, 9);
  private static final FogZone ZONE = FogZone.around(HEART, WHOLE);

  @ParameterizedTest(name = "heart {0}..{1} gives centre {2}")
  @CsvSource({"0, 9, 5.0", "-4, 3, 0.0", "10, 10, 10.5"})
  @DisplayName("the heart sits at the middle of the heart piece")
  void centresOnTheHeart(final int min, final int max, final double centre) {
    final FogZone zone = FogZone.around(new BlockBox(min, min, min, max, max, max), WHOLE);
    assertEquals(centre, zone.x());
    assertEquals(centre, zone.y());
    assertEquals(centre, zone.z());
  }

  @ParameterizedTest(name = "at {0},{1},{2} closeness {3}")
  @CsvSource({
    "5, 65, 5, 1.0",
    "-40, 65, 5, 0.353",
    "-64, 65, 5, 0.0",
    "34, 65, 5, 0.0",
    "5, 65, 34, 0.0",
    "5, 200, 5, 0.0"
  })
  @DisplayName("closeness is full at the heart and falls to nothing a margin past the structure")
  void closeness(final double x, final double y, final double z, final float expected) {
    assertEquals(expected, ZONE.closeness(x, y, z), 0.01F);
  }

  @ParameterizedTest(name = "walking in along x from {0}")
  @CsvSource({"-64", "34"})
  @DisplayName("fog rises smoothly from nothing as a player walks in from either side")
  void risesSmoothly(final double from) {
    float last = ZONE.closeness(from, 65, 5);
    assertEquals(0.0F, last, 1.0E-6F);
    final double step = from < 5 ? 1.0 : -1.0;
    for (double x = from + step; Math.abs(x - 5) >= 1; x += step) {
      final float now = ZONE.closeness(x, 65, 5);
      assertTrue(now >= last, "closeness fell at x " + x);
      assertTrue(now - last < 0.1F, "closeness jumped at x " + x);
      last = now;
    }
  }

  @ParameterizedTest(name = "at {0},{1},{2} inside: {3}")
  @CsvSource({"5, 65, 5, true", "-50, 65, 5, true", "-64, 65, 5, false", "5, 200, 5, false"})
  @DisplayName("a point is inside a zone only while it has some fog")
  void contains(final double x, final double y, final double z, final boolean inside) {
    assertEquals(inside, ZONE.contains(x, y, z));
  }
}
