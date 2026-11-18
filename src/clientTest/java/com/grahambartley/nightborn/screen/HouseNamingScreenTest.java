package com.grahambartley.nightborn.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.DyeColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HouseNamingScreenTest {

  @ParameterizedTest(name = "{0} stepped {1} -> {2}")
  @CsvSource({
    "WHITE, 1, ORANGE",
    "WHITE, -1, BLACK",
    "BLACK, 1, WHITE",
    "PURPLE, 16, PURPLE",
    "RED, 0, RED"
  })
  @DisplayName("stepping a crest colour runs through all sixteen dyes and wraps round")
  void dye(final DyeColor from, final int by, final DyeColor to) {
    assertEquals(to, HouseNamingScreen.dye(from, by));
  }
}
