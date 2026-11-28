package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Coffins;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BedchambersTest {

  static Template rooms;

  @BeforeAll
  static void build() {
    rooms = new Template("vampire/test", Manor.SIZE_X, Manor.SIZE_Y, Manor.SIZE_Z);
    ManorShell.build(rooms);
    new Turret(true).build(rooms);
    Bedchambers.build(rooms);
  }

  @ParameterizedTest(
      name =
          "a half tester heading {0} has posts at {1},{2} and {3},{4}, a drape at {5},{6} and an open foot at {7},{8}")
  @CsvSource({
    "north, 3, 3, 5, 3, 4, 3, 3, 6",
    "south, 3, 7, 5, 7, 4, 7, 5, 4",
    "east, 6, 4, 6, 6, 6, 5, 3, 4",
    "west, 2, 4, 2, 6, 2, 5, 5, 6"
  })
  void halfTestersCanopyOnlyTheHead(
      final String head,
      final int x0,
      final int z0,
      final int x1,
      final int z1,
      final int drapeX,
      final int drapeZ,
      final int footX,
      final int footZ) {
    final Template t = new Template("vampire/test", 9, Manor.TOP + 1, 10);
    Bedchambers.halfTester(t, Manor.SPRUCE_COFFIN, 4, 5, head);
    assertTrue(t.get(4, Bedchambers.SEAT, 5).contains("part=foot"));
    for (final int[] post : new int[][] {{x0, z0}, {x1, z1}}) {
      assertTrue(
          t.get(post[0], Bedchambers.SEAT, post[1]).contains("fence"), post[0] + "," + post[1]);
      assertTrue(t.get(post[0], Bedchambers.TESTER, post[1]).contains("dark_oak_slab"));
    }
    assertTrue(t.get(drapeX, Bedchambers.SEAT, drapeZ).contains("velvet_drape"));
    assertNull(t.get(footX, Bedchambers.SEAT, footZ));
    assertNull(t.get(footX, Bedchambers.TESTER, footZ));
  }

  @ParameterizedTest(name = "a {3} lies at {0},{1},{2} with its canopy drape on the wall")
  @CsvSource({
    "22, 13, 24, spruce_coffin, 20, 24, 19, 24",
    "22, 13, 32, dark_oak_coffin, 22, 30, 22, 29",
    "28, 13, 32, dark_oak_coffin, 28, 30, 28, 29",
    "48, 13, 32, spruce_coffin, 48, 30, 48, 29",
    "54, 13, 32, spruce_coffin, 54, 30, 54, 29"
  })
  void everyCoffinIsCanopiedAgainstAWall(
      final int x,
      final int y,
      final int z,
      final String coffin,
      final int drapeX,
      final int drapeZ,
      final int wallX,
      final int wallZ) {
    assertTrue(rooms.get(x, y, z).contains(coffin + "[") && rooms.get(x, y, z).contains("foot"));
    assertTrue(rooms.get(drapeX, y, drapeZ).contains("velvet_drape"));
    final String behind = rooms.get(wallX, y, wallZ);
    assertTrue(!behind.contains("air") && !behind.contains("door"), behind);
  }

  @Test
  void everyCoffinLidOpensFreely() {
    Coffins.assertSound(rooms, List.of(Manor.DARK_OAK_COFFIN, Manor.SPRUCE_COFFIN));
    assertEquals(5, Coffins.in(rooms).size());
  }

  @ParameterizedTest(name = "{3} at {0},{1},{2}")
  @CsvSource({
    "53, 14, 22, chalice",
    "54, 13, 21, ornate_chair[facing=east",
    "56, 13, 28, chest",
    "56, 19, 42, lectern",
    "57, 19, 44, ornate_table",
    "56, 19, 40, chest",
    "21, 13, 19, chest",
    "29, 13, 40, chest",
    "47, 13, 40, chest"
  })
  void furnishesEachChamberAndTheStudy(final int x, final int y, final int z, final String block) {
    assertTrue(rooms.get(x, y, z).contains(block), rooms.get(x, y, z));
  }
}
