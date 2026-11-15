package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class CoffinSleepersTest {

  static Stream<Arguments> states() {
    final BlockState coffin = Coffins.get(CoffinWood.OAK).getDefaultState();
    return Stream.of(
        Arguments.of(coffin.with(CoffinBlock.OPEN, false), true, true),
        Arguments.of(coffin.with(CoffinBlock.OPEN, true), true, false),
        Arguments.of(Blocks.RED_BED.getDefaultState(), false, false),
        Arguments.of(Blocks.STONE.getDefaultState(), false, false));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("states")
  @DisplayName("only a coffin counts as one, and only a shut one shelters its sleeper")
  void knowsAShutCoffin(final BlockState state, final boolean coffin, final boolean shelters) {
    assertEquals(coffin, CoffinSleepers.isCoffin(single(state), BlockPos.ORIGIN));
    assertEquals(shelters, CoffinSleepers.shelters(state));
  }

  private static final BlockPos HEAD = new BlockPos(3, 70, 3);

  static Stream<Arguments> restingSpots() {
    return Direction.Type.HORIZONTAL.stream()
        .flatMap(facing -> Stream.of(true, false).map(standing -> Arguments.of(facing, standing)));
  }

  @ParameterizedTest(name = "facing {0}, standing {1}")
  @MethodSource("restingSpots")
  @DisplayName("a coffin gives its sleeper the resting spot its layout sets, anything else none")
  void restsWhereTheLayoutSays(final Direction facing, final boolean standing) {
    final BlockState coffin =
        Coffins.get(CoffinWood.OAK)
            .getDefaultState()
            .with(BedBlock.FACING, facing)
            .with(CoffinBlock.STANDING, standing);
    assertEquals(
        Optional.of(CoffinLayout.restingSpot(HEAD, facing, standing)),
        CoffinSleepers.restingSpot(world(Map.of(HEAD, coffin)), HEAD));
    assertEquals(Optional.empty(), CoffinSleepers.restingSpot(world(Map.of()), HEAD));
  }

  static Stream<Arguments> fronts() {
    return Direction.Type.HORIZONTAL.stream()
        .flatMap(
            facing ->
                Stream.of(
                    Arguments.of(facing, true, 0, false),
                    Arguments.of(facing, true, 1, true),
                    Arguments.of(facing, true, 2, true),
                    Arguments.of(facing, true, 3, false),
                    Arguments.of(facing, false, 1, false)));
  }

  @ParameterizedTest(name = "facing {0}, standing {1}, wall case {2}: blocked {3}")
  @MethodSource("fronts")
  @DisplayName("a standing coffin is blocked only by a solid block where the lid opens")
  void needsRoomInFront(
      final Direction facing, final boolean standing, final int wall, final boolean blocked) {
    final BlockState coffin =
        Coffins.get(CoffinWood.OAK)
            .getDefaultState()
            .with(BedBlock.FACING, facing)
            .with(CoffinBlock.STANDING, standing);
    final Map<BlockPos, BlockState> blocks = new HashMap<>();
    blocks.put(HEAD, coffin);
    final Direction out = facing.getOpposite();
    switch (wall) {
      case 1 -> blocks.put(HEAD.offset(out), Blocks.STONE.getDefaultState());
      case 2 -> blocks.put(HEAD.down().offset(out), Blocks.STONE.getDefaultState());
      case 3 -> {
        blocks.put(HEAD.offset(facing), Blocks.STONE.getDefaultState());
        blocks.put(HEAD.up(), Blocks.STONE.getDefaultState());
        blocks.put(HEAD.offset(out), Blocks.GLASS.getDefaultState());
      }
      default -> {}
    }
    assertEquals(blocked, CoffinSleepers.blockedInFront(world(blocks), HEAD));
  }

  private static BlockView world(final Map<BlockPos, BlockState> blocks) {
    return new BlockView() {
      @Override
      public BlockEntity getBlockEntity(final BlockPos pos) {
        return null;
      }

      @Override
      public BlockState getBlockState(final BlockPos pos) {
        return blocks.getOrDefault(pos, Blocks.AIR.getDefaultState());
      }

      @Override
      public FluidState getFluidState(final BlockPos pos) {
        return this.getBlockState(pos).getFluidState();
      }

      @Override
      public int getHeight() {
        return 384;
      }

      @Override
      public int getBottomY() {
        return -64;
      }
    };
  }

  private static BlockView single(final BlockState state) {
    return new BlockView() {
      @Override
      public BlockEntity getBlockEntity(final BlockPos pos) {
        return null;
      }

      @Override
      public BlockState getBlockState(final BlockPos pos) {
        return state;
      }

      @Override
      public FluidState getFluidState(final BlockPos pos) {
        return state.getFluidState();
      }

      @Override
      public int getHeight() {
        return 384;
      }

      @Override
      public int getBottomY() {
        return -64;
      }
    };
  }
}
