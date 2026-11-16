package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ArmorDrawTest {

  @ParameterizedTest(name = "armour {0}, GeckoLib model {1}: {2}")
  @CsvSource({
    "true, false, VANILLA",
    "true, true, GECKO",
    "false, false, NONE",
    "false, true, NONE"
  })
  @DisplayName("worn armour is drawn by its own GeckoLib model when it has one, else as vanilla")
  void picksHowToDraw(final boolean armour, final boolean geckoModel, final ArmorDraw draw) {
    assertEquals(draw, ArmorDraw.of(armour, geckoModel));
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({"VANILLA, true", "GECKO, false", "NONE, false"})
  @DisplayName("only vanilla armour flattens the bust under it, having no room for one")
  void flattensBustUnderVanillaArmour(final ArmorDraw draw, final boolean flattened) {
    assertEquals(flattened, draw.flattensBust());
  }
}
