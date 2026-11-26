package com.grahambartley.nightborn.batform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BatTurnBackTest {

  private static final double BAT = 0.9;
  private static final double STANDING = 1.8;
  private static final double OPEN = Double.POSITIVE_INFINITY;
  private static final double NO_FLOOR = Double.NEGATIVE_INFINITY;

  @ParameterizedTest(name = "bat at {0}, ceiling {1}, floor {2}: stands at {3}")
  @CsvSource({
    "64.0, Infinity, -Infinity, 64.0",
    "64.0, 70.0, 63.0, 64.0",
    "64.0, 65.8, 64.0, 64.0",
    "90.7, 92.0, 87.0, 90.2",
    "91.1, 92.0, 87.0, 90.2",
    "64.5, 66.0, 64.0, 64.2",
    "64.0, 66.0, 64.0, 64.0"
  })
  @DisplayName("a bat under a ceiling turns back lower, by no more than it grows")
  void standsLower(
      final double y, final double ceiling, final double floor, final double standsAt) {
    final Optional<Vec3d> feet =
        BatTurnBack.feet(new Vec3d(0.5, y, 0.5), Optional.empty(), BAT, STANDING, ceiling, floor);
    assertTrue(feet.isPresent(), "there is room to stand");
    assertEquals(standsAt, feet.get().y, 1.0E-6);
    assertEquals(0.5, feet.get().x);
    assertEquals(0.5, feet.get().z);
  }

  @ParameterizedTest(name = "bat at {0}, ceiling {1}, floor {2}: no room")
  @CsvSource({
    "64.0, 65.0, 64.0",
    "64.0, 65.5, 64.0",
    "64.2, 65.0, 63.0",
    "64.0, 65.7, 64.0",
    "88.0, 89.0, 88.0"
  })
  @DisplayName("a gap too small to stand in, or too far to drop, keeps the vampire a bat")
  void refused(final double y, final double ceiling, final double floor) {
    assertTrue(
        BatTurnBack.feet(new Vec3d(0.5, y, 0.5), Optional.empty(), BAT, STANDING, ceiling, floor)
            .isEmpty());
  }

  @Test
  @DisplayName("a hanging bat stands under the block it hangs from, whatever is around it")
  void hanging() {
    final Optional<Vec3d> feet =
        BatTurnBack.feet(
            new Vec3d(3.5, 70.0, 3.5),
            Optional.of(new BlockPos(3, 71, 3)),
            BAT,
            STANDING,
            OPEN,
            NO_FLOOR);
    assertEquals(Optional.of(new Vec3d(3.5, 71.0 - STANDING, 3.5)), feet);
  }
}
