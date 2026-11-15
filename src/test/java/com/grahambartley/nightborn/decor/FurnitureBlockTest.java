package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.stream.Stream;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class FurnitureBlockTest {

  private static final String[] FURNITURE = {
    "candelabra", "chandelier", "burgundy_velvet_drape", "emerald_velvet_drape"
  };

  static Stream<Arguments> rotations() {
    return Stream.of(FURNITURE)
        .flatMap(
            id ->
                Stream.of(
                    Arguments.of(id, BlockRotation.NONE, Direction.NORTH),
                    Arguments.of(id, BlockRotation.CLOCKWISE_90, Direction.EAST),
                    Arguments.of(id, BlockRotation.CLOCKWISE_180, Direction.SOUTH),
                    Arguments.of(id, BlockRotation.COUNTERCLOCKWISE_90, Direction.WEST)));
  }

  @ParameterizedTest(name = "{0} under {1} faces {2}")
  @MethodSource("rotations")
  void turnsWithTheStructureItIsPlacedIn(
      final String id, final BlockRotation rotation, final Direction expected) {
    assertEquals(
        expected,
        DecorBlocks.get(id).getDefaultState().rotate(rotation).get(FurnitureBlock.FACING));
  }

  static Stream<Arguments> mirrors() {
    return Stream.of(FURNITURE)
        .flatMap(
            id ->
                Stream.of(
                    Arguments.of(id, BlockMirror.NONE, Direction.NORTH),
                    Arguments.of(id, BlockMirror.LEFT_RIGHT, Direction.SOUTH),
                    Arguments.of(id, BlockMirror.FRONT_BACK, Direction.NORTH)));
  }

  @ParameterizedTest(name = "{0} under {1} faces {2}")
  @MethodSource("mirrors")
  void mirrorsWithTheStructureItIsPlacedIn(
      final String id, final BlockMirror mirror, final Direction expected) {
    assertEquals(
        expected, DecorBlocks.get(id).getDefaultState().mirror(mirror).get(FurnitureBlock.FACING));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {"candelabra", "chandelier", "burgundy_velvet_drape", "emerald_velvet_drape"})
  void drawsThroughTheAnimatedFurnitureEntity(final String id) {
    final FurnitureBlock block = (FurnitureBlock) DecorBlocks.get(id);
    final BlockState state = block.getDefaultState();
    assertEquals(BlockRenderType.ENTITYBLOCK_ANIMATED, state.getRenderType());
    assertInstanceOf(FurnitureBlockEntity.class, block.createBlockEntity(BlockPos.ORIGIN, state));
  }
}
