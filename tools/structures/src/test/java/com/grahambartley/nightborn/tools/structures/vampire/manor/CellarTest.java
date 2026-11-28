package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class CellarTest {

  static Template t;

  @BeforeAll
  static void build() {
    t = new Template("vampire/test", Manor.SIZE_X, Manor.SIZE_Y, Manor.SIZE_Z);
    ManorShell.build(t);
    Cellar.build(t);
  }

  @ParameterizedTest(name = "the cellar is clear at y {0}")
  @ValueSource(ints = {1, 2, 3, 4})
  void theCellarIsFourHigh(final int y) {
    assertEquals(BlockState.AIR, t.get(54, y, 25));
  }

  @ParameterizedTest(name = "step {0} down from the kitchen is at {1},{2}")
  @CsvSource({"0, 5, 24", "1, 4, 23", "2, 3, 22", "3, 2, 21", "4, 1, 20"})
  void aStairLeadsDownFromTheKitchen(final int step, final int y, final int z) {
    for (int x = Cellar.STAIR_X; x <= Cellar.STAIR_X + 1; x++) {
      assertTrue(t.get(x, y, z).startsWith("minecraft:dark_oak_stairs[facing=south"));
      assertEquals(BlockState.AIR, t.get(x, y + 1, z));
    }
  }

  @ParameterizedTest(name = "the stairwell is railed at {0},{1}")
  @CsvSource({"46, 19", "47, 19", "48, 20", "48, 23"})
  void theStairwellIsRailed(final int x, final int z) {
    assertTrue(t.get(x, Manor.F0 + 1, z).contains("fence"));
  }

  @ParameterizedTest(name = "a barred vent breathes at {0},{1}")
  @CsvSource({"58, 25", "57, 28", "49, 16", "54, 17"})
  void ventsAreBarred(final int x, final int z) {
    assertEquals("minecraft:iron_bars", t.get(x, Cellar.STAND + 1, z));
  }

  @Test
  void theOldBloodFontStandsOnADaisWithOpenAirAbove() {
    assertEquals(
        "nightborn:old_blood_font[level=full]", t.get(Cellar.FONT_X, Cellar.STAND, Cellar.FONT_Z));
    assertEquals(
        "minecraft:polished_blackstone_bricks", t.get(Cellar.FONT_X, Cellar.FLOOR, Cellar.FONT_Z));
    assertEquals(BlockState.AIR, t.get(Cellar.FONT_X, Cellar.STAND + 1, Cellar.FONT_Z));
  }
}
