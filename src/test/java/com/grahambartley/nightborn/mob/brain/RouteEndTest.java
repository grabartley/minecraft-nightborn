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
}
