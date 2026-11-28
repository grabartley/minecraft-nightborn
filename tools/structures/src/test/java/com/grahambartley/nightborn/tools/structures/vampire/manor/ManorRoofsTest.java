package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ManorRoofsTest {

  static Template t;

  @BeforeAll
  static void build() {
    t = new Template("vampire/test", Manor.SIZE_X, Manor.SIZE_Y, Manor.SIZE_Z);
    ManorShell.build(t);
    ManorRoofs.build(t);
  }

  @ParameterizedTest(name = "the roof rises {1} over z {0}")
  @CsvSource({"15, 0", "16, 1", "22, 7", "29, 14", "36, 7", "42, 1", "43, 0", "50, 0"})
  void risesToARidgeOverTheMiddle(final int z, final int rise) {
    assertEquals(rise, ManorRoofs.rise(z));
  }

  @ParameterizedTest(name = "at x {0} the slope at z {1} is slate")
  @CsvSource({"25, 20", "50, 35", "30, 15", "46, 43"})
  void theSlopesAreSlate(final int x, final int z) {
    assertTrue(t.get(x, Manor.TOP + ManorRoofs.rise(z), z).contains("deepslate_tile"));
  }

  @ParameterizedTest(name = "the attic is filled solid at {0},{1},{2}")
  @CsvSource({"30, 20, 29", "50, 25, 25", "25, 19, 20"})
  void theAtticIsSolid(final int x, final int y, final int z) {
    assertEquals(Manor.PLANKS, t.get(x, y, z));
  }

  @ParameterizedTest(name = "the gables stand in gothic stone at x {0}")
  @CsvSource({"18", "58"})
  void gableEndsAreStone(final int x) {
    assertTrue(t.get(x, Manor.TOP + 6, ManorRoofs.RIDGE_Z).contains("gothic_stone"));
  }

  @Test
  void aFrontGableRisesOverTheHall() {
    assertTrue(t.get(Manor.MID_X, Manor.TOP + 3, Manor.HOUSE_S).contains("gothic_stone"));
    assertTrue(t.get(ManorRoofs.GABLE_W, Manor.TOP + 1, Manor.HOUSE_S).contains("stairs"));
  }
}
