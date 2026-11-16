package com.grahambartley.nightborn.tools.structures.vampire.crypt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Coffins;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OssuaryTest {

  private static final Template CRYPT = Ossuary.build();

  @Test
  void holdsFourStoneCoffinsWhoseLidsCanSlideClear() {
    assertEquals(4, Coffins.in(CRYPT).size());
    Coffins.assertSound(CRYPT, List.of(Undercroft.STONE_COFFIN, Undercroft.GOTHIC_COFFIN));
  }

  @ParameterizedTest(
      name = "the charnel shelf at {0},{1} stacks skulls between bones facing rotation {2}")
  @CsvSource({"9, 5, 0", "11, 5, 0", "1, 9, 8", "4, 9, 8", "10, 9, 8"})
  void charnelShelvesFaceTheGallery(final int x, final int z, final int rotation) {
    assertTrue(CRYPT.get(x, 1, z).startsWith("minecraft:bone_block"));
    assertEquals("minecraft:skeleton_skull[rotation=" + rotation + "]", CRYPT.get(x, 2, z));
    assertTrue(CRYPT.get(x, 3, z).startsWith("minecraft:bone_block"));
  }

  @ParameterizedTest(name = "the gallery at x {0} is a pointed barrel vault")
  @CsvSource({"1", "6", "11"})
  void theGalleryIsBarrelVaulted(final int x) {
    assertTrue(CRYPT.get(x, 4, Ossuary.NORTH).contains("facing=north,half=top"));
    assertTrue(CRYPT.get(x, 4, Ossuary.SOUTH).contains("facing=south,half=top"));
    assertTrue(
        CRYPT.get(x, 4, 7).equals(BlockState.AIR)
            || CRYPT.get(x, 4, 7).startsWith("nightborn:chandelier"));
  }

  @Test
  void bothChapelsOpenOffTheGallery() {
    assertEquals(BlockState.AIR, CRYPT.get(2, 1, 5));
    assertEquals(BlockState.AIR, CRYPT.get(7, 1, 9));
  }

  @Test
  void chandeliersHangFromTheVault() {
    Coffins.assertHung(CRYPT);
  }
}
