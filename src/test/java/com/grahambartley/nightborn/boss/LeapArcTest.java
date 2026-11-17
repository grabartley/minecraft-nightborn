package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LeapArcTest {

  @ParameterizedTest(name = "lift {0} at tick {1} of {2} is {3} high")
  @CsvSource({
    "4, 0, 20, 0.0",
    "4, 5, 20, 3.0",
    "4, 10, 20, 4.0",
    "4, 15, 20, 3.0",
    "4, 20, 20, 0.0",
    "4, 30, 20, 0.0",
    "4, 3, 0, 0.0",
    "0, 10, 20, 0.0"
  })
  @DisplayName("a leap rises to its lift half way and comes back down")
  void height(final double lift, final int tick, final int total, final double height) {
    assertEquals(height, LeapArc.height(lift, tick, total), 1.0E-9);
  }

  @ParameterizedTest(name = "{0} ticks with lift {1}")
  @CsvSource({"1, 2.0", "8, 0.6", "22, 3.5", "40, 0.0"})
  @DisplayName("the steps of a leap add up to the whole trip")
  void stepsReachTheEnd(final int total, final double lift) {
    final Vec3d from = new Vec3d(3.0, 64.0, -2.0);
    final Vec3d to = new Vec3d(-9.0, 66.0, 7.5);
    Vec3d at = from;
    for (int tick = 0; tick < total; tick++) {
      at = at.add(LeapArc.step(from, to, lift, tick, total));
    }
    assertEquals(to.x, at.x, 1.0E-9);
    assertEquals(to.y, at.y, 1.0E-9);
    assertEquals(to.z, at.z, 1.0E-9);
  }

  @ParameterizedTest(name = "tick {0} of 10")
  @CsvSource({"0, 0.0, 64.0", "5, 5.0, 66.0", "10, 10.0, 64.0"})
  @DisplayName("a leap moves evenly along the ground while it arcs")
  void at(final int tick, final double x, final double y) {
    final Vec3d at =
        LeapArc.at(new Vec3d(0.0, 64.0, 0.0), new Vec3d(10.0, 64.0, 0.0), 2.0, tick, 10);
    assertEquals(x, at.x, 1.0E-9);
    assertEquals(y, at.y, 1.0E-9);
  }
}
