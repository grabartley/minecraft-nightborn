package com.grahambartley.nightborn.tools.structures.vampire.mountain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WardsTest {

  private static Template box() {
    return new Template(
        "vampire/test", MountainCastle.SIZE, MountainCastle.HEIGHT, MountainCastle.SIZE);
  }

  private static Template built(final MountainCastle.Layout layout) {
    final Template t = box();
    Crag.build(t);
    Wards.build(t, layout);
    return t;
  }

  @ParameterizedTest(name = "a lamp stands at {0},{1},{2}")
  @CsvSource({"25, 33, 12", "37, 33, 12", "27, 33, 41", "35, 33, 41", "15, 27, 48", "36, 27, 52"})
  void lampsLightBothWards(final int x, final int y, final int z) {
    final Template t = built(MountainCastle.EYRIE);
    assertTrue(t.get(x, y, z).contains("stone_brick_wall"));
    assertTrue(t.get(x, y + 1, z).contains("dark_oak_fence"));
    assertTrue(t.get(x, y + 2, z).contains("lantern[hanging=false"));
  }

  @ParameterizedTest(name = "the path is paved at {0},{1},{2}")
  @CsvSource({"31, 26, 52", "30, 32, 41", "32, 32, 18", "31, 26, 53"})
  void pavedPathsLeadFromGateToStairToKeepToFountain(final int x, final int y, final int z) {
    assertTrue(built(MountainCastle.EYRIE).get(x, y, z).contains("stone_bricks"));
  }

  @ParameterizedTest(name = "{0},{1},{2} is {3}")
  @CsvSource({
    "31, 33, 13, blood_fountain",
    "30, 32, 13, polished_blackstone_bricks",
    "28, 34, 40, gargoyle[facing=south",
    "34, 34, 40, gargoyle[facing=south",
    "28, 33, 40, chiseled_stone_bricks"
  })
  void aBloodFountainAndGargoylesDressTheUpperWard(
      final int x, final int y, final int z, final String block) {
    final String state = built(MountainCastle.BASTION).get(x, y, z);
    assertTrue(state.contains(block), state);
  }

  @ParameterizedTest(name = "chapel {0}")
  @CsvSource({"true", "false"})
  void aGraveyardTakesTheChapelsPlaceWhenThereIsNone(final boolean chapel) {
    final Template t = built(chapel ? MountainCastle.EYRIE : MountainCastle.BASTION);
    for (final int[] grave : Wards.GRAVES) {
      final String stone = t.get(grave[0], MountainCastle.UP + 1, grave[1]);
      assertEquals(!chapel, stone.contains("cobblestone_wall"), stone);
      assertEquals(
          !chapel,
          t.get(grave[0], MountainCastle.UP + 2, grave[1]) != null
              && t.get(grave[0], MountainCastle.UP + 2, grave[1]).contains("skeleton_skull"));
    }
    assertEquals(!chapel, t.get(Chapel.WEST, MountainCastle.UP + 1, 20).contains("iron_bars"));
    assertEquals(BlockState.AIR, t.get(Chapel.WEST, MountainCastle.UP + 1, Chapel.DOOR_Z));
  }
}
