package com.grahambartley.nightborn.batform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BatFlightModelTest {

  private static final Vec3d SOUTH = new Vec3d(0.0, 0.0, 1.0);
  private static final double EPSILON = 1.0E-9;

  private static BatFlightModel.Controls controls(
      final Vec3d look, final double forward, final boolean sprinting, final boolean jumping) {
    return new BatFlightModel.Controls(look, 0.0F, forward, 0.0, sprinting, jumping);
  }

  private static Vec3d flyFor(final int ticks, final Vec3d from, final BatFlightModel.Controls c) {
    Vec3d velocity = from;
    for (int tick = 0; tick < ticks; tick++) {
      velocity = BatFlightModel.next(velocity, false, c);
    }
    return velocity;
  }

  @ParameterizedTest(name = "look ({0}, {1}, {2})")
  @CsvSource({"0, 0, 1", "1, 0, 0", "0, 1, 0", "0, -1, 0", "0.6, 0.8, 0", "-0.6, -0.8, 0"})
  @DisplayName("holding forward flies where the bat looks, up and down included")
  void fliesWhereItLooks(final double x, final double y, final double z) {
    final Vec3d look = new Vec3d(x, y, z);
    final Vec3d velocity = flyFor(100, Vec3d.ZERO, controls(look, 1.0, false, false));
    assertEquals(1.0, velocity.normalize().dotProduct(look), 1.0E-6);
  }

  @ParameterizedTest(name = "after {0} ticks")
  @CsvSource({"1, 0.03", "5, 0.15", "10, 0.3", "14, 0.4", "60, 0.4"})
  @DisplayName("speed eases in from a hover up to the cruising speed")
  void easesIn(final int ticks, final double speed) {
    assertEquals(
        speed, flyFor(ticks, Vec3d.ZERO, controls(SOUTH, 1.0, false, false)).length(), EPSILON);
  }

  @ParameterizedTest(name = "sprinting {0}, look y {1} -> {2}")
  @CsvSource({
    "false, 0.0, 0.4",
    "true, 0.0, 0.7",
    "false, 1.0, 0.28",
    "false, -1.0, 0.52",
    "true, -1.0, 0.91"
  })
  @DisplayName("sprint flies faster, a dive faster still and a climb slower")
  void targetSpeed(final boolean sprinting, final double lookY, final double expected) {
    assertEquals(expected, BatFlightModel.targetSpeed(sprinting, lookY), EPSILON);
  }

  @ParameterizedTest(name = "after {0} ticks of sprint -> {1}")
  @CsvSource({"1, 0.68", "5, 0.6", "10, 0.5", "15, 0.4", "40, 0.4"})
  @DisplayName("letting go of sprint eases the bat back down rather than snapping to cruise")
  void easesOut(final int ticks, final double speed) {
    final Vec3d sprinting = SOUTH.multiply(BatFlightModel.SPRINT_SPEED);
    assertEquals(
        speed, flyFor(ticks, sprinting, controls(SOUTH, 1.0, false, false)).length(), EPSILON);
  }

  @ParameterizedTest(name = "after {0} ticks")
  @CsvSource({"1", "5", "20"})
  @DisplayName("letting go of forward coasts, slower every tick but never stopping dead")
  void coasts(final int ticks) {
    final Vec3d cruising = SOUTH.multiply(BatFlightModel.CRUISE_SPEED);
    final Vec3d coasting = flyFor(ticks, cruising, controls(SOUTH, 0.0, false, false));
    assertEquals(
        BatFlightModel.CRUISE_SPEED * Math.pow(BatFlightModel.COAST_DRAG, ticks),
        coasting.length(),
        EPSILON);
    assertTrue(coasting.length() > 0.0);
  }

  @ParameterizedTest(name = "forward {0}")
  @CsvSource({"0.0", "-1.0"})
  @DisplayName("a coasting bat settles into a hover, sooner when it brakes")
  void settlesToAHover(final double forward) {
    final Vec3d cruising = SOUTH.multiply(BatFlightModel.CRUISE_SPEED);
    assertEquals(Vec3d.ZERO, flyFor(200, cruising, controls(SOUTH, forward, false, false)));
    assertTrue(
        flyFor(3, cruising, controls(SOUTH, -1.0, false, false)).length()
            < flyFor(3, cruising, controls(SOUTH, 0.0, false, false)).length());
  }

  @ParameterizedTest(name = "alignment {0} keeps {1}")
  @CsvSource({"1.0, 1.0", "0.0, 0.75", "-1.0, 0.5"})
  @DisplayName("the sharper the turn, the more speed it costs")
  void turnKeep(final double alignment, final double kept) {
    assertEquals(kept, BatFlightModel.turnKeep(alignment), EPSILON);
  }

  @ParameterizedTest(name = "turning to ({0}, 0, {1})")
  @CsvSource({"1, 0", "-0.7071, -0.7071", "-1, 0"})
  @DisplayName("turning at speed slows the bat and swings its heading round a little each tick")
  void turningSlows(final double x, final double z) {
    final Vec3d look = new Vec3d(x, 0.0, z).normalize();
    final Vec3d cruising = SOUTH.multiply(BatFlightModel.CRUISE_SPEED);
    final Vec3d turned = BatFlightModel.next(cruising, false, controls(look, 1.0, false, false));
    assertTrue(turned.length() < BatFlightModel.CRUISE_SPEED, "slowed: " + turned.length());
    assertTrue(turned.normalize().dotProduct(look) < 1.0 - 1.0E-6, "not there yet");
    assertTrue(
        turned.normalize().dotProduct(look) >= SOUTH.dotProduct(look) - EPSILON, "but closer");
  }

  @ParameterizedTest(name = "flying ({0}, {1}, {2})")
  @CsvSource({"0, 0, -0.4", "0, 0, -0.7", "0.3, 0, -0.3", "0, 0.4, 0", "0, -0.2, -0.2"})
  @DisplayName("a bat comes round to where it looks from any heading, a full about-face included")
  void comesRound(final double x, final double y, final double z) {
    final Vec3d velocity = flyFor(60, new Vec3d(x, y, z), controls(SOUTH, 1.0, false, false));
    assertEquals(1.0, velocity.normalize().dotProduct(SOUTH), 1.0E-6);
    assertEquals(BatFlightModel.CRUISE_SPEED, velocity.length(), 1.0E-6);
  }

  @ParameterizedTest(name = "strafe {0}, forward {1}, yaw {2} -> ({3}, {4})")
  @CsvSource({
    "0, 1, 0, 0.0, 0.04",
    "0, 1, 90, -0.04, 0.0",
    "0, -1, 0, 0.0, -0.04",
    "1, 0, 0, 0.04, 0.0",
    "0, 0, 0, 0.0, 0.0"
  })
  @DisplayName("a landed bat crawls slowly with the movement keys and keeps its grip on the ground")
  void crawls(
      final double strafe, final double forward, final float yaw, final double x, final double z) {
    final Vec3d velocity =
        BatFlightModel.next(
            new Vec3d(0.3, -0.2, 0.3),
            true,
            new BatFlightModel.Controls(SOUTH, yaw, forward, strafe, false, false));
    assertEquals(x, velocity.x, 1.0E-6);
    assertEquals(z, velocity.z, 1.0E-6);
    assertEquals(-BatFlightModel.GRIP, velocity.y, EPSILON);
  }

  @ParameterizedTest(name = "forward {0}")
  @CsvSource({"0.0", "1.0"})
  @DisplayName("jump takes a landed bat off the ground")
  void takesOff(final double forward) {
    final Vec3d velocity =
        BatFlightModel.next(Vec3d.ZERO, true, controls(SOUTH, forward, false, true));
    assertEquals(BatFlightModel.TAKE_OFF, velocity.y, EPSILON);
  }

  @ParameterizedTest(name = "jumping {0}")
  @CsvSource({"true", "false"})
  @DisplayName("jump is not a climb key in the air")
  void jumpDoesNothingAloft(final boolean jumping) {
    assertEquals(
        BatFlightModel.next(SOUTH.multiply(0.2), false, controls(SOUTH, 1.0, false, false)),
        BatFlightModel.next(SOUTH.multiply(0.2), false, controls(SOUTH, 1.0, false, jumping)));
  }
}
