package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class CoffinStyleTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource({"WOOD, coffin", "STONE, stone_coffin"})
  @DisplayName("each style names the model it is drawn with")
  void namesItsModel(final CoffinStyle style, final String model) {
    assertEquals(model, style.model());
  }

  @ParameterizedTest(name = "{0} open={1}")
  @CsvSource({
    "WOOD, true, nightborn:block.coffin.open",
    "WOOD, false, nightborn:block.coffin.close",
    "STONE, true, nightborn:block.stone_coffin.open",
    "STONE, false, nightborn:block.stone_coffin.close"
  })
  @DisplayName("each style's lid has its own open and close sounds")
  void soundsItsLid(final CoffinStyle style, final boolean open, final String sound) {
    assertEquals(sound, style.lid(open).getId().toString());
  }
}
