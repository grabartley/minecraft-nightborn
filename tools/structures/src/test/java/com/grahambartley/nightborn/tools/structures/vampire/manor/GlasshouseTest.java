package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class GlasshouseTest {

  static Template built(final boolean fallen) {
    final Template t = new Template("vampire/test", Manor.SIZE_X, Manor.SIZE_Y, Manor.SIZE_Z);
    Glasshouse.build(t, fallen);
    return t;
  }

  static int panesLeft(final Template t) {
    int glass = 0;
    for (int x = Glasshouse.WEST; x <= Glasshouse.EAST; x++) {
      for (int z = Glasshouse.NORTH; z <= Glasshouse.SOUTH; z++) {
        if ("minecraft:glass".equals(t.get(x, Glasshouse.roofY(x), z))) {
          glass++;
        }
      }
    }
    return glass;
  }

  @ParameterizedTest(name = "the roof at x {0} sits at y {1}")
  @CsvSource({"5, 8", "7, 10", "8, 11", "9, 10", "11, 8"})
  void theRoofIsAGable(final int x, final int y) {
    assertEquals(y, Glasshouse.roofY(x));
  }

  @ParameterizedTest(name = "fallen {0}: keeps between {1} and {2} roof panes")
  @CsvSource({"false, 48, 63", "true, 12, 32"})
  void fallenGlasshousesLoseMostOfTheirRoof(final boolean fallen, final int least, final int most) {
    final int left = panesLeft(built(fallen));
    assertTrue(least <= left && left <= most, "panes " + left);
  }

  @ParameterizedTest(name = "fallen {0}: the tree reaches past the roof {0}")
  @ValueSource(booleans = {true, false})
  void theTreeBreaksTheRoofOnlyWhenFallen(final boolean fallen) {
    final Template t = built(fallen);
    final String atRoof =
        t.get(Glasshouse.TREE_X, Glasshouse.roofY(Glasshouse.TREE_X), Glasshouse.TREE_Z);
    assertEquals(fallen, atRoof.contains("dark_oak_log"), atRoof);
  }

  @ParameterizedTest(name = "fallen {0}: a door and steps lead out to the garden")
  @ValueSource(booleans = {true, false})
  void aDoorLeadsToTheGarden(final boolean fallen) {
    final Template t = built(fallen);
    assertTrue(t.get(Glasshouse.WEST, Manor.F0 + 1, Glasshouse.DOOR_Z).contains("dark_oak_door"));
    for (int i = 1; i <= Manor.F0; i++) {
      assertTrue(
          t.get(Glasshouse.WEST - i, Manor.F0 + 1 - i, Glasshouse.DOOR_Z).contains("facing=east"));
    }
    assertEquals(BlockState.AIR, t.get(Glasshouse.EAST, Manor.F0 + 1, Glasshouse.DOOR_Z));
  }

  @ParameterizedTest(name = "glazing at {0},{1},{2} is {3}")
  @CsvSource({"4, 5, 11, iron_bars", "6, 5, 9, iron_bars", "4, 6, 10, glass_pane"})
  void glazingHasIronPosts(final int x, final int y, final int z, final String block) {
    assertEquals("minecraft:" + block, BlockState.full(Glasshouse.glazing(x, y, z, 0)));
  }
}
