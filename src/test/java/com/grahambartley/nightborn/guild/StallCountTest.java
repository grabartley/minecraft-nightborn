package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class StallCountTest {

  private static final BlockPos TARGET = new BlockPos(40, 64, 0);

  private static int ticksUntilGiveUp(final double step, final double sway) {
    final StallCount count = new StallCount();
    for (int tick = 0; tick < 400; tick++) {
      final Vec3d pos = new Vec3d(tick * step + (tick % 2 == 0 ? sway : -sway), 64, 0);
      if (count.gaveUp(TARGET, pos)) {
        return tick;
      }
    }
    return -1;
  }

  @ParameterizedTest(name = "moving {0} a tick, swaying {1}")
  @CsvSource({"0.0, 0.0", "0.0, 0.4", "0.005, 0.3", "0.01, 0.0"})
  @DisplayName("a walking mob that stays on one spot gives up after three seconds")
  void givesUpWhenStuckOnOneSpot(final double step, final double sway) {
    assertEquals(StallCount.GIVE_UP_TICKS, ticksUntilGiveUp(step, sway));
  }

  @ParameterizedTest(name = "moving {0} a tick")
  @ValueSource(doubles = {0.03, 0.1, 0.2})
  @DisplayName("a mob that keeps moving never gives up, however it winds")
  void keepsGoingWhileMoving(final double step) {
    assertEquals(-1, ticksUntilGiveUp(step, 0.0));
  }

  @ParameterizedTest(name = "stalled {0} ticks before a new target")
  @ValueSource(ints = {30, 59})
  @DisplayName("a new target starts the count again")
  void newTargetRestartsCount(final int stalled) {
    final StallCount count = new StallCount();
    for (int tick = 0; tick <= stalled; tick++) {
      assertFalse(count.gaveUp(TARGET, Vec3d.ZERO));
    }
    for (int tick = 0; tick < StallCount.GIVE_UP_TICKS; tick++) {
      assertFalse(count.gaveUp(TARGET.east(), Vec3d.ZERO));
    }
    assertTrue(count.gaveUp(TARGET.east(), Vec3d.ZERO));
  }

  @Test
  @DisplayName("standing idle never counts as stalled")
  void idleNeverStalls() {
    final StallCount count = new StallCount();
    for (int tick = 0; tick < StallCount.GIVE_UP_TICKS * 3; tick++) {
      assertFalse(count.gaveUp(null, Vec3d.ZERO));
    }
  }
}
