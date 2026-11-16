package com.grahambartley.nightborn.vampire;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ImpulseTest {

  @ParameterizedTest(name = "({0}, {1}, {2}) -> ({3}, {4})")
  @CsvSource({"3, 0, 4, 0.6, 0.8", "0, 9, -2, 0.0, -1.0", "0, 5, 0, 0.0, 0.0", "0, 0, 0, 0.0, 0.0"})
  @DisplayName("ahead is the level unit heading, or nothing when the way is straight up or down")
  void ahead(final double x, final double y, final double z, final double ax, final double az) {
    final Vec3d ahead = Impulse.ahead(new Vec3d(x, y, z));
    assertEquals(ax, ahead.x, 0.0001);
    assertEquals(0.0, ahead.y, 0.0001);
    assertEquals(az, ahead.z, 0.0001);
  }

  @ParameterizedTest(name = "push {0}, lift {1}")
  @CsvSource({"2.0, 0.5", "0.0, 1.0", "1.5, 0.0"})
  @DisplayName("the push scales the heading and adds the lift")
  void push(final double push, final double lift) {
    final Vec3d out = Impulse.push(new Vec3d(0.6, 0.0, 0.8), push, lift);
    assertEquals(0.6 * push, out.x, 0.0001);
    assertEquals(lift, out.y, 0.0001);
    assertEquals(0.8 * push, out.z, 0.0001);
  }
}
