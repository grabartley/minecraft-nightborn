package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class CaveCellTest {

  static Stream<Arguments> states() {
    return Stream.of(
        Arguments.of(Blocks.DEEPSLATE.getDefaultState(), CaveCell.SOLID),
        Arguments.of(Blocks.STONE.getDefaultState(), CaveCell.SOLID),
        Arguments.of(Blocks.TUFF.getDefaultState(), CaveCell.SOLID),
        Arguments.of(Blocks.AIR.getDefaultState(), CaveCell.OPEN),
        Arguments.of(Blocks.CAVE_AIR.getDefaultState(), CaveCell.OPEN),
        Arguments.of(Blocks.WATER.getDefaultState(), CaveCell.FLUID),
        Arguments.of(Blocks.LAVA.getDefaultState(), CaveCell.FLUID),
        Arguments.of(
            Blocks.POINTED_DRIPSTONE.getDefaultState().with(Properties.WATERLOGGED, true),
            CaveCell.FLUID));
  }

  @ParameterizedTest(name = "{0} is {1}")
  @MethodSource("states")
  void sortsWhatTheNoiseLeavesInto(final BlockState state, final CaveCell cell) {
    assertEquals(cell, CaveCell.of(state));
  }
}
