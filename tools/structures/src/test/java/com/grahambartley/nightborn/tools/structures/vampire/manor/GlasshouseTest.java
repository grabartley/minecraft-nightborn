package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class GlasshouseTest {

  private static final Map<Boolean, Template> BUILT =
      Map.of(true, built(true), false, built(false));

  static Template built(final boolean fallen) {
    final Template t = new Template("vampire/test", Manor.SIZE_X, Manor.SIZE_Y, Manor.SIZE_Z);
    ManorShell.build(t);
    Glasshouse.build(t, fallen);
    return t;
  }

  @ParameterizedTest(name = "the glass roof is at y {1} over x {0}")
  @CsvSource({"6, 10", "8, 12", "11, 15", "12, 15", "17, 10"})
  void theRoofPeaksOverTheMiddle(final int x, final int y) {
    assertEquals(y, Glasshouse.roofY(x));
  }

  @ParameterizedTest(name = "{0},{1} under the glasshouse {2}")
  @CsvSource({"6, 17, true", "17, 29, true", "18, 22, false", "5, 20, false", "10, 30, false"})
  void coversItsFootprint(final int x, final int z, final boolean covered) {
    assertEquals(covered, Glasshouse.covers(x, z));
  }

  @ParameterizedTest(name = "fallen {0}: a door leads down steps to the garden")
  @ValueSource(booleans = {true, false})
  void aDoorLeadsDownToTheGarden(final boolean fallen) {
    final Template t = BUILT.get(fallen);
    for (int x = Glasshouse.DOOR_X; x <= Glasshouse.DOOR_X + 1; x++) {
      assertTrue(t.get(x, Manor.F0 + 1, Glasshouse.NORTH).contains("dark_oak_door"));
      for (int z = Glasshouse.STEP_FOOT_Z; z <= Glasshouse.STEP_TOP_Z; z++) {
        assertTrue(t.get(x, Manor.F0 - (Glasshouse.STEP_TOP_Z - z), z).contains("stairs"));
      }
    }
  }

  @ParameterizedTest(name = "fallen {0}: the tree crown stays under the glass {1}")
  @CsvSource({"true, false", "false, true"})
  void theTreeBreaksThroughOnlyWhenFallen(final boolean fallen, final boolean under) {
    final Template t = BUILT.get(fallen);
    int top = Manor.F0;
    for (int y = Manor.F0; y < t.sizeY(); y++) {
      final String state = t.get(Glasshouse.TREE_X, y, Glasshouse.TREE_Z);
      if (state != null && state.contains("dark_oak_log")) {
        top = y;
      }
    }
    assertEquals(under, top < Glasshouse.roofY(Glasshouse.TREE_X));
  }

  @ParameterizedTest(name = "fallen {0}: the lower course is stone at {1},{2}")
  @CsvSource({"true, 6, 20", "false, 9, 17", "false, 14, 29"})
  void theLowerCourseIsStone(final boolean fallen, final int x, final int z) {
    assertTrue(BUILT.get(fallen).get(x, Manor.F0 + 1, z).contains("gothic_stone"));
    assertTrue(!BlockState.AIR.equals(BUILT.get(fallen).get(x, Manor.F0, z)));
  }
}
