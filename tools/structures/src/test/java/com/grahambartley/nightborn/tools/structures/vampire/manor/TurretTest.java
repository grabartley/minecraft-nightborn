package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Facing;
import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class TurretTest {

  private static final Map<Boolean, Template> BUILT =
      Map.of(true, built(true), false, built(false));

  static Template built(final boolean round) {
    final Template t = new Template("vampire/test", Manor.SIZE_X, Manor.SIZE_Y, Manor.SIZE_Z);
    ManorShell.build(t);
    new Turret(round).build(t);
    return t;
  }

  @ParameterizedTest(name = "round {0}: {1},{2} ring {3}")
  @CsvSource({
    "true, 62, 42, true",
    "true, 61, 44, true",
    "true, 60, 44, false",
    "true, 54, 40, true",
    "true, 63, 42, false",
    "false, 62, 46, true",
    "false, 61, 45, false",
    "false, 54, 38, true"
  })
  void shapesItsRing(final boolean round, final int x, final int z, final boolean ring) {
    assertEquals(ring, new Turret(round).ring(x, z));
  }

  @ParameterizedTest(name = "round {0}: the tower opens into the corner rooms on both floors")
  @ValueSource(booleans = {true, false})
  void opensIntoTheHouse(final boolean round) {
    final Template t = BUILT.get(round);
    for (final int y : new int[] {Manor.F0 + 1, Manor.F1 + 1}) {
      assertEquals(BlockState.AIR, t.get(Manor.ROOM_E - 1, y, Manor.ROOM_S));
      assertEquals(BlockState.AIR, t.get(Manor.HOUSE_E, y, Turret.CZ - 1));
      assertEquals(BlockState.AIR, t.get(Turret.CX - 1, y, Manor.HOUSE_S));
    }
  }

  @ParameterizedTest(name = "round {0}: rises past the eaves to a solid capped roof")
  @ValueSource(booleans = {true, false})
  void risesToACappedRoof(final boolean round) {
    final Turret turret = new Turret(round);
    final Template t = BUILT.get(round);
    assertTrue(
        t.get(Turret.CX + Turret.RADIUS, Turret.WALL_TOP - 1, Turret.CZ).contains("gothic_stone"));
    assertEquals("minecraft:deepslate_tiles", t.get(Turret.CX, turret.roofTop(), Turret.CZ));
    assertTrue(t.get(Turret.CX, turret.roofTop() + 1, Turret.CZ).contains(ManorRoofs.FINIAL));
    assertTrue(turret.roofTop() > Manor.TOP + ManorRoofs.rise(ManorRoofs.RIDGE_Z));
  }

  @ParameterizedTest(name = "round {0}: every window has a curtain on the inside")
  @ValueSource(booleans = {true, false})
  void windowsAreCurtained(final boolean round) {
    final Turret turret = new Turret(round);
    final Template t = BUILT.get(round);
    for (final Window window : turret.windows()) {
      assertTrue(turret.ring(window.x(), window.z()), window.toString());
      final Pos in = window.reveal();
      assertTrue(turret.inside(in.x(), in.z(), Turret.RADIUS - 1), "reveal " + in);
      assertEquals(Facing.OPPOSITE.get(window.outward()), Turret.inward(window.x(), window.z()));
      assertTrue(t.get(in.x(), window.sill(), in.z()).contains("_velvet_drape"));
    }
  }

  @ParameterizedTest(name = "a roof stair at {0},{1} faces {2}")
  @CsvSource({"63, 42, west", "53, 42, east", "58, 47, north", "58, 37, south", "61, 43, west"})
  void roofStairsFaceTheTop(final int x, final int z, final String facing) {
    assertEquals(facing, Turret.inward(x, z));
  }

  @ParameterizedTest(name = "round {0}: step {1} of the stair to the study is at y {2}, z {3}")
  @CsvSource({
    "true, 0, 13, 45",
    "true, 3, 16, 42",
    "true, 5, 18, 40",
    "false, 0, 13, 45",
    "false, 5, 18, 40"
  })
  void aStairClimbsToTheStudy(final boolean round, final int step, final int y, final int z) {
    final Template t = BUILT.get(round);
    assertTrue(
        t.get(Turret.STAIR_X, y, z).startsWith("minecraft:dark_oak_stairs[facing=north"),
        t.get(Turret.STAIR_X, y, z));
    for (int up = 1; up <= 4; up++) {
      assertEquals(BlockState.AIR, t.get(Turret.STAIR_X, y + up, z));
    }
  }

  @ParameterizedTest(name = "round {0}: the study stairwell is railed at {1},{2}")
  @CsvSource({"true, 58, 42", "true, 60, 43", "false, 58, 44", "false, 60, 41"})
  void theStairwellIsRailed(final boolean round, final int x, final int z) {
    assertTrue(BUILT.get(round).get(x, Turret.STUDY + 1, z).contains("fence"));
  }
}
