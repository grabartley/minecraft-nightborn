package com.grahambartley.nightborn.tools.structures.vampire.manor;

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
    final Turret turret = new Turret(true);
    turret.build(rooms);
    Bedchambers.build(rooms, turret);
  }

  @ParameterizedTest(
      name = "a four-poster heading {0} has posts at {1},{2} and {3},{4} and a drape at {5},{6}")
  @CsvSource({
    "north, 3, 3, 5, 6, 4, 3",
    "south, 3, 4, 5, 7, 4, 7",
    "east, 3, 4, 6, 6, 6, 5",
    "west, 2, 4, 5, 6, 2, 5"
  })
  void fourPostersFrameTheCoffin(
      final String head,
      final int x0,
      final int z0,
      final int x1,
      final int z1,
      final int drapeX,
      final int drapeZ) {
    final Template t = new Template("vampire/test", 9, Manor.TOP + 1, 10);
    Bedchambers.fourPoster(t, Manor.SPRUCE_COFFIN, 4, 5, head);
    assertTrue(t.get(4, Bedchambers.SEAT, 5).contains("part=foot"));
    for (final int px : new int[] {x0, x1}) {
      for (final int pz : new int[] {z0, z1}) {
        assertTrue(t.get(px, Bedchambers.SEAT, pz).contains("fence"), px + "," + pz);
        assertTrue(t.get(px, Bedchambers.TESTER, pz).contains("dark_oak_slab"));
      }
    }
    assertTrue(
        t.get(drapeX, Bedchambers.SEAT, drapeZ).contains("velvet_drape"),
        t.get(drapeX, Bedchambers.SEAT, drapeZ));
  }

  @ParameterizedTest(name = "{3} at {0},{1},{2}")
  @CsvSource({
    "19, 10, 21, dark_oak_coffin",
    "19, 10, 24, dark_oak_coffin",
    "15, 10, 13, spruce_coffin",
    "34, 10, 20, spruce_coffin",
    "37, 10, 20, spruce_coffin",
    "35, 11, 11, chalice",
    "40, 15, 22, ornate_chair",
    "40, 15, 20, lectern",
    "42, 15, 22, chest",
    "14, 11, 18, portrait"
  })
  void furnishesEachChamber(final int x, final int y, final int z, final String block) {
    assertTrue(rooms.get(x, y, z).contains(block), rooms.get(x, y, z));
  }
}
