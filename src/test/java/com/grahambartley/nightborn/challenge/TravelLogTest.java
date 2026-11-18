package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TravelLogTest {

  @ParameterizedTest(name = "from {0} carried, moving {1} at tick gap {2} carries {3}")
  @CsvSource({"0.0, 1.5, 1, 1.5", "3.0, 2.0, 1, 5.0", "3.0, 9.0, 1, 3.0", "3.0, 1.0, 40, 3.0"})
  void countsSteadyTravelAndIgnoresJumpsAndGaps(
      final double carried, final double moved, final long gap, final double after) {
    final TravelLog.Leg leg = new TravelLog.Leg(Vec3d.ZERO, 10L, carried);
    assertEquals(after, leg.next(new Vec3d(moved, 0.0, 0.0), 10L + gap).carried(), 1.0E-9);
  }

  @ParameterizedTest(name = "{0} carried reports {1} whole blocks")
  @CsvSource({"15.9, 0", "16.0, 16", "20.7, 20"})
  void reportsInWholeBlocks(final double carried, final int whole) {
    final TravelLog.Leg leg = new TravelLog.Leg(Vec3d.ZERO, 0L, carried);
    assertEquals(whole, leg.whole());
    assertEquals(carried - whole, leg.spent(whole).carried(), 1.0E-9);
  }
}
