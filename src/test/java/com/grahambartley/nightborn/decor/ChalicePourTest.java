package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.blood.BloodQuality;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class ChalicePourTest {

  @ParameterizedTest(name = "{0} x{1}, {2} of {3}, vampire={4} -> {5}")
  @CsvSource({
    "EMPTY, 0, BLOOD_BOTTLE, ANIMAL, false, FILL",
    "EMPTY, 0, BLOOD_BOTTLE, PLAYER, true, FILL",
    "ANIMAL, 1, BLOOD_BOTTLE, ANIMAL, false, FILL",
    "ANIMAL, 6, BLOOD_BOTTLE, ANIMAL, false, FILL",
    "ANIMAL, 7, BLOOD_BOTTLE, ANIMAL, false, BRIMMING",
    "ANIMAL, 3, BLOOD_BOTTLE, VILLAGER, false, WRONG_QUALITY",
    "PLAYER, 7, BLOOD_BOTTLE, VILLAGER, false, BRIMMING",
    "PLAYER, 1, GLASS_BOTTLE, ANIMAL, false, BOTTLE",
    "VILLAGER, 7, GLASS_BOTTLE, ANIMAL, true, BOTTLE",
    "EMPTY, 0, GLASS_BOTTLE, ANIMAL, false, NONE",
    "VILLAGER, 2, EMPTY_HAND, ANIMAL, true, DRINK",
    "VILLAGER, 2, EMPTY_HAND, ANIMAL, false, NONE",
    "EMPTY, 0, EMPTY_HAND, ANIMAL, true, NONE",
    "PLAYER, 4, OTHER, ANIMAL, true, NONE",
    "EMPTY, 0, OTHER, ANIMAL, false, NONE"
  })
  void decidesWhatAUseDoes(
      final ChaliceBlood blood,
      final int bottles,
      final ChalicePour.Held held,
      final BloodQuality poured,
      final boolean vampire,
      final String expected) {
    final Optional<ChalicePour> want =
        "NONE".equals(expected) ? Optional.empty() : Optional.of(ChalicePour.valueOf(expected));
    assertEquals(
        want,
        ChalicePour.decide(
            new ChaliceContents(blood, bottles), held, Optional.of(poured), vampire));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(ChalicePour.class)
  void onlyARefusalCarriesAMessage(final ChalicePour pour) {
    assertEquals(
        pour == ChalicePour.WRONG_QUALITY || pour == ChalicePour.BRIMMING,
        pour.refusal().isPresent());
  }
}
