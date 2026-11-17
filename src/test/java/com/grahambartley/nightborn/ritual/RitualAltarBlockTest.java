package com.grahambartley.nightborn.ritual;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class RitualAltarBlockTest {

  @Test
  @DisplayName("a new altar faces north, sits idle and is drawn by its animated model")
  void defaults() {
    final BlockState state = RitualBlocks.ALTAR.getDefaultState();
    assertEquals(Direction.NORTH, state.get(RitualAltarBlock.FACING));
    assertEquals(AltarPhase.IDLE, state.get(RitualAltarBlock.PHASE));
    assertEquals(BlockRenderType.ENTITYBLOCK_ANIMATED, state.getRenderType());
  }

  @ParameterizedTest(name = "{0} turned {1} faces {2}")
  @CsvSource({
    "NORTH, CLOCKWISE_90, EAST",
    "EAST, CLOCKWISE_180, WEST",
    "SOUTH, COUNTERCLOCKWISE_90, EAST",
    "WEST, NONE, WEST"
  })
  @DisplayName("structures can turn the altar and it keeps its phase")
  void rotates(final Direction from, final BlockRotation rotation, final Direction to) {
    final BlockState state =
        RitualBlocks.ALTAR
            .getDefaultState()
            .with(RitualAltarBlock.FACING, from)
            .with(RitualAltarBlock.PHASE, AltarPhase.READY);
    final BlockState turned = state.rotate(rotation);
    assertEquals(to, turned.get(RitualAltarBlock.FACING));
    assertEquals(AltarPhase.READY, turned.get(RitualAltarBlock.PHASE));
  }

  @ParameterizedTest(name = "{0} mirrored {1} faces {2}")
  @CsvSource({"NORTH, LEFT_RIGHT, SOUTH", "EAST, LEFT_RIGHT, EAST", "EAST, FRONT_BACK, WEST"})
  @DisplayName("structures can mirror the altar")
  void mirrors(final Direction from, final BlockMirror mirror, final Direction to) {
    final BlockState state =
        RitualBlocks.ALTAR.getDefaultState().with(RitualAltarBlock.FACING, from);
    assertEquals(to, state.mirror(mirror).get(RitualAltarBlock.FACING));
  }
}
