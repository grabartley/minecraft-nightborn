package com.grahambartley.nightborn.gramophone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.decor.DecorBlockEntities;
import com.grahambartley.nightborn.decor.DecorBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class GramophoneBlockTest {

  private static BlockState facing(final Direction direction) {
    return DecorBlocks.GRAMOPHONE.getDefaultState().with(GramophoneBlock.FACING, direction);
  }

  @Test
  @DisplayName("a gramophone is placed empty and silent")
  void placedEmpty() {
    final BlockState state = DecorBlocks.GRAMOPHONE.getDefaultState();
    assertFalse(state.get(GramophoneBlock.HAS_RECORD));
    assertFalse(state.get(GramophoneBlock.PLAYING));
    assertEquals(Direction.NORTH, state.get(GramophoneBlock.FACING));
  }

  @Test
  @DisplayName("a gramophone shares the jukebox's disc property and signals like one")
  void behavesLikeAJukebox() {
    final BlockState state = DecorBlocks.GRAMOPHONE.getDefaultState();
    assertTrue(Blocks.JUKEBOX.getDefaultState().contains(GramophoneBlock.HAS_RECORD));
    assertTrue(state.emitsRedstonePower());
    assertTrue(state.hasComparatorOutput());
    assertEquals(Blocks.JUKEBOX.getHardness(), DecorBlocks.GRAMOPHONE.getHardness());
    assertTrue(DecorBlockEntities.GRAMOPHONE.supports(state));
    assertTrue(
        ((GramophoneBlock) DecorBlocks.GRAMOPHONE).createBlockEntity(BlockPos.ORIGIN, state)
            instanceof GramophoneBlockEntity);
  }

  @ParameterizedTest(name = "{0} turned {1} faces {2}")
  @CsvSource({
    "NORTH, CLOCKWISE_90, EAST",
    "EAST, CLOCKWISE_180, WEST",
    "SOUTH, COUNTERCLOCKWISE_90, EAST",
    "WEST, NONE, WEST"
  })
  @DisplayName("a structure that turns a gramophone turns its horn with it")
  void rotates(final Direction from, final BlockRotation rotation, final Direction to) {
    assertEquals(to, facing(from).rotate(rotation).get(GramophoneBlock.FACING));
  }

  @ParameterizedTest(name = "{0} mirrored {1} faces {2}")
  @CsvSource({"NORTH, LEFT_RIGHT, SOUTH", "EAST, FRONT_BACK, WEST", "EAST, LEFT_RIGHT, EAST"})
  @DisplayName("a mirrored structure mirrors the gramophone")
  void mirrors(final Direction from, final BlockMirror mirror, final Direction to) {
    assertEquals(to, facing(from).mirror(mirror).get(GramophoneBlock.FACING));
  }
}
