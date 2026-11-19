package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DeedKindsTest {

  @ParameterizedTest(name = "{0} is seen by the game: {1}")
  @CsvSource({
    "nightborn:fed, true",
    "nightborn:vampire_staked, true",
    "nightborn:accessories_held, true",
    "nightborn:accessories_worn, true",
    "nightborn:trial_completed, true",
    "nightborn:banes_dealt, true",
    "nightborn:journal_pages, true",
    "nightborn:wrong_call, true",
    "nightborn:parasol_walked, true",
    "nightborn:woke_rested, true",
    "nightborn:nemesis_ended, true",
    "nightborn:trapped, true",
    "nightborn:grounded, true",
    "nightborn:not_yet_reported, false"
  })
  void onlyDeedsTheGameReportsAreLive(final String kind, final boolean live) {
    assertEquals(live, DeedKinds.live().contains(Identifier.of(kind)));
  }
}
