package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class TurretTest {

  static Template built(final boolean round) {
    final Template t = new Template("vampire/test", Manor.SIZE_X, Manor.SIZE_Y, Manor.SIZE_Z);
    ManorShell.build(t);
    new Turret(round).build(t);
    return t;
  }

  @ParameterizedTest(name = "round {0}: {1},{2} ring {3}")
  @CsvSource({
    "true, 43, 22, true",
    "true, 42, 24, true",
    "true, 43, 25, false",
    "true, 41, 23, false",
    "false, 43, 25, true",
    "false, 42, 24, false"
  })
  void shapesItsRing(final boolean round, final int x, final int z, final boolean ring) {
    assertEquals(ring, new Turret(round).ring(x, z));
  }

  @ParameterizedTest(name = "round {0}: the house wall opens into the turret")
  @ValueSource(booleans = {true, false})
  void opensIntoTheHouse(final boolean round) {
    final Template t = built(round);
    for (final int y : new int[] {Manor.F0 + 1, Manor.F1 + 1}) {
      assertEquals(BlockState.AIR, t.get(Manor.MAIN_E, y, Turret.CZ - 1));
      assertEquals(BlockState.AIR, t.get(Turret.CX - 1, y, Manor.MAIN_S));
    }
  }

  @ParameterizedTest(name = "round {0}: rises past the eaves to a solid capped cone")
  @ValueSource(booleans = {true, false})
  void risesToACone(final boolean round) {
    final Turret turret = new Turret(round);
    final Template t = built(round);
    assertTrue(t.get(Turret.CX + Turret.RADIUS, Turret.WALL_TOP - 1, Turret.CZ).contains("bricks"));
    assertEquals("minecraft:deepslate_tiles", t.get(Turret.CX, turret.roofTop(), Turret.CZ));
    assertTrue(t.get(Turret.CX, turret.roofTop() + 1, Turret.CZ).contains(ManorRoofs.FINIAL));
    assertEquals("minecraft:deepslate_tiles", t.get(Turret.CX, turret.roofTop() - 1, Turret.CZ));
  }

  @ParameterizedTest(name = "round {0}: the wall inside the house at {1},{2} is {3} at y {4}")
  @CsvSource({
    "true, 38, 20, air, 4",
    "true, 38, 20, air, 6",
    "true, 38, 20, air, 12",
    "true, 38, 20, bricks, 7",
    "true, 38, 20, stone_bricks, 9",
    "true, 38, 20, air, 11",
    "true, 38, 20, bricks, 16",
    "true, 37, 21, bricks, 4",
    "true, 39, 19, bricks, 10",
    "false, 37, 20, bricks, 5",
    "false, 38, 19, air, 4",
    "false, 38, 19, air, 10",
    "false, 37, 19, bricks, 4",
    "false, 39, 19, bricks, 4",
    "false, 37, 20, bricks, 12"
  })
  void theWallInsideTheHouseRisesFromTheFoundationWithArches(
      final boolean round, final int x, final int z, final String block, final int y) {
    assertTrue(built(round).get(x, y, z).contains(block), built(round).get(x, y, z));
  }

  @ParameterizedTest(name = "a cone stair at {0},{1} faces {2}")
  @CsvSource({"44, 22, west", "36, 22, east", "40, 26, north", "40, 18, south", "43, 24, west"})
  void coneStairsFaceTheTop(final int x, final int z, final String facing) {
    assertEquals(facing, Turret.inward(x, z));
  }

  @ParameterizedTest(name = "round {0}: step {1} of the stair to the study is at y {2}")
  @CsvSource({
    "true, 0, 10, 24",
    "true, 2, 12, 22",
    "true, 4, 14, 20",
    "false, 0, 10, 24",
    "false, 4, 14, 20"
  })
  void aStairClimbsToTheStudy(final boolean round, final int step, final int y, final int z) {
    final Template t = built(round);
    assertTrue(
        t.get(Turret.STAIR_X, y, z).startsWith("minecraft:dark_oak_stairs[facing=north"),
        t.get(Turret.STAIR_X, y, z));
    assertEquals(BlockState.AIR, t.get(Turret.STAIR_X, y + 1, z));
    assertEquals(BlockState.AIR, t.get(Turret.STAIR_X, y + 2, z));
  }
}
