package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ChalicePourTest {

  @ParameterizedTest(name = "{0} chalice, {1}, vampire={2} -> {3}")
  @CsvSource({
    "EMPTY, BLOOD_BOTTLE, false, FILL",
    "EMPTY, BLOOD_BOTTLE, true, FILL",
    "ANIMAL, BLOOD_BOTTLE, false, NONE",
    "PLAYER, GLASS_BOTTLE, false, BOTTLE",
    "VILLAGER, GLASS_BOTTLE, true, BOTTLE",
    "EMPTY, GLASS_BOTTLE, false, NONE",
    "VILLAGER, EMPTY_HAND, true, DRINK",
    "VILLAGER, EMPTY_HAND, false, NONE",
    "EMPTY, EMPTY_HAND, true, NONE",
    "PLAYER, OTHER, true, NONE",
    "EMPTY, OTHER, false, NONE"
  })
  void decidesWhatAUseDoes(
      final ChaliceBlood contents,
      final ChalicePour.Held held,
      final boolean vampire,
      final String expected) {
    final Optional<ChalicePour> want =
        "NONE".equals(expected) ? Optional.empty() : Optional.of(ChalicePour.valueOf(expected));
    assertEquals(want, ChalicePour.decide(contents, held, vampire));
  }
}
