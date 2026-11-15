package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CoffinModelsTest {

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "oak_coffin",
        "spruce_coffin",
        "dark_oak_coffin",
        "mangrove_coffin",
        "cherry_coffin",
        "crimson_coffin"
      })
  @DisplayName("each coffin's lid is a block model named after it")
  void namesTheLidAfterTheCoffin(final String coffin) {
    assertEquals(
        Identifier.of("nightborn", "block/" + coffin + "_lid"),
        CoffinModels.lid(Identifier.of("nightborn", coffin)));
  }
}
