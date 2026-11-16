package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ManorRoofsTest {

  static Template roofed;

  @BeforeAll
  static void build() {
    roofed = new Template("vampire/test", Manor.SIZE_X, Manor.SIZE_Y, Manor.SIZE_Z);
    ManorShell.build(roofed);
    ManorFront.build(roofed);
    ManorRoofs.build(roofed);
  }

  @ParameterizedTest(name = "a finial tops the ridge at {0},{1},{2}")
  @CsvSource({"17, 21, 27", "17, 21, 8", "27, 19, 22", "40, 23, 15"})
  void finialsTopEachGable(final int x, final int y, final int z) {
    assertTrue(
        roofed.get(x, y, z).startsWith("minecraft:" + ManorRoofs.FINIAL), roofed.get(x, y, z));
  }

  @ParameterizedTest(name = "the hall ceiling stays plank at z {0}")
  @ValueSource(ints = {9, 15, 21})
  void theWingEaveStaysOutOfTheHall(final int z) {
    assertEquals("minecraft:" + Manor.PLANKS, roofed.get(Manor.WING_E + 1, Manor.TOP, z));
  }

  @ParameterizedTest(name = "the main eave breaks for the dormer at x {0}")
  @ValueSource(ints = {24, 27, 30})
  void theDormerBreaksTheEave(final int x) {
    assertEquals(BlockState.AIR, roofed.get(x, Manor.TOP, Manor.MAIN_S + 1));
  }

  @ParameterizedTest(name = "the eave runs on at x {0}")
  @ValueSource(ints = {33, 38})
  void theMainEaveOverhangs(final int x) {
    assertTrue(roofed.get(x, Manor.TOP, Manor.MAIN_S + 1).contains("deepslate_tile_stairs"));
    assertTrue(roofed.get(x, Manor.TOP, Manor.MAIN_N - 1).contains("deepslate_tile_stairs"));
  }

  @ParameterizedTest(name = "attic light at {0},{1}")
  @CsvSource({"17, 8", "17, 27"})
  void gablesLightTheAttic(final int x, final int z) {
    assertEquals("minecraft:" + Manor.PANE, roofed.get(x, Manor.TOP + 2, z));
  }
}
