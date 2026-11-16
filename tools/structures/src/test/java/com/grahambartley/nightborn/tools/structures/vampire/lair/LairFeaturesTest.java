package com.grahambartley.nightborn.tools.structures.vampire.lair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Facing;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Coffins;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class LairFeaturesTest {

  private static Template room() {
    final Template t = new Template("vampire/test", 15, 8, 15);
    LairCave.carve(t);
    return t;
  }

  @ParameterizedTest(name = "a shrine facing {0} stands its coffin in an open arch")
  @ValueSource(strings = {"north", "south", "east", "west"})
  void shrinesHoldAStandingCoffinWithRoomToOpen(final String front) {
    final Template t = room();
    final var step = Facing.STEP.get(front);
    final int x = 7 - step.x() * 6;
    final int z = 7 - step.z() * 6;
    LairFeatures.shrine(t, x, z, front, LairFeatures.DEEPSLATE_COFFIN);
    final List<Coffins.Coffin> coffins = Coffins.in(t);
    assertEquals(1, coffins.size());
    assertTrue(coffins.get(0).standing());
    assertEquals(Facing.OPPOSITE.get(front), coffins.get(0).facing());
    Coffins.assertSound(t, List.of(LairFeatures.DEEPSLATE_COFFIN));
    assertEquals(BlockState.AIR, t.get(x + step.x(), 1, z + step.z()));
  }

  @ParameterizedTest(name = "{2} blocks from the centre is {3}")
  @CsvSource({
    "7, 7, 0, nightborn:blood_pool",
    "9, 7, 2, nightborn:blood_pool",
    "10, 7, 3, minecraft:polished_blackstone_bricks"
  })
  void thePoolIsRingedByAKerb(final int x, final int z, final int r, final String floor) {
    final Template t = room();
    LairFeatures.pool(t, 7, 7, 2.5);
    assertEquals(floor, t.get(x, 0, z));
    if (floor.contains("blackstone")) {
      assertTrue(t.get(x, 1, z).startsWith("minecraft:polished_blackstone_brick_slab"));
    }
  }

  @ParameterizedTest(name = "a stalagmite {0} tall ends in a tip")
  @ValueSource(ints = {1, 2, 3})
  void stalagmitesRiseToAPoint(final int height) {
    final Template t = room();
    LairFeatures.stalagmite(t, 4, 4, height);
    assertTrue(t.get(4, height, 4).contains("thickness=frustum"));
    assertTrue(t.get(4, height + 1, 4).contains("thickness=tip"));
    for (int y = 1; y < height; y++) {
      assertEquals("minecraft:dripstone_block", t.get(4, y, 4));
    }
  }

  @Test
  void bonesLieWithASkull() {
    final Template t = room();
    LairFeatures.bones(t, 4, 4, 2);
    assertTrue(t.get(4, 1, 4).startsWith("minecraft:bone_block"));
    assertTrue(t.get(5, 1, 4).startsWith("minecraft:skeleton_skull"));
    assertTrue(t.get(4, 2, 4).startsWith("minecraft:skeleton_skull"));
  }

  @ParameterizedTest(name = "a reading corner facing {0} seats its reader at the lectern")
  @CsvSource({"west, 12, 10, 9", "east, 2, 4, 5"})
  void readingCornersFaceTheirReader(
      final String toward, final int shelfX, final int lecternX, final int chairX) {
    final Template t = room();
    LairFeatures.readingCorner(t, shelfX, lecternX, 7, toward);
    for (int z = 6; z <= 8; z++) {
      assertEquals("minecraft:bookshelf", t.get(shelfX, 1, z));
      assertEquals("minecraft:bookshelf", t.get(shelfX, 2, z));
    }
    assertTrue(t.get(lecternX, 1, 7).contains("facing=" + toward));
    assertTrue(
        t.get(chairX, 1, 7).contains("facing=" + Facing.OPPOSITE.get(toward)), t.get(chairX, 1, 7));
  }

  @ParameterizedTest(name = "the path tile at {0},{1} is worn deepslate")
  @CsvSource({"2, 2", "3, 5", "4, 9"})
  void pathsAreWornDeepslate(final int x, final int z) {
    final Template t = room();
    LairFeatures.path(t, 2, 2, 4, 9);
    final String tile = t.get(x, 0, z);
    assertTrue(
        tile.equals("minecraft:polished_deepslate")
            || tile.equals("minecraft:cracked_deepslate_tiles"),
        tile);
  }
}
