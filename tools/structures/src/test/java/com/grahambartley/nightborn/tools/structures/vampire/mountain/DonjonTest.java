package com.grahambartley.nightborn.tools.structures.vampire.mountain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.RouteRoom;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DonjonTest {

  private static Template box() {
    return new Template(
        "vampire/test", MountainCastle.SIZE, MountainCastle.HEIGHT, MountainCastle.SIZE);
  }

  private static Template built() {
    final Template t = box();
    Crag.build(t);
    Donjon.build(t);
    return t;
  }

  @ParameterizedTest(name = "storey {0} has its floor at {1} and its stair on x {2}")
  @CsvSource({
    "0, 32, 24",
    "1, 38, 38",
    "2, 44, 24",
    "3, 50, 38",
    "4, 56, 24",
    "5, 62, 38",
    "6, 68, 24"
  })
  void sevenStoreysStackSixApartWithStairsOnAlternateWalls(
      final int storey, final int floor, final int x) {
    assertEquals(floor, Donjon.floor(storey));
    assertEquals(x, Donjon.flightX(storey));
    assertEquals(floor, Donjon.floors().get(storey));
    assertEquals(7, Donjon.floors().size());
    assertEquals(68, Donjon.EYRIE);
    assertEquals(74, Donjon.TERRACE);
  }

  @ParameterizedTest(name = "the flight from storey {0}")
  @CsvSource({"0", "1", "2", "3", "4", "5"})
  void eachFlightClimbsToTheNextFloorWithHeadroom(final int storey) {
    final Template t = built();
    for (int lane = 0; lane < RouteRoom.LANES; lane++) {
      final int x = Donjon.flightX(storey) + lane * Donjon.inward(storey);
      for (int i = 0; i < Donjon.STOREY; i++) {
        final int y = Donjon.floor(storey) + 1 + i;
        final int z = Donjon.FLIGHT_FOOT - i;
        assertTrue(t.get(x, y, z).contains("stone_brick_stairs[facing=north"), t.get(x, y, z));
        for (int above = y + 1; above <= y + 1 + RouteRoom.HOUSEHOLD; above++) {
          assertEquals(BlockState.AIR, t.get(x, above, z));
        }
      }
      assertEquals(
          "minecraft:dark_oak_planks", t.get(x, Donjon.floor(storey + 1), Donjon.FLIGHT_FOOT));
      assertTrue(
          t.get(x, Donjon.floor(storey + 1) + 1, Donjon.FLIGHT_FOOT).contains("dark_oak_fence"),
          "a rail closes the open end of the stairwell");
    }
  }

  @ParameterizedTest(name = "{0},{1},{2} is {3}")
  @CsvSource({
    "23, 40, 31, stone_bricks",
    "31, 38, 31, dark_oak_planks",
    "31, 74, 31, stone_bricks",
    "31, 35, 39, air",
    "30, 33, 39, air",
    "27, 34, 23, gothic_stained_glass_pane",
    "23, 70, 35, gothic_stained_glass_pane",
    "35, 34, 39, stone_bricks",
    "35, 40, 39, gothic_stained_glass_pane"
  })
  void aStoneShellWithOneDoorAndLancetsOnEveryFloor(
      final int x, final int y, final int z, final String block) {
    assertTrue(built().get(x, y, z).contains(block), built().get(x, y, z));
  }

  @ParameterizedTest(name = "{0},{1} on the corbel ring")
  @CsvSource({"22, 31, east", "40, 31, west", "31, 22, south", "31, 40, north"})
  void theBattlementsHangOutOnCorbels(final int x, final int z, final String facing) {
    final Template t = built();
    assertTrue(t.get(x, Donjon.TERRACE - 1, z).contains("facing=" + facing + ",half=top"));
    assertTrue(t.get(x, Donjon.TERRACE + 1, z).contains("stone_bricks"));
    assertTrue(t.get(22, Donjon.TERRACE - 1, 22).contains("stone_bricks"));
  }

  @ParameterizedTest(name = "{0},{1},{2} under the roof is {3}")
  @CsvSource({
    "31, 75, 31, chiseled_stone_bricks",
    "31, 77, 31, chiseled_stone_bricks",
    "30, 75, 31, air",
    "23, 76, 23, dark_oak_log",
    "31, 78, 31, dark_oak_planks",
    "31, 80, 31, dark_oak_planks",
    "40, 78, 31, clay_roof_tile",
    "24, 69, 36, stairs[facing=north",
    "24, 74, 31, stairs[facing=north",
    "24, 75, 32, air"
  })
  void aSealedRoofOnPostsCoversTheWalkAndAFlightReachesIt(
      final int x, final int y, final int z, final String block) {
    assertTrue(built().get(x, y, z).contains(block), built().get(x, y, z));
  }
}
