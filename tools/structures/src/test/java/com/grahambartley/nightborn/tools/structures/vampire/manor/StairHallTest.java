package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class StairHallTest {

  static Template hall;

  @BeforeAll
  static void build() {
    hall = new Template("vampire/test", Manor.SIZE_X, Manor.SIZE_Y, Manor.SIZE_Z);
    ManorShell.build(hall);
    StairHall.build(hall);
  }

  @ParameterizedTest(name = "a step at {0},{1},{2} rises {3}")
  @CsvSource({
    "27, 4, 17, north",
    "27, 5, 16, north",
    "24, 7, 13, south",
    "24, 8, 14, south",
    "30, 7, 13, south",
    "30, 8, 14, south"
  })
  void flightsRiseOneBlockAStep(final int x, final int y, final int z, final String facing) {
    assertTrue(hall.get(x, y, z).contains("dark_oak_stairs"), hall.get(x, y, z));
    assertTrue(hall.get(x, y, z).contains("facing=" + facing));
  }

  @ParameterizedTest(name = "under the flights there is headroom at {0},{1}")
  @CsvSource({"24, 13", "24, 14", "27, 12", "27, 14"})
  void passagesRunUnderTheStairs(final int x, final int z) {
    assertEquals(BlockState.AIR, hall.get(x, Manor.F0 + 1, z));
    assertEquals(BlockState.AIR, hall.get(x, Manor.F0 + 2, z));
  }

  @ParameterizedTest(name = "the gallery edge is railed at z {0}")
  @ValueSource(ints = {15, 17, 19})
  void galleriesAreRailed(final int z) {
    assertTrue(hall.get(Manor.MID_X - 2, Manor.F1 + 1, z).startsWith("minecraft:dark_oak_fence"));
    assertTrue(hall.get(Manor.MID_X + 2, Manor.F1 + 1, z).startsWith("minecraft:dark_oak_fence"));
  }

  @ParameterizedTest(name = "the hall is open to the ceiling at {0},{1}")
  @CsvSource({"27, 17", "27, 10", "24, 10"})
  void theHallRisesTwoStoreys(final int x, final int z) {
    assertEquals(BlockState.AIR, hall.get(x, Manor.F1, z));
  }

  @ParameterizedTest(name = "a gallery floor at {0},{1}")
  @CsvSource({"23, 15", "31, 21", "27, 21"})
  void galleriesCarryTheFloor(final int x, final int z) {
    assertEquals("minecraft:" + Manor.PLANKS, hall.get(x, Manor.F1, z));
  }

  @ParameterizedTest(name = "a fence joined {0} {1}")
  @CsvSource({"true, false", "false, true"})
  void fencesJoinAlongTheirRun(final boolean northSouth, final boolean eastWest) {
    final String fence = StairHall.fence(northSouth, eastWest);
    assertTrue(fence.contains("north=" + northSouth) && fence.contains("east=" + eastWest));
  }
}
