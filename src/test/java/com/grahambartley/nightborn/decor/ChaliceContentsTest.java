package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.blood.BloodQuality;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class ChaliceContentsTest {

  @ParameterizedTest(name = "{0} x{1} reads as {2} x{3}")
  @CsvSource({
    "EMPTY, 0, EMPTY, 0",
    "EMPTY, 5, EMPTY, 0",
    "ANIMAL, 0, EMPTY, 0",
    "ANIMAL, -2, EMPTY, 0",
    "ANIMAL, 3, ANIMAL, 3",
    "PLAYER, 7, PLAYER, 7",
    "PLAYER, 12, PLAYER, 7"
  })
  void keepsOneQualityAndAtMostSevenBottles(
      final ChaliceBlood blood, final int bottles, final ChaliceBlood keptBlood, final int kept) {
    final ChaliceContents contents = new ChaliceContents(blood, bottles);
    assertEquals(keptBlood, contents.blood());
    assertEquals(kept, contents.bottles());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(BloodQuality.class)
  void sevenBottlesFillItAndAllSevenComeBackOut(final BloodQuality quality) {
    ChaliceContents contents = ChaliceContents.EMPTY;
    for (int bottle = 1; bottle <= ChaliceContents.CAPACITY; bottle++) {
      assertTrue(contents.takes(quality));
      contents = contents.poured(quality);
      assertEquals(bottle, contents.bottles());
    }
    assertTrue(contents.full());
    assertFalse(contents.takes(quality));
    for (int bottle = ChaliceContents.CAPACITY - 1; bottle >= 0; bottle--) {
      assertEquals(quality, contents.quality().orElseThrow());
      contents = contents.less();
      assertEquals(bottle, contents.bottles());
    }
    assertTrue(contents.empty());
    assertEquals(ChaliceContents.EMPTY, contents);
  }

  @ParameterizedTest(name = "{0} into {1}")
  @CsvSource({"ANIMAL, VILLAGER", "VILLAGER, PLAYER", "PLAYER, ANIMAL"})
  void refusesAnotherQuality(final BloodQuality held, final BloodQuality poured) {
    assertFalse(new ChaliceContents(ChaliceBlood.of(held), 1).takes(poured));
  }

  @ParameterizedTest(name = "{0} bottles shows as {1}")
  @CsvSource({"0, 1", "1, 1", "4, 4", "7, 7"})
  void neverShowsLessThanTheFirstLevel(final int bottles, final int shown) {
    assertEquals(shown, new ChaliceContents(ChaliceBlood.ANIMAL, bottles).stateBottles());
  }

  @Test
  void survivesSaveAndLoad() {
    final ChaliceContents contents = new ChaliceContents(ChaliceBlood.VILLAGER, 5);
    assertEquals(
        contents,
        ChaliceContents.CODEC
            .parse(
                JsonOps.INSTANCE,
                ChaliceContents.CODEC.encodeStart(JsonOps.INSTANCE, contents).getOrThrow())
            .getOrThrow());
  }
}
