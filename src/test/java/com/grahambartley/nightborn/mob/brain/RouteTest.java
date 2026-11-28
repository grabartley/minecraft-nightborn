package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RouteTest {

  private static final Vec3d FROM = new Vec3d(0, 64, 0);

  @ParameterizedTest(
      name = "via ({0}, {1}) to ({2}, {3}), reaches {4}: gap {5}, detour {6}, trip {7}")
  @CsvSource({
    "5, 0, 10, 0, true, 0, 1, 10",
    "0, 10, 10, 0, true, 0, 2.414214, 10",
    "5, 0, 10, 0, false, 1, NaN, 10",
    "0, 0, 0.5, 0, true, 0, 1, 0.5",
    "0, 30, 0, 40, true, 0, 1, 40"
  })
  @DisplayName(
      "a route that falls short has a gap ahead, and one that reaches compares its walk with the"
          + " straight line")
  void shape(
      final double viaX,
      final double viaZ,
      final double goalX,
      final double goalZ,
      final boolean reaches,
      final double gap,
      final double detour,
      final double trip) {
    final Vec3d goal = new Vec3d(goalX, 64, goalZ);
    final Route route = Route.of(FROM, List.of(new Vec3d(viaX, 64, viaZ), goal), goal, reaches);
    assertEquals(gap, route.gapAhead(), 1.0E-6);
    assertEquals(detour, route.detour(), 1.0E-6);
    assertEquals(trip, route.tripLength(), 1.0E-6);
  }
}
