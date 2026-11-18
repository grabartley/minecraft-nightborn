package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PounceArcTest {

  @ParameterizedTest(name = "to ({0}, {1}, {2})")
  @CsvSource({"10, 0, 0", "6, 2, -4", "1, 0, 1", "0, 3, 8"})
  @DisplayName("the leap lands on the target when followed tick by tick under gravity")
  void lands(final double x, final double y, final double z) {
    final Vec3d target = new Vec3d(x, y, z);
    Vec3d at = Vec3d.ZERO;
    Vec3d velocity = PounceArc.launch(Vec3d.ZERO, target);
    final int ticks = PounceArc.flightTicks(target.horizontalLength());
    for (int i = 0; i < ticks; i++) {
      at = at.add(velocity);
      velocity = velocity.subtract(0.0, PounceArc.GRAVITY, 0.0);
    }
    assertEquals(x, at.x, 1.0E-6);
    assertEquals(z, at.z, 1.0E-6);
    assertEquals(y, at.y, 1.0E-6);
  }

  @ParameterizedTest(name = "distance {0}: caught {1}")
  @CsvSource({"1.5, true", "2.0, true", "2.5, false"})
  @DisplayName("the pounce lands when the pouncer reaches the target")
  void catches(final double distance, final boolean caught) {
    assertEquals(caught, PounceArc.caught(distance));
  }
}
