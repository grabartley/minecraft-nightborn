package com.grahambartley.nightborn.levitation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.OptionalDouble;
import net.minecraft.util.math.Box;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FloatRoomRulesTest {

  @ParameterizedTest(name = "feet {0}, surface {1}, levitating {2}: ground {3}")
  @CsvSource({
    "64.0, 64.0, false, 64.0",
    "64.0, , false, 64.0",
    "65.0, 64.0, true, 64.0",
    "65.4, 64.5, true, 64.5",
    "70.0, 60.0, true, 69.0",
    "65.0, , true, 64.0",
    "64.2, 64.0, true, 64.0"
  })
  @DisplayName(
      "a floating body is measured from the floor under it, or from a hover below its feet over"
          + " a drop")
  void groundFeet(
      final double feet, final Double surface, final boolean levitating, final double ground) {
    assertEquals(
        ground,
        FloatRoomRules.groundFeet(
            feet,
            surface == null ? OptionalDouble.empty() : OptionalDouble.of(surface),
            levitating),
        1.0E-9);
  }

  @ParameterizedTest(name = "{0} wide, {1} tall at ground {2}")
  @CsvSource({"0.6, 1.95, 64.0", "0.6, 2.0, 64.5", "1.2, 2.5, 10.0"})
  @DisplayName("the floating body sits one hover above the ground and is just inside its size")
  void floatingBody(final double width, final double height, final double ground) {
    final Box box = FloatRoomRules.floatingBody(width, height, 0.5, ground, 0.5);
    assertEquals(ground + LevitationLift.HOVER_HEIGHT + FloatRoomRules.FIT_MARGIN, box.minY, 1e-9);
    assertEquals(
        ground + LevitationLift.HOVER_HEIGHT + height - FloatRoomRules.FIT_MARGIN, box.maxY, 1e-9);
    assertEquals(width - 2 * FloatRoomRules.FIT_MARGIN, box.getLengthX(), 1e-9);
    assertEquals(width - 2 * FloatRoomRules.FIT_MARGIN, box.getLengthZ(), 1e-9);
    assertEquals(0.5, box.getCenter().x, 1e-9);
  }

  @ParameterizedTest(name = "a {2} block opening under a {1} tall body: fits {3}")
  @CsvSource({
    "64.0, 1.95, 2, false",
    "64.0, 1.95, 3, true",
    "64.0, 2.0, 3, true",
    "64.0, 2.5, 3, false",
    "64.5, 2.0, 3, false",
    "64.0, 0.9, 2, true"
  })
  @DisplayName("a floating body only fits an opening a hover taller than itself")
  void fitsUnderCeiling(
      final double ground, final double height, final int opening, final boolean fits) {
    final double ceiling = Math.floor(ground) + opening;
    assertEquals(fits, FloatRoomRules.floatingBody(0.6, height, 0.5, ground, 0.5).maxY <= ceiling);
  }

  @ParameterizedTest(name = "node {0} of {1}: check to {2}")
  @CsvSource({"0, 10, 3", "4, 10, 7", "8, 10, 10", "10, 10, 10", "-1, 5, 3", "0, 0, 0"})
  @DisplayName("only the next few path nodes are checked for room")
  void lastNodeChecked(final int current, final int length, final int last) {
    assertEquals(last, FloatRoomRules.lastNodeChecked(current, length));
  }
}
