package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.decor.DecorBlocks;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class CoffinMaterialTest {

  static Stream<Arguments> materials() {
    return Stream.of(
        Arguments.of(CoffinMaterial.OAK, "oak_coffin", CoffinStyle.WOOD, Blocks.OAK_PLANKS),
        Arguments.of(
            CoffinMaterial.SPRUCE, "spruce_coffin", CoffinStyle.WOOD, Blocks.SPRUCE_PLANKS),
        Arguments.of(
            CoffinMaterial.DARK_OAK, "dark_oak_coffin", CoffinStyle.WOOD, Blocks.DARK_OAK_PLANKS),
        Arguments.of(
            CoffinMaterial.MANGROVE, "mangrove_coffin", CoffinStyle.WOOD, Blocks.MANGROVE_PLANKS),
        Arguments.of(
            CoffinMaterial.CHERRY, "cherry_coffin", CoffinStyle.WOOD, Blocks.CHERRY_PLANKS),
        Arguments.of(
            CoffinMaterial.CRIMSON, "crimson_coffin", CoffinStyle.WOOD, Blocks.CRIMSON_PLANKS),
        Arguments.of(
            CoffinMaterial.STONE_BRICK,
            "stone_brick_coffin",
            CoffinStyle.STONE,
            Blocks.STONE_BRICKS),
        Arguments.of(
            CoffinMaterial.DEEPSLATE_BRICK,
            "deepslate_brick_coffin",
            CoffinStyle.STONE,
            Blocks.DEEPSLATE_BRICKS),
        Arguments.of(
            CoffinMaterial.POLISHED_BLACKSTONE_BRICK,
            "polished_blackstone_brick_coffin",
            CoffinStyle.STONE,
            Blocks.POLISHED_BLACKSTONE_BRICKS),
        Arguments.of(
            CoffinMaterial.GOTHIC_STONE_BRICK,
            "gothic_stone_brick_coffin",
            CoffinStyle.STONE,
            DecorBlocks.get("gothic_stone_bricks")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("materials")
  @DisplayName("each material names its coffin, its style and the block it is made from")
  void describesTheMaterial(
      final CoffinMaterial material, final String id, final CoffinStyle style, final Block source) {
    assertEquals(id, material.id());
    assertEquals(style, material.style());
    assertEquals(source, material.source());
  }
}
