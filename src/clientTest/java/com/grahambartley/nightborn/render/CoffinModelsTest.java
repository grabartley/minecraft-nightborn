package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CoffinModelsTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "nightborn:oak_coffin, nightborn:block/oak_coffin_lid",
    "nightborn:dark_oak_coffin, nightborn:block/dark_oak_coffin_lid",
    "nightborn:crimson_coffin, nightborn:block/crimson_coffin_lid"
  })
  @DisplayName("each coffin's lid is a block model named after it")
  void namesTheLidAfterTheCoffin(final String coffin, final String lid) {
    assertEquals(Identifier.of(lid), CoffinModels.lid(Identifier.of(coffin)));
  }
}
