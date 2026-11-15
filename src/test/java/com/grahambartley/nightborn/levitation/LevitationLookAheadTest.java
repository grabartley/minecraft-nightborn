package com.grahambartley.nightborn.levitation;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.OptionalDouble;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class LevitationLookAheadTest {

  @ParameterizedTest(name = "strafe {0}, forward {1}, yaw {2}: ({3}, {4})")
  @CsvSource({
    "0, 1, 0, 0, 1",
    "0, 0.98, 90, -1, 0",
    "0, 1, 180, 0, -1",
    "1, 0, 0, 1, 0",
    "0, -1, 0, 0, -1",
    "0.98, 0.98, 0, 0.7071, 0.7071"
  })
  @DisplayName("it looks the way the vampire is being moved, as one block of unit length")
  void heading(
      final double strafe, final double forward, final float yaw, final double x, final double z) {
    final LevitationLookAhead.Heading heading =
        LevitationLookAhead.heading(strafe, forward, yaw).orElseThrow();
    assertEquals(x, heading.x(), 0.0001);
    assertEquals(z, heading.z(), 0.0001);
  }

  @ParameterizedTest(name = "strafe {0}, forward {1}")
  @CsvSource({"0, 0", "0.00001, 0", "0, -0.00001"})
  @DisplayName("with no movement input it looks only straight down")
  void stillHasNoHeading(final double strafe, final double forward) {
    assertTrue(LevitationLookAhead.heading(strafe, forward, 45.0F).isEmpty());
  }

  @ParameterizedTest(name = "speed {0}: {1}")
  @CsvSource({
    "0, 0.0875; 0.175; 0.2625; 0.35",
    "0.216, 0.6275; 1.255; 1.8825; 2.51",
    "0.28, 0.7875; 1.575; 2.3625; 3.15",
    "2, 0.875; 1.75; 2.625; 3.5"
  })
  @DisplayName(
      "it looks about half a second ahead, and still just past its body when pressed against a"
          + " step")
  void distances(final double speed, final String expected) {
    final double[] want =
        Stream.of(expected.split(";")).mapToDouble(v -> Double.parseDouble(v.trim())).toArray();
    assertArrayEquals(want, LevitationLookAhead.distances(speed), 0.0001);
  }

  static Stream<Arguments> pairs() {
    return Stream.of(
        Arguments.of(OptionalDouble.empty(), OptionalDouble.empty(), OptionalDouble.empty()),
        Arguments.of(OptionalDouble.of(64), OptionalDouble.empty(), OptionalDouble.of(64)),
        Arguments.of(OptionalDouble.empty(), OptionalDouble.of(65), OptionalDouble.of(65)),
        Arguments.of(OptionalDouble.of(64), OptionalDouble.of(65), OptionalDouble.of(65)),
        Arguments.of(OptionalDouble.of(65), OptionalDouble.of(64), OptionalDouble.of(65)));
  }

  @ParameterizedTest(name = "{0} and {1}: {2}")
  @MethodSource("pairs")
  @DisplayName("it follows the higher of the ground below and the ground ahead")
  void higher(
      final OptionalDouble first, final OptionalDouble second, final OptionalDouble higher) {
    assertEquals(higher, LevitationLookAhead.higher(first, second));
  }

  @ParameterizedTest(name = "ground {0}, feet {1}: looks from {2}")
  @CsvSource({"64, 65, 65.05", "64, 66.4, 65.05", ", 70, 70.05", "64.5, 65.5, 65.55"})
  @DisplayName(
      "it only climbs one block above the ground it follows, whatever its step height, so a"
          + " fence or a wall still stops it")
  void climbLimit(final Double ground, final double feet, final double limit) {
    assertEquals(
        limit,
        LevitationLookAhead.climbLimit(
            ground == null ? OptionalDouble.empty() : OptionalDouble.of(ground), feet),
        0.0001);
  }
}
