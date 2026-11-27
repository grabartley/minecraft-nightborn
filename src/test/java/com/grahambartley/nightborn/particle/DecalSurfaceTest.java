package com.grahambartley.nightborn.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DecalSurfaceTest {

  private static final BlockPos BLOCK = new BlockPos(4, 63, 9);

  @ParameterizedTest(name = "{0} with {1} beside, {2} face at ({3}, {4}, {5}): {6}")
  @CsvSource({
    "STONE, AIR, UP, 4.5, 64.0, 9.5, true",
    "STONE, AIR, WEST, 4.0, 63.5, 9.5, true",
    "STONE, AIR, EAST, 5.0, 63.5, 9.5, true",
    "STONE, AIR, NORTH, 4.5, 63.5, 9.0, true",
    "STONE, AIR, SOUTH, 4.5, 63.5, 10.0, true",
    "STONE, AIR, DOWN, 4.5, 63.0, 9.5, false",
    "STONE, AIR, UP, 4.5, 64.05, 9.5, false",
    "STONE, AIR, WEST, 3.95, 63.5, 9.5, false",
    "STONE, WATER, UP, 4.5, 64.0, 9.5, false",
    "STONE, LAVA, UP, 4.5, 64.0, 9.5, false",
    "STONE, STONE, UP, 4.5, 64.0, 9.5, false",
    "STONE, CARPET, UP, 4.5, 64.0, 9.5, false",
    "STONE, FLOWER_POT, UP, 4.5, 64.0, 9.5, false",
    "STONE, TORCH, UP, 4.5, 64.0, 9.5, true",
    "CARPET, AIR, UP, 4.5, 63.0625, 9.5, true",
    "BOTTOM_SLAB, AIR, UP, 4.5, 63.5, 9.5, true",
    "BOTTOM_SLAB, AIR, WEST, 4.0, 63.25, 9.5, false",
    "WET_SLAB, AIR, UP, 4.5, 63.5, 9.5, false",
    "WET_STAIRS, AIR, NORTH, 4.5, 63.5, 9.0, false",
    "STAIRS, AIR, NORTH, 4.5, 63.5, 9.0, true",
    "STAIRS, AIR, SOUTH, 4.5, 63.25, 10.0, false",
    "STAIRS, AIR, UP, 4.5, 63.5, 9.75, false",
    "STAIRS, AIR, UP, 4.5, 64.0, 9.25, false",
    "OAK_FENCE, AIR, UP, 4.5, 64.5, 9.5, false",
    "OAK_FENCE, AIR, WEST, 4.375, 63.5, 9.5, false",
    "GLASS_PANE, AIR, UP, 4.5, 64.0, 9.5, false",
    "OAK_LEAVES, AIR, UP, 4.5, 64.0, 9.5, true",
    "TORCH, AIR, UP, 4.5, 63.0, 9.5, false",
    "FLOWER_POT, AIR, UP, 4.5, 63.375, 9.5, false",
    "GLASS_PANE, AIR, WEST, 4.4375, 63.5, 9.5, false",
    "AIR, AIR, UP, 4.5, 64.0, 9.5, false",
    "WATER, AIR, UP, 4.5, 64.0, 9.5, false"
  })
  @DisplayName("blood holds only on a full, dry, uncovered face it rests exactly on")
  void holdsAt(
      final String block,
      final String beside,
      final Direction face,
      final double x,
      final double y,
      final double z,
      final boolean holds) {
    final Map<BlockPos, BlockState> blocks = new HashMap<>();
    blocks.put(BLOCK, state(block));
    blocks.put(BLOCK.offset(face), state(beside));

    assertEquals(holds, DecalSurface.holdsAt(worldOf(blocks), new Vec3d(x, y, z), face));
  }

  @ParameterizedTest(name = "centre x {0}, half extent {1}, {2} wide ledge: {3}")
  @CsvSource({
    "4.5, 0.4, 1, true",
    "4.5, 0.6, 1, false",
    "4.9, 0.2, 1, false",
    "4.9, 0.2, 2, true",
    "4.9, 1.2, 2, false",
    "5.0, 0.45, 2, true"
  })
  @DisplayName("a splat holds only when every face it covers holds blood")
  void holds(final double x, final double halfExtent, final int width, final boolean holds) {
    assertEquals(
        holds, DecalSurface.holds(ledge(width), new Vec3d(x, 64.0, 9.5), Direction.UP, halfExtent));
  }

  @ParameterizedTest(name = "centre x {0}, half extent {1}: {2}")
  @CsvSource({"4.5, 0.4, 0.4", "4.5, 0.6, 0.5", "4.9, 0.2, 0.1", "4.9, 0.6, ", "3.5, 0.2, "})
  @DisplayName("a splat that would hang over an edge shrinks to fit its own face, or is dropped")
  void fit(final double x, final double halfExtent, final Double fitted) {
    final OptionalDouble fit =
        DecalSurface.fit(ledge(1), new Vec3d(x, 64.0, 9.5), Direction.UP, halfExtent);

    assertEquals(fitted == null, fit.isEmpty());
    if (fitted != null) {
      assertEquals(fitted, fit.getAsDouble(), 1.0e-9);
    }
  }

  @ParameterizedTest(name = "half size {0} spun {1} rad at x {2}: {3}")
  @CsvSource({
    "0.2, 0.0, 4.5, 0.2",
    "0.4, 0.7853982, 4.5, 0.35355339",
    "0.2, 0.0, 4.9, 0.1",
    "0.3, 0.0, 4.9, ",
    "0.3, 0.0, 3.5, "
  })
  @DisplayName("a spun splat keeps its size when it fits, and shrinks to its own face when not")
  void fittedHalfSize(
      final double halfSize, final float spin, final double x, final Double fitted) {
    final OptionalDouble fit =
        DecalSurface.fittedHalfSize(
            ledge(1), new Vec3d(x, 64.0, 9.5), Direction.UP, halfSize, spin);

    assertEquals(fitted == null, fit.isEmpty());
    if (fitted != null) {
      assertEquals(fitted, fit.getAsDouble(), 1.0e-6);
    }
  }

  @ParameterizedTest(name = "half size {0} spun {1} rad: {2}")
  @CsvSource({
    "0.5, 0.0, 0.5",
    "0.5, 1.5707964, 0.5",
    "0.5, 0.7853982, 0.70710678",
    "0.2, 3.1415927, 0.2"
  })
  @DisplayName("a spun splat reaches further along the face than its half size")
  void halfExtent(final double halfSize, final float spin, final double extent) {
    assertEquals(extent, DecalSurface.halfExtent(halfSize, spin), 1.0e-6);
  }

  @ParameterizedTest(name = "{0} along {1}: {2}")
  @CsvSource({
    "4.5, 64.0, 9.5, Y, 0.5",
    "4.1, 64.0, 9.5, Y, 0.1",
    "4.5, 64.0, 9.8, Y, 0.2",
    "4.0, 63.3, 9.5, X, 0.3"
  })
  @DisplayName("the room a splat has is the distance to the nearest edge of its face")
  void roomInCell(
      final double x,
      final double y,
      final double z,
      final Direction.Axis normal,
      final double room) {
    assertEquals(room, DecalSurface.roomInCell(new Vec3d(x, y, z), normal), 1.0e-9);
  }

  @ParameterizedTest(name = "{0} under feet at y {1}: {2}")
  @CsvSource({
    "STONE, AIR, 64.0, 64.0",
    "STONE, AIR, 66.5, 64.0",
    "STONE, AIR, 67.5, ",
    "BOTTOM_SLAB, AIR, 63.5, 63.5",
    "OAK_FENCE, AIR, 64.5, ",
    "STONE, WATER, 64.0, ",
    "AIR, AIR, 64.0, "
  })
  @DisplayName("a pool forms on the first floor under the feet, and only if it holds blood")
  void floorBelow(final String block, final String above, final double feetY, final Double floorY) {
    final Map<BlockPos, BlockState> blocks = new HashMap<>();
    blocks.put(BLOCK, state(block));
    blocks.put(BLOCK.up(), state(above));

    assertEquals(
        Optional.ofNullable(floorY).map(y -> new Vec3d(4.5, y, 9.5)),
        DecalSurface.floorBelow(worldOf(blocks), new Vec3d(4.5, feetY, 9.5), 2));
  }

  private static BlockView ledge(final int width) {
    final Map<BlockPos, BlockState> blocks = new HashMap<>();
    for (int i = 0; i < width; i++) {
      blocks.put(BLOCK.east(i), Blocks.STONE.getDefaultState());
    }
    return worldOf(blocks);
  }

  private static BlockView worldOf(final Map<BlockPos, BlockState> blocks) {
    final BlockView world = mock(BlockView.class);
    when(world.getBlockState(any()))
        .thenAnswer(call -> blocks.getOrDefault(call.getArgument(0), Blocks.AIR.getDefaultState()));
    when(world.getFluidState(any()))
        .thenAnswer(call -> world.getBlockState(call.getArgument(0)).getFluidState());
    return world;
  }

  private static BlockState state(final String name) {
    final BlockState stairs =
        Blocks.STONE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH);
    final BlockState slab =
        Blocks.STONE_SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.BOTTOM);
    return switch (name) {
      case "STONE" -> Blocks.STONE.getDefaultState();
      case "WATER" -> Blocks.WATER.getDefaultState();
      case "LAVA" -> Blocks.LAVA.getDefaultState();
      case "CARPET" -> Blocks.RED_CARPET.getDefaultState();
      case "FLOWER_POT" -> Blocks.FLOWER_POT.getDefaultState();
      case "TORCH" -> Blocks.TORCH.getDefaultState();
      case "BOTTOM_SLAB" -> slab;
      case "WET_SLAB" -> slab.with(Properties.WATERLOGGED, true);
      case "STAIRS" -> stairs;
      case "WET_STAIRS" -> stairs.with(Properties.WATERLOGGED, true);
      case "OAK_FENCE" -> Blocks.OAK_FENCE.getDefaultState();
      case "GLASS_PANE" -> Blocks.GLASS_PANE.getDefaultState();
      case "OAK_LEAVES" -> Blocks.OAK_LEAVES.getDefaultState();
      default -> Blocks.AIR.getDefaultState();
    };
  }
}
