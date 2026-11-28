package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SharedScansTest {

  private static final double REUSE = 8.0;

  private final AtomicInteger scans = new AtomicInteger();
  private final Function<Vec3d, List<String>> scanner =
      at -> {
        scans.incrementAndGet();
        return List.of("scan " + scans.get());
      };

  @Test
  @DisplayName("a raid of ten builds one group picture per think tick")
  void raidOfTenScansOnce() {
    final SharedScans<String> shared = new SharedScans<>(REUSE);
    for (int raider = 0; raider < 10; raider++) {
      assertEquals(
          List.of("scan 1"), shared.near(100, new Vec3d(raider % 5, 64, raider / 5 * 2), scanner));
    }
    assertEquals(1, scans.get());
    for (int raider = 0; raider < 10; raider++) {
      shared.near(140, new Vec3d(raider % 5, 64, raider / 5 * 2), scanner);
    }
    assertEquals(2, scans.get());
  }

  @ParameterizedTest(name = "a second mob {0} blocks away on tick {1}: scans {2}")
  @CsvSource({"0, 100, 1", "8, 100, 1", "8.5, 100, 2", "30, 100, 2", "0, 101, 2"})
  @DisplayName(
      "a mob reuses a picture taken this tick close enough to cover it, and scans afresh when it"
          + " is too far off or the tick has moved on")
  void reusesOnlyNearbyThisTick(final double away, final long tick, final int expected) {
    final SharedScans<String> shared = new SharedScans<>(REUSE);
    shared.near(100, Vec3d.ZERO, scanner);
    shared.near(tick, new Vec3d(away, 0, 0), scanner);
    assertEquals(expected, scans.get());
  }
}
