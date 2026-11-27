package com.grahambartley.nightborn.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.OptionalDouble;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DecalFootprintTest {

  @ParameterizedTest(name = "centre x {0}, half extent {1}, {2} wide ledge: {3}")
  @CsvSource({
    "4.5, 0.4, 1, true",
    "4.5, 0.6, 1, false",
    "4.9, 0.2, 1, false",
    "4.9, 0.2, 2, true",
    "4.9, 1.2, 2, false",
    "5.0, 0.45, 2, true"
  })
  @DisplayName("a splat holds only when every face it covers holds blood")
  void holds(final double x, final double halfExtent, final int width, final boolean holds) {
    assertEquals(
        holds,
        DecalFootprint.holds(
            DecalTestWorlds.ledge(width), new Vec3d(x, 64.0, 9.5), Direction.UP, halfExtent));
  }

  @ParameterizedTest(name = "centre x {0}, half extent {1}: {2}")
  @CsvSource({"4.5, 0.4, 0.4", "4.5, 0.6, 0.5", "4.9, 0.2, 0.1", "4.9, 0.6, ", "3.5, 0.2, "})
  @DisplayName("a splat that would hang over an edge shrinks to fit its own face, or is dropped")
  void fit(final double x, final double halfExtent, final Double fitted) {
    final OptionalDouble fit =
        DecalFootprint.fit(
            DecalTestWorlds.ledge(1), new Vec3d(x, 64.0, 9.5), Direction.UP, halfExtent);

    assertEquals(fitted == null, fit.isEmpty());
    if (fitted != null) {
      assertEquals(fitted, fit.getAsDouble(), 1.0e-9);
    }
  }

  @ParameterizedTest(name = "half size {0} spun {1} rad at x {2}: {3}")
  @CsvSource({
    "0.2, 0.0, 4.5, 0.2",
    "0.4, 0.7853982, 4.5, 0.35355339",
    "0.2, 0.0, 4.9, 0.1",
    "0.3, 0.0, 4.9, ",
    "0.3, 0.0, 3.5, "
  })
  @DisplayName("a spun splat keeps its size when it fits, and shrinks to its own face when not")
  void fittedHalfSize(
      final double halfSize, final float spin, final double x, final Double fitted) {
    final OptionalDouble fit =
        DecalFootprint.fittedHalfSize(
            DecalTestWorlds.ledge(1), new Vec3d(x, 64.0, 9.5), Direction.UP, halfSize, spin);

    assertEquals(fitted == null, fit.isEmpty());
    if (fitted != null) {
      assertEquals(fitted, fit.getAsDouble(), 1.0e-6);
    }
  }

  @ParameterizedTest(name = "half size {0} spun {1} rad: {2}")
  @CsvSource({
    "0.5, 0.0, 0.5",
    "0.5, 1.5707964, 0.5",
    "0.5, 0.7853982, 0.70710678",
    "0.2, 3.1415927, 0.2"
  })
  @DisplayName("a spun splat reaches further along the face than its half size")
  void halfExtent(final double halfSize, final float spin, final double extent) {
    assertEquals(extent, DecalFootprint.halfExtent(halfSize, spin), 1.0e-6);
  }

  @ParameterizedTest(name = "{0} along {1}: {2}")
  @CsvSource({
    "4.5, 64.0, 9.5, Y, 0.5",
    "4.1, 64.0, 9.5, Y, 0.1",
    "4.5, 64.0, 9.8, Y, 0.2",
    "4.0, 63.3, 9.5, X, 0.3"
  })
  @DisplayName("the room a splat has is the distance to the nearest edge of its face")
  void roomInCell(
      final double x,
      final double y,
      final double z,
      final Direction.Axis normal,
      final double room) {
    assertEquals(room, DecalFootprint.roomInCell(new Vec3d(x, y, z), normal), 1.0e-9);
  }
}
