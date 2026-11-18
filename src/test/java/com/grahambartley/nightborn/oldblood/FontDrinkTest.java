package com.grahambartley.nightborn.oldblood;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class FontDrinkTest {

  @ParameterizedTest(name = "empty hand {0}, vampire {1}, full {2}, sick {3}, journal {4} -> {5}")
  @CsvSource({
    "true, false, true, false, false, DRINKS",
    "true, false, true, false, true, DRINKS",
    "true, false, false, false, false, DRY",
    "true, false, true, true, false, ALREADY_SICK",
    "true, false, false, true, false, DRY",
    "false, false, true, false, false, PASS",
    "true, true, true, false, true, PASS",
    "true, true, false, false, true, PASS",
    "true, true, true, false, false, JOURNAL",
    "true, true, false, false, false, JOURNAL",
    "false, true, true, false, false, PASS"
  })
  @DisplayName(
      "a human with an empty hand drinks a full font, and a vampire gets only a lost journal back")
  void decides(
      final boolean emptyHand,
      final boolean vampire,
      final boolean full,
      final boolean sick,
      final boolean carriesJournal,
      final FontDrink expected) {
    assertEquals(expected, FontDrink.decide(emptyHand, vampire, full, sick, carriesJournal));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(FontDrink.class)
  @DisplayName("every refusal says why")
  void refusals(final FontDrink drink) {
    assertEquals(
        drink == FontDrink.DRY || drink == FontDrink.ALREADY_SICK, drink.refusal().isPresent());
  }
}
