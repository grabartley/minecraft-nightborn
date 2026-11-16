package com.grahambartley.nightborn.tools.structures.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BlockStateTest {

  @Test
  void addsTheMinecraftNamespaceToABareName() {
    assertEquals("minecraft:stone", BlockState.of("stone"));
  }

  @Test
  void keepsAnExplicitNamespace() {
    assertEquals("nightborn:weapon_rack", BlockState.of("nightborn:weapon_rack"));
  }

  @Test
  void sortsPropertiesAndLowersTheirValues() {
    assertEquals(
        "minecraft:oak_door[facing=east,half=lower,open=false]",
        BlockState.of("oak_door", "open", false, "half", "LOWER", "facing", "east"));
  }

  @Test
  void writesNumbersAsText() {
    assertEquals("minecraft:sculk_sensor[power=0]", BlockState.of("sculk_sensor", "power", 0));
  }

  @Test
  void needsAValueForEveryProperty() {
    assertThrows(IllegalArgumentException.class, () -> BlockState.of("stone", "lonely"));
  }

  @Test
  void parsesANameAndItsPropertiesInOrder() {
    final BlockState.Parsed parsed = BlockState.parse("minecraft:chain[axis=y,waterlogged=false]");
    assertEquals("minecraft:chain", parsed.block());
    assertEquals(Map.of("axis", "y", "waterlogged", "false"), parsed.properties());
  }

  @Test
  void parsesABareNameWithNoProperties() {
    assertEquals(Map.of(), BlockState.parse("minecraft:air").properties());
  }

  @Test
  void withPropsReplacesAndAddsProperties() {
    assertEquals(
        "minecraft:lantern[hanging=true,waterlogged=false]",
        BlockState.withProps(
            "minecraft:lantern[hanging=false]", "hanging", true, "waterlogged", "false"));
  }

  @ParameterizedTest
  @CsvSource({"stone, minecraft:stone", "minecraft:stone, minecraft:stone", "md:x, md:x"})
  void fullAddsANamespaceOnlyWhenMissing(final String state, final String full) {
    assertEquals(full, BlockState.full(state));
  }
}
