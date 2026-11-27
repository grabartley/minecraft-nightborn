package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class StallCountTest {

  private static final BlockPos TARGET = new BlockPos(10, 64, 0);

  private static int ticksUntilGiveUp(final double step) {
    final StallCount count = new StallCount();
    double toNode = 100.0;
    for (int tick = 0; toNode > 0.0; tick++) {
      if (count.gaveUp(TARGET, 3, toNode)) {
        return tick;
      }
      toNode = Math.max(0.0, toNode - step);
    }
    return -1;
  }

  @ParameterizedTest(name = "closing {0} a tick")
  @ValueSource(doubles = {0.0, 0.001, 0.003})
  @DisplayName("a mob that gets no nearer its next path point gives up after three seconds")
  void givesUpWhenNoNearer(final double step) {
    assertEquals(StallCount.GIVE_UP_TICKS, ticksUntilGiveUp(step));
  }

  @ParameterizedTest(name = "closing {0} a tick")
  @ValueSource(doubles = {0.05, 0.1, 0.2})
  @DisplayName("a mob that keeps closing on its next path point never gives up")
  void keepsGoingWhileClosing(final double step) {
    assertEquals(-1, ticksUntilGiveUp(step));
  }

  @ParameterizedTest(name = "stalled {0} ticks, then {1}")
  @CsvSource({
    "30, a new target, 11, 3",
    "59, a new target, 11, 3",
    "30, the next path point, 10, 4",
    "59, the next path point, 10, 4"
  })
  @DisplayName("a new target or reaching the next path point starts the count again")
  void freshTargetOrPointRestartsCount(
      final int stalled, final String name, final int targetX, final int node) {
    final StallCount count = new StallCount();
    for (int tick = 0; tick <= stalled; tick++) {
      assertFalse(count.gaveUp(TARGET, 3, 5.0));
    }
    final BlockPos next = new BlockPos(targetX, 64, 0);
    for (int tick = 0; tick < StallCount.GIVE_UP_TICKS; tick++) {
      assertFalse(count.gaveUp(next, node, 5.0));
    }
    assertTrue(count.gaveUp(next, node, 5.0));
  }

  @Test
  @DisplayName("standing idle never counts as stalled")
  void idleNeverStalls() {
    final StallCount count = new StallCount();
    for (int tick = 0; tick < StallCount.GIVE_UP_TICKS * 3; tick++) {
      assertFalse(count.gaveUp(null, 0, 0.0));
    }
  }
}
