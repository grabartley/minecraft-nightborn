package com.grahambartley.nightborn.tools.structures.vampire.mountain;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ChapelTest {

  private static Template box() {
    return new Template(
        "vampire/test", MountainCastle.SIZE, MountainCastle.HEIGHT, MountainCastle.SIZE);
  }

  private static Template built() {
    final Template t = box();
    Crag.build(t);
    Chapel.build(t);
    return t;
  }

  @ParameterizedTest(name = "{0},{1},{2} is {3}")
  @CsvSource({
    "43, 36, 19, limewashed_bricks",
    "53, 41, 30, limewashed_bricks",
    "48, 36, 27, air",
    "43, 33, 27, air",
    "43, 36, 26, half=top",
    "43, 37, 27, limewashed_bricks",
    "43, 36, 21, gothic_stained_glass_pane",
    "43, 39, 33, gothic_stained_glass_pane",
    "48, 41, 18, gothic_stained_glass_pane",
    "47, 39, 18, gothic_stained_glass_pane",
    "42, 42, 27, clay_roof_tile_stairs",
    "43, 43, 27, clay_roof_tiles"
  })
  void aTallNaveWithStainedLancetsUnderATiledRoof(
      final int x, final int y, final int z, final String block) {
    assertTrue(built().get(x, y, z).contains(block), built().get(x, y, z));
  }

  @ParameterizedTest(name = "pews at z {0}")
  @CsvSource({"25", "27", "29", "31", "33"})
  void pewsFaceTheAltarEitherSideOfTheAisle(final int z) {
    final Template t = built();
    for (final int x : new int[] {44, 45, 46, 50, 51, 52}) {
      assertTrue(t.get(x, 33, z).contains("ornate_chair[facing=north"), t.get(x, 33, z));
    }
    assertTrue(t.get(Chapel.MID_X, 33, z).contains("red_carpet"));
  }

  @ParameterizedTest(name = "{0},{1},{2} is {3}")
  @CsvSource({
    "48, 33, 20, carved_gothic_stone",
    "48, 34, 20, chalice",
    "46, 33, 20, candelabra",
    "50, 33, 20, candelabra",
    "44, 38, 19, burgundy_velvet_drape",
    "52, 33, 19, chest",
    "48, 33, 34, blood_pool"
  })
  void anAltarStandsAtTheNorthEnd(final int x, final int y, final int z, final String block) {
    assertTrue(built().get(x, y, z).contains(block), built().get(x, y, z));
  }
}
