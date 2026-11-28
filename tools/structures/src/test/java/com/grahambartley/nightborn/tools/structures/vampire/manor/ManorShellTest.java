package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ManorShellTest {

  static Template shell;

  @BeforeAll
  static void build() {
    shell = new Template("vampire/test", Manor.SIZE_X, Manor.SIZE_Y, Manor.SIZE_Z);
    ManorShell.build(shell);
  }

  @ParameterizedTest(name = "{0},{1}: outer {2}, lining {3}")
  @CsvSource({
    "18, 16, true, false",
    "58, 30, true, false",
    "19, 30, false, true",
    "40, 41, false, true",
    "20, 20, false, false",
    "38, 29, false, false"
  })
  void wallsAreTwoThick(final int x, final int z, final boolean outer, final boolean lining) {
    assertEquals(outer, ManorShell.HOUSE.onWall(x, z));
    assertEquals(lining, ManorShell.LINING.onWall(x, z));
  }

  @ParameterizedTest(name = "y {0} of the outer wall is {1}")
  @CsvSource({
    "0, gothic_stone_bricks",
    "5, carved_gothic_stone",
    "8, gothic_stone_bricks",
    "12, carved_gothic_stone",
    "15, gothic_stone_bricks",
    "18, carved_gothic_stone"
  })
  void outerWallsAreGothicStone(final int y, final String block) {
    assertTrue(shell.get(30, y, Manor.HOUSE_N).contains(block), shell.get(30, y, Manor.HOUSE_N));
  }

  @ParameterizedTest(name = "{0} above a floor the lining is {1}")
  @CsvSource({"1, ebony_planks", "2, ebony_planks", "3, stripped_dark_oak_log"})
  void liningIsWainscoted(final int above, final String block) {
    assertTrue(ManorShell.finish(30, Manor.F0 + above, 17).contains(block));
    assertTrue(ManorShell.finish(30, Manor.F1 + above, 17).contains(block));
  }

  @ParameterizedTest(name = "above the dado at y {0} the walls are plaster or limewash")
  @CsvSource({"9", "10", "11", "16", "17"})
  void upperWallsAreLimewashed(final int y) {
    final String finish = ManorShell.finish(25, y, 17);
    assertTrue(finish.contains("plaster") || finish.contains("limewashed"), finish);
  }

  @ParameterizedTest(name = "inside at y {0} is {1}")
  @CsvSource({
    "3, cobbled_deepslate",
    "5, ebony_planks",
    "8, air",
    "12, ebony_planks",
    "15, air",
    "18, ebony_planks"
  })
  void roomsAreHollowBetweenFloors(final int y, final String block) {
    assertTrue(shell.get(25, y, 23).contains(block), shell.get(25, y, 23));
  }

  @ParameterizedTest(name = "a beam runs across {0} at z {1}")
  @CsvSource({"library, 21", "library, 25", "drawing room, 33", "kitchen, 21", "dining room, 37"})
  void groundRoomsHaveCeilingBeams(final String room, final int z) {
    final ManorShell.Box box =
        switch (room) {
          case "library" -> ManorShell.LIBRARY;
          case "drawing room" -> ManorShell.DRAWING_ROOM;
          case "kitchen" -> ManorShell.KITCHEN;
          default -> ManorShell.DINING_ROOM;
        };
    assertTrue(ManorShell.beamLines(box).contains(z));
    assertTrue(shell.get(box.x0() + 2, Manor.F1 - 1, z).contains("axis=x"));
  }

  @ParameterizedTest(name = "a doorway opens at {0},{1},{2}")
  @CsvSource({
    "38, 6, 42",
    "31, 6, 25",
    "31, 6, 34",
    "45, 6, 34",
    "45, 6, 26",
    "24, 6, 29",
    "50, 6, 16",
    "18, 6, 22",
    "31, 13, 24",
    "45, 13, 35",
    "50, 13, 29"
  })
  void doorwaysOpenEveryRoom(final int x, final int y, final int z) {
    final String state = shell.get(x, y, z);
    assertTrue(state.contains("air") || state.contains("dark_oak_door"), state);
    assertTrue(shell.get(x, y + 1, z).contains("air") || shell.get(x, y + 1, z).contains("upper"));
  }
}
