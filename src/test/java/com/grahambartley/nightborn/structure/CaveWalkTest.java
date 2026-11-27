package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class CaveWalkTest {

  private static final BlockPos DOOR = new BlockPos(0, -30, 1);
  private static final BlockBox FAR_AWAY = new BlockBox(100, -70, 100, 120, -60, 120);
  private static final FakeRock HALL = new FakeRock(CaveCell.SOLID).cave(-4, -30, 1, 4, 12);

  @ParameterizedTest(name = "from floor y {0} to floor y {1} under open air up to y {2}: walks {3}")
  @CsvSource({
    "-30, -30, -28, true",
    "-30, -29, -27, true",
    "-30, -29, -28, false",
    "-30, -28, -25, false",
    "-27, -30, -25, true",
    "-26, -30, -24, false",
    "-27, -30, -26, false"
  })
  void walksStepsUpOneAndDropsThreeWithRoomOverhead(
      final int fromY, final int toY, final int openTo, final boolean walks) {
    final FakeRock ground =
        new FakeRock(CaveCell.SOLID)
            .fill(new BlockBox(0, Math.min(fromY, toY) + 1, 0, 1, openTo, 0), CaveCell.OPEN)
            .fill(new BlockBox(0, fromY, 0, 0, fromY, 0), CaveCell.SOLID)
            .fill(new BlockBox(1, toY, 0, 1, toY, 0), CaveCell.SOLID);
    assertEquals(walks, CaveWalk.walks(new BlockPos(0, fromY, 0), new BlockPos(1, toY, 0), ground));
  }

  @ParameterizedTest(name = "a floor under {0} and {1} stands: {2}")
  @CsvSource({"OPEN, OPEN, true", "OPEN, SOLID, false", "SOLID, OPEN, false", "FLUID, OPEN, false"})
  void standsOnRockWithTwoOpenBlocksAbove(
      final CaveCell feet, final CaveCell head, final boolean stands) {
    final FakeRock ground =
        new FakeRock(CaveCell.SOLID)
            .fill(new BlockBox(0, 1, 0, 0, 1, 0), feet)
            .fill(new BlockBox(0, 2, 0, 0, 2, 0), head);
    assertEquals(stands, CaveWalk.stands(BlockPos.ORIGIN, ground));
  }

  static Stream<Arguments> approaches() {
    return Stream.of(
        Arguments.of("a wide hall", HALL, 64, true),
        Arguments.of("a wide hall asked for more floors than it has", HALL, 200, false),
        Arguments.of(
            "a narrow pocket", new FakeRock(CaveCell.SOLID).cave(-1, -30, 1, 1, 4), 64, false),
        Arguments.of(
            "a hall reached only by climbing three blocks",
            new FakeRock(CaveCell.SOLID)
                .cave(-4, -30, 1, 4, 3)
                .fill(new BlockBox(-4, -33, 4, 4, -30, 14), CaveCell.OPEN),
            30,
            false),
        Arguments.of(
            "a hall two blocks above the door drops down to it",
            new FakeRock(CaveCell.SOLID)
                .fill(new BlockBox(-4, -29, 1, 4, -24, 3), CaveCell.OPEN)
                .fill(new BlockBox(-4, -27, 4, 4, -24, 14), CaveCell.OPEN),
            30,
            true));
  }

  @ParameterizedTest(name = "{0}: {2} floors walk to the door: {3}")
  @MethodSource("approaches")
  void countsTheCaveFloorsThatWalkToTheDoor(
      final String name, final CaveMouth.Ground ground, final int floors, final boolean enough) {
    assertEquals(enough, CaveWalk.approached(DOOR, FAR_AWAY, ground, floors));
  }

  @ParameterizedTest(name = "a route of {0} steps runs from the door over cave floor")
  @CsvSource({"1", "4", "8"})
  void routesStepByStepFromTheDoor(final int steps) {
    final List<BlockPos> route = CaveWalk.route(DOOR, FAR_AWAY, HALL, steps).orElseThrow();
    assertEquals(steps + 1, route.size());
    assertEquals(DOOR, route.getFirst());
    for (int i = 1; i < route.size(); i++) {
      assertEquals(1, route.get(i - 1).getManhattanDistance(route.get(i)));
      assertTrue(CaveWalk.stands(route.get(i), HALL));
    }
  }

  @Test
  void findsNoRouteLongerThanTheCave() {
    final FakeRock pocket = new FakeRock(CaveCell.SOLID).cave(0, -30, 1, 0, 3);
    assertEquals(Optional.empty(), CaveWalk.route(DOOR, FAR_AWAY, pocket, 5));
  }

  @Test
  void neverRoutesThroughTheLairItself() {
    final FakeRock corridor = new FakeRock(CaveCell.SOLID).cave(-6, -30, 1, 6, 1);
    final BlockBox lair = new BlockBox(-6, -30, 1, -1, -20, 3);
    final List<BlockPos> route = CaveWalk.route(DOOR, lair, corridor, 3).orElseThrow();
    assertTrue(route.stream().noneMatch(lair::contains), route.toString());
  }

  @ParameterizedTest(name = "a floor at {0},{1},{2} is within reach of the door: {3}")
  @CsvSource({"0, -30, 13, true", "12, -18, 1, true", "13, -30, 1, false", "0, -43, 1, false"})
  void keepsTheWalkNearTheDoor(final int x, final int y, final int z, final boolean near) {
    assertEquals(near, CaveWalk.near(new BlockPos(x, y, z), DOOR));
  }
}
