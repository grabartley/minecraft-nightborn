package com.grahambartley.nightborn.client.formview;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RippleTest {

  private static final Ripple RIPPLE = new Ripple(Vec3d.ZERO, 10.0, 20.0F, 0.8F);

  @ParameterizedTest(name = "at {0}s the ring is {1} blocks out")
  @CsvSource({"9.0, 0.0", "10.0, 0.0", "10.5, 12.0", "10.8, 19.2", "11.0, 20.0", "15.0, 20.0"})
  @DisplayName("a ripple spreads at its speed and stops at its range")
  void grows(final double now, final float radius) {
    assertEquals(radius, RIPPLE.radius(now, 24.0F), 1.0E-4F);
  }

  @ParameterizedTest(name = "at {0}s it shows at {1}")
  @CsvSource({"10.0, 0.8", "10.5, 0.6", "11.0, 0.4", "12.0, 0.0", "13.0, 0.0"})
  @DisplayName("a ripple fades evenly over its life from its starting strength")
  void fades(final double now, final float alpha) {
    assertEquals(alpha, RIPPLE.alpha(now, 2.0F), 1.0E-4F);
  }

  @ParameterizedTest(name = "at {0}s over: {1}")
  @CsvSource({"10.0, false", "11.99, false", "12.0, true", "20.0, true"})
  @DisplayName("a ripple is over once its life has run out")
  void ends(final double now, final boolean over) {
    assertEquals(over, RIPPLE.over(now, 2.0F));
  }
}
