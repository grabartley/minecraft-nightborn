package com.grahambartley.nightborn.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class GearBlocksTest {

  private static final Path ASSETS = Path.of("src/main/resources/assets/nightborn");

  static Stream<Arguments> ores() {
    return Stream.of(
        Arguments.of("silver_ore", GearBlocks.SILVER_ORE),
        Arguments.of("deepslate_silver_ore", GearBlocks.DEEPSLATE_SILVER_ORE));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("ores")
  @DisplayName("each ore is registered with its item, blockstate and model, and needs a tool")
  void ore(final String name, final Block ore) {
    assertEquals("nightborn:" + name, Registries.BLOCK.getId(ore).toString());
    assertEquals(ore, ((BlockItem) ore.asItem()).getBlock());
    assertTrue(ore.getDefaultState().isToolRequired());
    assertTrue(Files.exists(ASSETS.resolve("blockstates/" + name + ".json")));
    assertTrue(Files.exists(ASSETS.resolve("models/block/" + name + ".json")));
  }
}
