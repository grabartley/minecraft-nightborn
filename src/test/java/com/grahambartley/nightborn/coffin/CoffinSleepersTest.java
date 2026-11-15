package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
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
