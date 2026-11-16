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

  @ParameterizedTest(name = "{0},{1} on wall {2}")
  @CsvSource({
    "12, 8, true",
    "12, 27, true",
    "22, 15, true",
    "40, 8, true",
    "30, 22, true",
    "17, 17, false",
    "27, 15, false",
    "11, 15, false"
  })
  void boxesKnowTheirWalls(final int x, final int z, final boolean wall) {
    assertEquals(wall, ManorShell.WING.onWall(x, z) || ManorShell.MAIN.onWall(x, z), x + "," + z);
  }

  @ParameterizedTest(name = "y {0} of the outer wall is {1}")
  @CsvSource({
    "0, gothic_stone_bricks",
    "3, gothic_stone_bricks",
    "6, minecraft:bricks",
    "9, gothic_stone_bricks",
    "12, minecraft:bricks",
    "14, gothic_stone_bricks"
  })
  void wallsCarryStoneBands(final int y, final String block) {
    assertTrue(shell.get(30, y, Manor.MAIN_N).contains(block), shell.get(30, y, Manor.MAIN_N));
  }

  @ParameterizedTest(name = "inside at y {0} is {1}")
  @CsvSource({
    "3, dark_oak_planks",
    "6, air",
    "9, dark_oak_planks",
    "12, air",
    "14, dark_oak_planks"
  })
  void roomsAreHollowBetweenFloors(final int y, final String block) {
    assertTrue(shell.get(36, y, 19).contains(block), shell.get(36, y, 19));
  }

  @ParameterizedTest(name = "a partition is {1} at {0} above its floor")
  @CsvSource({"1, dark_oak_planks", "2, dark_oak_planks", "3, stripped_dark_oak_wood"})
  void partitionsAreWainscoted(final int above, final String block) {
    assertEquals(block, ManorShell.panel(Manor.F0 + above));
    assertEquals(block, ManorShell.panel(Manor.F1 + above));
  }

  @ParameterizedTest(name = "a door stands at {0},{1},{2}")
  @CsvSource({
    "27, 4, 8",
    "22, 4, 20",
    "32, 4, 19",
    "17, 4, 17",
    "12, 4, 13",
    "22, 10, 19",
    "32, 10, 18",
    "36, 10, 15"
  })
  void doorsOpenEveryRoom(final int x, final int y, final int z) {
    assertTrue(shell.get(x, y, z).contains("dark_oak_door"), shell.get(x, y, z));
    assertTrue(shell.get(x, y + 1, z).contains("half=upper"));
  }

  @ParameterizedTest(name = "the window list holds {0},{1}")
  @CsvSource({"23, 22", "12, 19", "40, 9", "38, 8"})
  void windowsAreGlazed(final int x, final int z) {
    assertTrue(ManorShell.WINDOWS.stream().anyMatch(window -> window.x() == x && window.z() == z));
    assertTrue(shell.get(x, Window.GROUND_SILL, z).contains("glass"));
  }
}
