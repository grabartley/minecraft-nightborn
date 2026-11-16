package com.grahambartley.nightborn.tools.structures.vampire.castle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.Template;
import java.util.Set;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CourtyardTest {

  private static Template yard(final Castle.Layout layout) {
    final Template t = new Template("vampire/test", Castle.SIZE, Castle.HEIGHT, Castle.SIZE);
    Courtyard.build(t, layout);
    return t;
  }

  @ParameterizedTest(name = "{0},{1}")
  @CsvSource({"2, 2", "10, 30", "33, 7", "38, 38"})
  void theGroundIsWornEarth(final int x, final int z) {
    assertTrue(
        Set.of("coarse_dirt", "podzol", "gravel", "grass_block").contains(Courtyard.ground(x, z)));
  }

  @ParameterizedTest(name = "{0}: fountain {1}, graves {2}")
  @CsvSource({"BASTIONS, true, false", "RUINED, false, true"})
  void eachLayoutHasItsOwnYard(final String name, final boolean fountain, final boolean graves) {
    final Template t = yard(name.equals("RUINED") ? Castle.RUINED : Castle.BASTIONS);
    assertEquals(
        fountain,
        "nightborn:blood_fountain".equals(t.get(Castle.MID, Castle.FLOOR, Courtyard.FOUNTAIN_Z)));
    final int[] grave = Courtyard.GRAVES[0];
    final String marker = t.get(grave[0], Castle.FLOOR + 1, grave[1]);
    assertEquals(graves, marker != null && marker.startsWith("minecraft:skeleton_skull"));
  }

  @ParameterizedTest(name = "the path at z {0}")
  @CsvSource({"31", "35", "38"})
  void aPathRunsFromTheGateToTheKeep(final int z) {
    final Template t = yard(Castle.BASTIONS);
    for (int x = Castle.MID - 1; x <= Castle.MID + 1; x++) {
      assertTrue(t.get(x, Castle.GROUND, z).contains("stone_bricks"), t.get(x, Castle.GROUND, z));
    }
  }
}
