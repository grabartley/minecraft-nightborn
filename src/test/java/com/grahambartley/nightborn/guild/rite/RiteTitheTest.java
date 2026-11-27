package com.grahambartley.nightborn.guild.rite;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RiteTitheTest {

  @ParameterizedTest(name = "{0} emeralds against a tithe of {1} -> {2}")
  @CsvSource({"16, 16, true", "64, 16, true", "15, 16, false", "0, 16, false", "0, 0, true"})
  @DisplayName("the tithe is met only with every emerald of it in hand")
  void affords(final int held, final int price, final boolean affords) {
    assertEquals(affords, RiteTithe.affords(held, price));
  }
}
