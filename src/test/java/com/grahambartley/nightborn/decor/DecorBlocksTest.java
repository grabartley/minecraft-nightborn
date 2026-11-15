package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.registry.Registries;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DecorBlocksTest {

  static Stream<String> familyIds() {
    return BuildingFamily.ALL.stream().flatMap(family -> family.ids().stream());
  }

  @ParameterizedTest(name = "{0} is registered with an item")
  @MethodSource("familyIds")
  void registersEveryFamilyMemberWithItsItem(final String id) {
    final Block block = DecorBlocks.get(id);
    assertEquals("nightborn:" + id, Registries.BLOCK.getId(block).toString());
    assertEquals(block, Block.getBlockFromItem(Registries.ITEM.get(Registries.BLOCK.getId(block))));
  }

  @ParameterizedTest(name = "{0} is a {1}")
  @CsvSource({
    "gothic_stone_brick_stairs, StairsBlock",
    "mossy_gothic_stone_brick_slab, SlabBlock",
    "cracked_gothic_stone_brick_wall, WallBlock",
    "ebony_fence, FenceBlock",
    "candelabra, CandelabraBlock",
    "chandelier, ChandelierBlock",
    "ornate_chair, ChairBlock",
    "ornate_table, TableBlock"
  })
  void buildsEachMemberFromTheMatchingVanillaShape(final String id, final String kind) {
    final Class<?> expected =
        switch (kind) {
          case "StairsBlock" -> StairsBlock.class;
          case "SlabBlock" -> SlabBlock.class;
          case "WallBlock" -> WallBlock.class;
          case "FenceBlock" -> FenceBlock.class;
          case "CandelabraBlock" -> CandelabraBlock.class;
          case "ChandelierBlock" -> ChandelierBlock.class;
          case "ChairBlock" -> ChairBlock.class;
          default -> TableBlock.class;
        };
    assertTrue(expected.isInstance(DecorBlocks.get(id)));
  }

  @Test
  void refusesAnUnknownId() {
    assertThrows(IllegalArgumentException.class, () -> DecorBlocks.get("velvet_coffin"));
  }
}
