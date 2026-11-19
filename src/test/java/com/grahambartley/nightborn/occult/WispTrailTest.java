package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WispTrailTest {

  @ParameterizedTest(name = "age {0}, fading {1}: sheds {2}")
  @CsvSource({
    "0, false, true",
    "1, false, false",
    "2, false, false",
    "3, false, true",
    "1, true, true",
    "2, true, true"
  })
  @DisplayName("a familiar sheds a wisp every few ticks, and every tick while it fades")
  void sheds(final int age, final boolean fading, final boolean expected) {
    assertEquals(expected, WispTrail.sheds(age, fading));
  }

  @ParameterizedTest(name = "roll {0}: offset {1}")
  @CsvSource({"0.0, -0.25", "0.5, 0.0", "1.0, 0.25", "0.75, 0.125"})
  @DisplayName("a wisp leaves from within a quarter block of the familiar's feet")
  void offset(final double roll, final double expected) {
    assertEquals(expected, WispTrail.offset(roll), 1.0e-9);
  }

  @ParameterizedTest(name = "feet ({0}, {1}, {2}), yaw {3}, rolls {4} {5}: wisp at ({6}, {7}, {8})")
  @CsvSource({
    "0, 64, 0, 0, 0.5, 0.5, 0, 64.25, -0.35",
    "0, 64, 0, 180, 0.5, 0.5, 0, 64.25, 0.35",
    "0, 64, 0, 90, 0.5, 0.5, 0.35, 64.25, 0",
    "10, 70, -5, 0, 0.0, 1.0, 9.75, 70.25, -5.1",
    "-3, 0, 2, -90, 1.0, 0.0, -3.1, 0.25, 1.75"
  })
  @DisplayName(
      "a wisp rises just above the hem, behind the way the familiar faces, spread a little")
  void spawnAt(
      final double x,
      final double y,
      final double z,
      final float yaw,
      final double rollX,
      final double rollZ,
      final double expectedX,
      final double expectedY,
      final double expectedZ) {
    final Vec3d at = WispTrail.spawnAt(new Vec3d(x, y, z), yaw, rollX, rollZ);
    assertEquals(expectedX, at.x, 1.0e-9);
    assertEquals(expectedY, at.y, 1.0e-9);
    assertEquals(expectedZ, at.z, 1.0e-9);
  }
}
