package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FamiliarRulesTest {

  @ParameterizedTest(name = "owner here {0}, {1} away -> starts {2}, keeps {3}")
  @CsvSource({
    "true, 5.0, true, true",
    "true, 3.0, false, true",
    "true, 1.5, false, false",
    "false, 10.0, false, false"
  })
  @DisplayName("a familiar drifts after its owner once they pull away and settles close by")
  void follows(
      final boolean ownerHere, final double distance, final boolean starts, final boolean keeps) {
    assertEquals(starts, FamiliarRules.follows(ownerHere, distance));
    assertEquals(keeps, FamiliarRules.keepsFollowing(ownerHere, distance));
  }

  @ParameterizedTest(name = "{0}, {1} away -> {2}")
  @CsvSource({"FIGHT, 25.0, true", "CARRY, 25.0, true", "SCOUT, 25.0, false", "FIGHT, 10.0, false"})
  @DisplayName("a familiar left far behind rejoins its owner, unless it is out scouting")
  void teleports(final FamiliarMode mode, final double distance, final boolean expected) {
    assertEquals(expected, FamiliarRules.teleports(mode, distance));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"FIGHT, true", "CARRY, false", "SCOUT, false"})
  @DisplayName("only a fighting familiar picks fights")
  void fights(final FamiliarMode mode, final boolean expected) {
    assertEquals(expected, FamiliarRules.fights(mode));
  }

  @ParameterizedTest(name = "look ({3},{4},{5}) range {6} -> ({7},{8},{9})")
  @CsvSource({
    "0, 64, 0, 0, 0, 1, 24, 0, 64, 24",
    "0, 64, 0, 0, 0, 2, 10, 0, 64, 10",
    "5, 64, 5, 1, 0, 0, 8, 13, 64, 5",
    "0, 64, 0, 0, 0, 0, 8, 0, 64, 8"
  })
  @DisplayName("a scout flies the full range the way its owner looks")
  void scoutTarget(
      final double x,
      final double y,
      final double z,
      final double lx,
      final double ly,
      final double lz,
      final double range,
      final double ex,
      final double ey,
      final double ez) {
    assertEquals(
        new Vec3d(ex, ey, ez),
        FamiliarRules.scoutTarget(new Vec3d(x, y, z), new Vec3d(lx, ly, lz), range));
  }

  @ParameterizedTest(name = "{0} of {1} ticks out, {2} from target -> {3}")
  @CsvSource({"10, 200, 12.0, false", "100, 200, 12.0, true", "10, 200, 0.5, true"})
  @DisplayName("a scout turns back halfway through its time or once it gets there")
  void scoutTurnsBack(
      final int ticks, final int limit, final double distance, final boolean expected) {
    assertEquals(expected, FamiliarRules.scoutTurnsBack(ticks, limit, distance));
  }

  @ParameterizedTest(name = "owner here {0}, {1} away, {2} of {3} ticks -> {4}")
  @CsvSource({
    "true, 10.0, 120, 200, false",
    "true, 2.0, 120, 200, true",
    "true, 10.0, 200, 200, true",
    "false, 10.0, 120, 200, true"
  })
  @DisplayName("a scout is home once beside its owner, out of time or with no owner to return to")
  void scoutHome(
      final boolean ownerHere,
      final double distance,
      final int ticks,
      final int limit,
      final boolean expected) {
    assertEquals(expected, FamiliarRules.scoutHome(ownerHere, distance, ticks, limit));
  }

  @ParameterizedTest(name = "{0} within {1} -> {2}")
  @CsvSource({"7.9, 8.0, true", "8.0, 8.0, true", "8.1, 8.0, false"})
  @DisplayName("a scout marks what it passes within its radius")
  void marks(final double distance, final double radius, final boolean expected) {
    assertEquals(expected, FamiliarRules.marks(distance, radius));
  }

  @ParameterizedTest(name = "{0} ticks left -> {1}")
  @CsvSource({"200, false", "21, false", "20, true", "0, true"})
  @DisplayName("a familiar fades over its last second")
  void fading(final int ticksLeft, final boolean expected) {
    assertEquals(expected, FamiliarRules.fading(ticksLeft));
  }

  @ParameterizedTest(name = "{0} owned, limit {1} -> dismiss {2}")
  @CsvSource({"0, 1, 0", "1, 1, 1", "1, 2, 0", "2, 2, 1", "3, 1, 3", "0, 0, 0", "1, 0, 1"})
  @DisplayName(
      "summoning past the limit sends the oldest familiars away, and one is always allowed")
  void toDismiss(final int owned, final int limit, final int expected) {
    assertEquals(expected, FamiliarRules.toDismiss(owned, limit));
  }

  @ParameterizedTest(name = "inside a wall {0}, owner here {1} -> {2}")
  @CsvSource({
    "false, true, SETTLE",
    "false, false, SETTLE",
    "true, true, JOIN_OWNER",
    "true, false, RISE"
  })
  @DisplayName(
      "a scout that ends inside a wall joins its owner, or rises out when no owner is near")
  void scoutEnd(
      final boolean insideWall, final boolean ownerHere, final FamiliarRules.ScoutEnd expected) {
    assertEquals(expected, FamiliarRules.scoutEnd(insideWall, ownerHere));
  }
}
