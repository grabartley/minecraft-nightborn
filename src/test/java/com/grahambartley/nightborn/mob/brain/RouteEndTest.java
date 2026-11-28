package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.List;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class RouteEndTest {

  private static Path path() {
    return new Path(
        List.of(new PathNode(0, 64, 0), new PathNode(1, 64, 0), new PathNode(6, 64, 3)),
        new BlockPos(6, 64, 3),
        true);
  }

  @ParameterizedTest(name = "walked {0} nodes: ends {1}")
  @CsvSource({"0, true", "2, true", "3, false"})
  @DisplayName("a route ends at its last node until the mob has walked all of it")
  void end(final int walked, final boolean ends) {
    final Path path = path();
    for (int i = 0; i < walked; i++) {
      path.next();
    }
    final Vec3d end = RouteEnd.of(path);
    if (ends) {
      assertEquals(new Vec3d(6.5, 64, 3.5), end);
    } else {
      assertNull(end);
    }
  }

  @Test
  @DisplayName("a mob with no route has no end")
  void noRoute() {
    assertNull(RouteEnd.of((Path) null));
  }

  @ParameterizedTest(name = "end {0}: {1}")
  @CsvSource({"true, 5", "false, NaN"})
  @DisplayName("the distance to a route's end is missing when there is no route")
  void distance(final boolean hasEnd, final double distance) {
    assertEquals(
        distance,
        RouteEnd.distance(new Vec3d(0, 64, 0), hasEnd ? new Vec3d(3, 64, 4) : null),
        1.0E-9);
  }

  @ParameterizedTest(name = "walked {0} nodes: headed for the target {1}")
  @CsvSource({"0, true", "2, true", "3, false"})
  @DisplayName("a route is headed for the spot it was asked for until the mob has walked it")
  void goal(final int walked, final boolean headed) {
    final Path path =
        new Path(
            List.of(new PathNode(0, 64, 0), new PathNode(1, 64, 0), new PathNode(2, 64, 0)),
            new BlockPos(9, 64, 0),
            false);
    for (int i = 0; i < walked; i++) {
      path.next();
    }
    final Vec3d goal = RouteEnd.goal(path);
    if (headed) {
      assertEquals(new Vec3d(9.5, 64, 0.5), goal);
    } else {
      assertNull(goal);
    }
  }

  @ParameterizedTest(name = "reaches {0}: gap {1}")
  @CsvSource({"true, 0", "false, 1"})
  @DisplayName("the route ahead is read from the nodes still to walk and whether it reaches")
  void route(final boolean reaches, final double gap) {
    final Path path =
        new Path(
            List.of(new PathNode(0, 64, 0), new PathNode(3, 64, 0), new PathNode(3, 64, 4)),
            new BlockPos(3, 64, 4),
            reaches);
    path.next();
    final Route route = RouteEnd.route(new Vec3d(0.5, 64, 0.5), path, 32.0);
    assertEquals(gap, route.gapAhead(), 1.0E-9);
    assertEquals(5.0, route.tripLength(), 1.0E-9);
    assertEquals(reaches ? 7.0 / 5.0 : Double.NaN, route.detour(), 1.0E-9);
  }

  @Test
  @DisplayName("a mob with no route knows nothing of the way ahead")
  void noRouteAhead() {
    assertEquals(Route.UNKNOWN, RouteEnd.route(Vec3d.ZERO, null, 32.0));
    assertNull(RouteEnd.goal((Path) null));
  }

  @ParameterizedTest(name = "away from home {0}, route goal {1}: heads for home {2}")
  @CsvSource({"true, true, true", "true, false, true", "false, true, false", "false, false, false"})
  @DisplayName(
      "a mob away from its home heads for home itself, not the next hop of its walk, and"
          + " otherwise for where its route goes")
  void course(final boolean awayFromHome, final boolean hasGoal, final boolean home) {
    final Vec3d homeSpot = new Vec3d(40.5, 64, 0.5);
    final Vec3d hop = new Vec3d(10.5, 64, 0.5);
    final Vec3d course = RouteEnd.course(awayFromHome ? homeSpot : null, hasGoal ? hop : null);
    assertEquals(home ? homeSpot : hasGoal ? hop : null, course);
  }
}
