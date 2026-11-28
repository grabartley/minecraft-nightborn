package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ManorFrontTest {

  static Template t;

  @BeforeAll
  static void build() {
    t = new Template("vampire/test", Manor.SIZE_X, Manor.SIZE_Y, Manor.SIZE_Z);
    ManorShell.build(t);
    ManorFront.build(t);
  }

  static boolean open(final String state) {
    return state == null || BlockState.AIR.equals(state);
  }

  @ParameterizedTest(name = "x {0} on the steps {1}")
  @CsvSource({"34, false", "35, true", "38, true", "41, true", "42, false"})
  void theStepsAreSevenWide(final int x, final boolean on) {
    assertEquals(on, ManorFront.onSteps(x));
  }

  @ParameterizedTest(name = "step at z {0} is at y {1}")
  @CsvSource({"47, 5", "48, 4", "49, 3", "50, 2", "51, 1"})
  void theStepsFallToTheGround(final int z, final int y) {
    for (int x = 35; x <= 41; x++) {
      assertTrue(t.get(x, y, z).contains("gothic_stone_brick_stairs[facing=north"), x + "," + z);
      assertTrue(open(t.get(x, y + 1, z)));
    }
  }

  @ParameterizedTest(name = "cheek wall at {0},{1} tops out at y {2}")
  @CsvSource({"34, 47, 6", "42, 49, 4", "34, 51, 2"})
  void cheekWallsFollowTheFlight(final int x, final int z, final int slab) {
    assertTrue(t.get(x, slab, z).contains("slab"), t.get(x, slab, z));
    assertTrue(open(t.get(x, slab + 1, z)));
  }

  @ParameterizedTest(name = "the porch at {0},{1} has {2} at y {3}")
  @CsvSource({
    "33, 46, gothic_stone, 11",
    "38, 45, deepslate_tiles, 11",
    "38, 46, half=top, 10",
    "38, 44, lantern, 10",
    "34, 46, gothic_stone_brick_wall, 6",
    "38, 46, air, 6"
  })
  void aPorchShelterTheDoor(final int x, final int z, final String block, final int y) {
    assertTrue(t.get(x, y, z).contains(block), t.get(x, y, z));
  }
}
