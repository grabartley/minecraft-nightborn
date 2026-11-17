package com.grahambartley.nightborn.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BatHangViewTest {

  @ParameterizedTest(name = "turned {0}, hanging {1} -> {2}")
  @CsvSource({
    "0.0, true, 0.125",
    "0.5, true, 0.625",
    "1.0, true, 1.0",
    "1.0, false, 0.875",
    "0.05, false, 0.0",
    "0.0, false, 0.0"
  })
  @DisplayName("the view turns over a few ticks rather than flipping at once")
  void step(final float turned, final boolean hanging, final float expected) {
    assertEquals(expected, BatHangView.step(turned, hanging), 1.0E-6F);
  }

  @ParameterizedTest(name = "third person {0}, turned {1} -> {2} degrees")
  @CsvSource({
    "false, 0.0, 0.0",
    "false, 0.5, 90.0",
    "false, 1.0, 180.0",
    "false, 0.25, 28.125",
    "true, 1.0, 0.0",
    "true, 0.5, 0.0"
  })
  @DisplayName("a hanging bat sees the world upside down in first person only")
  void angle(final boolean thirdPerson, final double turned, final double degrees) {
    assertEquals(degrees, Math.toDegrees(BatHangView.angle(thirdPerson, turned)), 1.0E-4);
  }

  @ParameterizedTest(name = "third person {0}, tick delta {1}")
  @CsvSource({"false, 0.0", "false, 0.5", "false, 1.0", "true, 0.5"})
  @DisplayName("a view that never hung stays the right way up")
  void roll(final boolean thirdPerson, final float tickDelta) {
    assertEquals(0.0F, BatHangView.roll(thirdPerson, tickDelta));
  }
}
