package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ClothingColorsTest {

  @ParameterizedTest(name = "layer {0} of colour {1}")
  @CsvSource({
    "0, 0x5A1A24, 0xFF5A1A24",
    "0, 0x000000, 0xFF000000",
    "1, 0x5A1A24, 0xFFFFFFFF",
    "2, 0x1F4A3A, 0xFFFFFFFF"
  })
  @DisplayName("the sprite's cloth layer takes the dye and its trim layer keeps its own colours")
  void tintsTheClothLayerOnly(final int layer, final String color, final String tint) {
    assertEquals(
        Integer.parseUnsignedInt(tint.substring(2), 16),
        ClothingColors.tint(layer, Integer.parseInt(color.substring(2), 16)));
  }
}
