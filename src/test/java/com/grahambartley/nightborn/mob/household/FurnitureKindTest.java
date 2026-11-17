package com.grahambartley.nightborn.mob.household;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.decor.DecorBlocks;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.TagKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class FurnitureKindTest {

  static Stream<Arguments> blocks() {
    return Stream.of(
        Arguments.of(DecorBlocks.ORNATE_CHAIR, FurnitureKind.SEAT),
        Arguments.of(DecorBlocks.BURGUNDY_VELVET_DRAPE, FurnitureKind.DRAPE),
        Arguments.of(DecorBlocks.EMERALD_VELVET_DRAPE, FurnitureKind.DRAPE),
        Arguments.of(DecorBlocks.GRAMOPHONE, FurnitureKind.GRAMOPHONE),
        Arguments.of(Blocks.JUKEBOX, null),
        Arguments.of(Blocks.STONE, null),
        Arguments.of(Blocks.OAK_STAIRS, null));
  }

  @ParameterizedTest(name = "{0}: {1}")
  @MethodSource("blocks")
  @DisplayName("chairs, drapes and gramophones are known by their block")
  void byBlock(final Block block, final FurnitureKind kind) {
    assertEquals(Optional.ofNullable(kind), FurnitureKind.of(block.getDefaultState()));
  }

  static Stream<Arguments> tags() {
    return Stream.of(
        Arguments.of(FurnitureKind.READING, "nightborn:vampire_reading"),
        Arguments.of(FurnitureKind.TENDED_LIGHTS, "nightborn:vampire_tended_lights"));
  }

  @ParameterizedTest(name = "{1}")
  @MethodSource("tags")
  @DisplayName("books and lights are known by block tags a data pack can change")
  void tags(final TagKey<Block> tag, final String id) {
    assertEquals(id, tag.id().toString());
  }
}
