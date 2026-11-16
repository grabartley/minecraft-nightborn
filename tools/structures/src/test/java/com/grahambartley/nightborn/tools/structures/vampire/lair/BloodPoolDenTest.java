package com.grahambartley.nightborn.tools.structures.vampire.lair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Coffins;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BloodPoolDenTest {

  private static final Template DEN = BloodPoolDen.build();

  @Test
  void holdsFiveLairCoffinsWhoseLidsCanSlideClear() {
    assertEquals(5, Coffins.in(DEN).size());
    Coffins.assertSound(
        DEN, List.of(LairFeatures.DEEPSLATE_COFFIN, LairFeatures.BLACKSTONE_COFFIN));
  }

  @Test
  void aFountainRisesFromTheMiddleOfThePool() {
    final int c = BloodPoolDen.CENTRE;
    assertTrue(DEN.get(c, 1, c).startsWith("nightborn:blood_fountain"));
    assertEquals(LairFeatures.POOL, DEN.get(c + 1, 0, c));
    assertEquals(LairFeatures.POOL, DEN.get(c, 0, c - 2));
  }

  @ParameterizedTest(name = "a chair at {0},{2} faces the pool ({3})")
  @CsvSource({"12, 1, 7, south", "17, 1, 12, west", "7, 1, 12, east"})
  void chairsGatherRoundThePool(final int x, final int y, final int z, final String facing) {
    assertTrue(DEN.get(x, y, z).contains("facing=" + facing), DEN.get(x, y, z));
  }

  @Test
  void theAltarHoldsAChaliceBetweenCandles() {
    assertTrue(DEN.get(4, 2, 18).startsWith("nightborn:chalice"));
    assertTrue(DEN.get(3, 2, 18).startsWith("minecraft:candle"));
    assertTrue(DEN.get(5, 2, 18).startsWith("minecraft:candle"));
  }

  @Test
  void theAltarPathRunsToThePoolKerb() {
    assertTrue(
        List.of("minecraft:polished_deepslate", "minecraft:cracked_deepslate_tiles")
            .contains(DEN.get(10, 0, 15)));
    assertTrue(DEN.get(10, 1, 14).startsWith("minecraft:polished_blackstone_brick_slab"));
    assertTrue(
        List.of("minecraft:polished_deepslate", "minecraft:cracked_deepslate_tiles")
            .contains(DEN.get(5, 0, 17)));
  }

  @Test
  void aReadingCornerSitsAwayFromThePool() {
    assertTrue(DEN.get(18, 1, 5).startsWith("minecraft:lectern"));
    assertTrue(DEN.get(17, 1, 5).startsWith("nightborn:ornate_chair"));
  }

  @Test
  void everyCoffinStandsOnTheFloor() {
    for (final Coffins.Coffin coffin : Coffins.in(DEN)) {
      final String below = DEN.get(coffin.foot().x(), coffin.foot().y() - 1, coffin.foot().z());
      assertTrue(below != null && !below.endsWith("air"), coffin.toString());
    }
  }
}
