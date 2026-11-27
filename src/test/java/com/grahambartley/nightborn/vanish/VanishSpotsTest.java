package com.grahambartley.nightborn.vanish;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.skill.PowerHeading;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class VanishSpotsTest {

  private static final double CLOSE = 1.0E-9;
  private static final Vec3d EYE = new Vec3d(10.0, 65.6, 20.0);
  private static final Vec3d SOUTH = new Vec3d(0.0, 0.0, 1.0);

  private static void assertClose(final Vec3d expected, final Vec3d actual) {
    assertEquals(expected.x, actual.x, CLOSE);
    assertEquals(expected.y, actual.y, CLOSE);
    assertEquals(expected.z, actual.z, CLOSE);
  }

  static Stream<Arguments> headings() {
    return Stream.of(
        Arguments.of(false, PowerHeading.NONE, true, List.of(PowerHeading.BACK)),
        Arguments.of(false, PowerHeading.NONE, false, List.of(PowerHeading.BACK)),
        Arguments.of(false, PowerHeading.FORWARD, true, List.of(PowerHeading.FORWARD)),
        Arguments.of(false, PowerHeading.BACK, true, List.of(PowerHeading.BACK)),
        Arguments.of(false, PowerHeading.LEFT, false, List.of(PowerHeading.LEFT)),
        Arguments.of(false, PowerHeading.RIGHT, true, List.of(PowerHeading.RIGHT)),
        Arguments.of(
            true,
            PowerHeading.FORWARD,
            true,
            List.of(PowerHeading.LEFT, PowerHeading.RIGHT, PowerHeading.BACK)),
        Arguments.of(
            true,
            PowerHeading.NONE,
            false,
            List.of(PowerHeading.RIGHT, PowerHeading.LEFT, PowerHeading.BACK)));
  }

  @ParameterizedTest(name = "dodging {0}, held {1}, left first {2}: {3}")
  @MethodSource("headings")
  @DisplayName(
      "a player goes the way they hold or behind, a dodging vampire tries a side then away")
  void headings(
      final boolean dodging,
      final PowerHeading held,
      final boolean leftFirst,
      final List<PowerHeading> expected) {
    assertEquals(expected, VanishSpots.headings(dodging, held, leftFirst));
  }

  @ParameterizedTest(name = "yaw {0}: {1}, {2}")
  @CsvSource({"0, 0, 1", "90, -1, 0", "180, 0, -1", "-90, 1, 0", "270, 1, 0"})
  @DisplayName("facing follows the yaw on the flat")
  void facing(final float yaw, final double x, final double z) {
    assertClose(new Vec3d(x, 0.0, z), VanishSpots.facing(yaw));
  }

  @ParameterizedTest(name = "to {0}, {1}, {2}: {3}, {4}")
  @CsvSource({"10, 64, 25, 0, 1", "7, 90, 20, -1, 0", "13, 64, 24, 0.6, 0.8", "10, 70, 20, 1, 0"})
  @DisplayName("facing a foe ignores height and falls back when the foe is straight above")
  void toward(
      final double x, final double y, final double z, final double wantX, final double wantZ) {
    assertClose(
        new Vec3d(wantX, 0.0, wantZ),
        VanishSpots.toward(new Vec3d(10, 64, 20), new Vec3d(x, y, z), new Vec3d(1, 0, 0)));
  }

  @ParameterizedTest(name = "{0} by {1}: {2}, {3}")
  @CsvSource({
    "FORWARD, 4, 10, 24",
    "BACK, 4, 10, 16",
    "NONE, 4, 10, 16",
    "LEFT, 4, 14, 20",
    "RIGHT, 4, 6, 20",
    "LEFT, 2.5, 12.5, 20"
  })
  @DisplayName(
      "the wanted spot is the set distance away at eye height, left being the east of south")
  void wanted(final PowerHeading heading, final double distance, final double x, final double z) {
    assertClose(new Vec3d(x, EYE.y, z), VanishSpots.wanted(EYE, SOUTH, heading, distance));
  }

  @ParameterizedTest(name = "spot {0}, foe {1}: faces {2},{3}")
  @CsvSource({"true, true, 0, 1", "true, false, 0, 1", "false, true, 1, 0", "false, false, -1, 0"})
  @DisplayName("a vanish faces the spot it aims at, else its foe, else where it looks")
  void facesTheAim(final boolean spot, final boolean foe, final double x, final double z) {
    final Vec3d facing =
        VanishSpots.facing(
            Vec3d.ZERO,
            spot ? new Vec3d(0, 0, 6) : null,
            foe ? new Vec3d(4, 0, 0) : null,
            new Vec3d(-1, 0, 0));
    assertEquals(x, facing.x, 1.0E-9);
    assertEquals(z, facing.z, 1.0E-9);
  }

  @ParameterizedTest(name = "spot {0}, held {1}: {2}")
  @CsvSource({
    "true, BACK, FORWARD",
    "true, NONE, FORWARD",
    "false, LEFT, LEFT",
    "false, NONE, NONE"
  })
  @DisplayName("a vanish aimed at a spot goes forward toward it, and otherwise the held way")
  void heldTowardASpot(final boolean spot, final PowerHeading held, final PowerHeading heading) {
    assertEquals(heading, VanishSpots.held(spot ? Vec3d.ZERO : null, held));
  }
}
