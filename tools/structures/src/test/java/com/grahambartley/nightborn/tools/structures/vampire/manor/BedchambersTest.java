package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.api.BeforeAll;
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

  @ParameterizedTest(name = "{3} at {0},{1},{2}")
  @CsvSource({
    "19, 10, 21, dark_oak_coffin",
    "19, 10, 24, dark_oak_coffin",
    "15, 10, 13, spruce_coffin",
    "34, 10, 20, spruce_coffin",
    "36, 10, 19, spruce_coffin",
    "35, 11, 11, chalice",
    "40, 15, 24, ornate_chair",
    "38, 15, 22, lectern",
    "38, 15, 21, chest",
    "42, 15, 22, bookshelf",
    "14, 11, 18, portrait"
  })
  void furnishesEachChamber(final int x, final int y, final int z, final String block) {
    assertTrue(rooms.get(x, y, z).contains(block), rooms.get(x, y, z));
  }
}
