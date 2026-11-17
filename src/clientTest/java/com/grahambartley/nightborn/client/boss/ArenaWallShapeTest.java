package com.grahambartley.nightborn.client.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ArenaWallShapeTest {

  @ParameterizedTest(name = "radius {0}")
  @ValueSource(ints = {1, 4, 8, 16, 48})
  @DisplayName("the wall is a closed ring of short panels, every corner on the arena's edge")
  void ring(final int radius) {
    final int segments = ArenaWallShape.segments(radius);
    assertTrue(segments >= ArenaWallShape.FEWEST);
    for (int i = 0; i < segments; i++) {
      final double x = ArenaWallShape.x(i, segments, radius);
      final double z = ArenaWallShape.z(i, segments, radius);
      assertEquals(radius, Math.hypot(x, z), 1.0E-9);
      final double panel =
          Math.hypot(
              ArenaWallShape.x(i + 1, segments, radius) - x,
              ArenaWallShape.z(i + 1, segments, radius) - z);
      assertTrue(panel <= ArenaWallShape.SEGMENT + 1.0E-9 || segments == ArenaWallShape.FEWEST);
    }
    assertEquals(
        ArenaWallShape.x(0, segments, radius),
        ArenaWallShape.x(segments, segments, radius),
        1.0E-9);
    assertEquals(
        ArenaWallShape.z(0, segments, radius),
        ArenaWallShape.z(segments, segments, radius),
        1.0E-9);
  }

  @ParameterizedTest(name = "{0} blocks from the wall: alpha {1}")
  @CsvSource({"0, 0.75", "6, 0.45", "-6, 0.45", "12, 0.15", "-12, 0.15", "40, 0.15", "-40, 0.15"})
  @DisplayName("the wall is strongest up close and fades to a faint veil from either side")
  void alpha(final double fromWall, final float alpha) {
    assertEquals(alpha, ArenaWallShape.alpha(fromWall), 1.0E-6F);
  }

  @ParameterizedTest(name = "angle {0}, height {1}, time {2}, strength {3}")
  @CsvSource({"0, 0, 0, 1.0", "1.3, 150, 200, 1.0", "4.4, 166, 99999, 1.8", "6.2, 144, 31, 1.8"})
  @DisplayName("the wall never ripples further than its swell and shiver allow")
  void ripple(final double angle, final double height, final double time, final float strength) {
    assertTrue(
        Math.abs(ArenaWallShape.ripple(angle, height, time, strength))
            <= strength * (ArenaWallShape.SWELL + ArenaWallShape.SHIVER) + 1.0E-9);
  }

  @ParameterizedTest(name = "fighting {0}: strength {1}")
  @CsvSource({"false, 1.0", "true, 1.8"})
  @DisplayName("the wall stirs harder while a fight is on")
  void strength(final boolean fighting, final float strength) {
    assertEquals(strength, ArenaWallShape.strength(fighting));
  }

  @ParameterizedTest(name = "at tick {0}")
  @ValueSource(doubles = {0.0, 2.8, 5.0, 9.0, 14.0, 27.9, 1000.5})
  @DisplayName("the wall beats like a heart between its resting glow and full strength")
  void pulse(final double time) {
    final float pulse = ArenaWallShape.pulse(time);
    assertTrue(pulse >= ArenaWallShape.REST && pulse <= 1.0F, "pulse " + pulse);
    assertEquals(pulse, ArenaWallShape.pulse(time + ArenaWallShape.BEAT_TICKS), 1.0E-4F);
  }

  @ParameterizedTest(name = "{0} of a turn round the wall: #{1}")
  @CsvSource({"0.0, 8E0A14", "0.125, 4A0A5E", "0.25, 2A0008", "0.375, 9C1050", "0.5, 8E0A14"})
  @DisplayName("the wall's colour runs through its palette twice round the ring")
  void tint(final double turn, final String colour) {
    assertEquals(
        Integer.parseInt(colour, 16), ArenaWallShape.tint(turn * 2.0 * Math.PI, 0.0), colour);
  }

  @ParameterizedTest(name = "{0} of the way: #{1}")
  @CsvSource({"0.0, 000000", "0.5, 804020", "1.0, FF8040"})
  @DisplayName("colours blend channel by channel")
  void mix(final double blend, final String colour) {
    assertEquals(Integer.parseInt(colour, 16), ArenaWallShape.mix(0x000000, 0xFF8040, blend));
  }

  @ParameterizedTest(name = "at tick {0} the shapes show {1}")
  @CsvSource({"0, 0.0", "60, 0.25", "120, 1.0", "180, 0.25", "240, 0.0", "360, 1.0"})
  @DisplayName("the shapes in the fog surface and sink smoothly and never flash")
  void surfacing(final double time, final float shown) {
    assertEquals(shown, ArenaWallShape.surfacing(time), 0.0001F);
    assertTrue(
        Math.abs(ArenaWallShape.surfacing(time + 1.0) - ArenaWallShape.surfacing(time)) < 0.03F);
  }
}
