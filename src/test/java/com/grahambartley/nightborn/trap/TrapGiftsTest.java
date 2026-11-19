package com.grahambartley.nightborn.trap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TrapGiftsTest {

  static final Identifier SNARE = Identifier.of("nightborn", "trapper/snare");
  static final Identifier FLASH = Identifier.of("nightborn", "trapper/flash_trap");

  static List<TrapGift> fieldCraft(final int ranks) {
    final float[] seen = {6.0F, 4.0F, 2.0F};
    final List<TrapGift> gifts = new ArrayList<>();
    for (int i = 0; i < ranks; i++) {
      gifts.add(new TrapGift(Optional.empty(), 0.25, Optional.of(seen[i]), 0, 0.0F, false, 0.0));
    }
    return gifts;
  }

  static TrapGift forSnare(final int extraLimit, final float extraDamage) {
    return new TrapGift(
        Optional.of(SNARE), 0.0, Optional.empty(), extraLimit, extraDamage, false, 0.0);
  }

  @ParameterizedTest(name = "{0} Field Craft ranks -> arms in {1} ticks, seen within {2}")
  @CsvSource({"0, 40, 8", "1, 30, 6", "2, 20, 4", "3, 10, 2"})
  @DisplayName("each Field Craft rank sets traps a quarter faster and hides them closer")
  void fieldCraftRanks(final int ranks, final int armTicks, final float seenWithin) {
    final List<TrapGift> gifts = fieldCraft(ranks);
    assertEquals(armTicks, TrapGifts.armTicks(40, gifts));
    assertEquals(seenWithin, TrapGifts.seenWithin(8.0F, gifts));
  }

  @ParameterizedTest(name = "{0} Quick Hands ranks -> {1} snares, {2} flash traps")
  @CsvSource({"0, 2, 1", "1, 3, 1", "3, 5, 1"})
  @DisplayName("Quick Hands adds snares only")
  void quickHands(final int ranks, final int snares, final int flashes) {
    final List<TrapGift> gifts = new ArrayList<>();
    for (int i = 0; i < ranks; i++) {
      gifts.add(forSnare(1, 0.0F));
    }
    assertEquals(snares, TrapGifts.limit(2, TrapGifts.forPower(gifts, SNARE)));
    assertEquals(flashes, TrapGifts.limit(1, TrapGifts.forPower(gifts, FLASH)));
  }

  @ParameterizedTest(name = "{0} Silver Edge ranks -> {1} more damage")
  @CsvSource({"0, 0", "1, 2", "2, 4", "3, 6"})
  @DisplayName("Silver Edge adds two damage a rank")
  void silverEdge(final int ranks, final float damage) {
    final List<TrapGift> gifts = new ArrayList<>();
    for (int i = 0; i < ranks; i++) {
      gifts.add(forSnare(0, 2.0F));
    }
    assertEquals(damage, TrapGifts.extraDamage(TrapGifts.forPower(gifts, SNARE)));
    assertEquals(0.0F, TrapGifts.extraDamage(TrapGifts.forPower(gifts, FLASH)));
  }

  @ParameterizedTest(name = "senses {0}, pinned {1}")
  @CsvSource({"false, 0.0", "true, 0.0", "false, 0.25", "true, 0.25"})
  @DisplayName("Trap Sense and Master Trapper are read from any gift held")
  void sensesAndPins(final boolean senses, final double pinned) {
    final List<TrapGift> gifts =
        List.of(
            new TrapGift(Optional.empty(), 0.0, Optional.empty(), 0, 0.0F, senses, 0.0),
            new TrapGift(Optional.empty(), 0.0, Optional.empty(), 0, 0.0F, false, pinned),
            forSnare(1, 2.0F));
    assertEquals(senses, TrapGifts.senses(gifts));
    assertEquals(pinned, TrapGifts.pinnedDamage(gifts));
  }
}
