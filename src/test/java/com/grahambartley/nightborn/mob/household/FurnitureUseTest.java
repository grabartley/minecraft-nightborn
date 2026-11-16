package com.grahambartley.nightborn.mob.household;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.decor.DecorBlocks;
import com.grahambartley.nightborn.decor.DrapeBlock;
import com.grahambartley.nightborn.decor.LightableFurnitureBlock;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CandleBlock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class FurnitureUseTest {

  static Stream<Arguments> states() {
    return Stream.of(
        Arguments.of(Blocks.CANDLE.getDefaultState().with(CandleBlock.LIT, true), true, false),
        Arguments.of(Blocks.CANDLE.getDefaultState().with(CandleBlock.LIT, false), false, false),
        Arguments.of(
            DecorBlocks.CANDELABRA.getDefaultState().with(LightableFurnitureBlock.LIT, true),
            true,
            false),
        Arguments.of(
            DecorBlocks.BURGUNDY_VELVET_DRAPE.getDefaultState().with(DrapeBlock.OPEN, true),
            false,
            true),
        Arguments.of(
            DecorBlocks.BURGUNDY_VELVET_DRAPE.getDefaultState().with(DrapeBlock.OPEN, false),
            false,
            false),
        Arguments.of(Blocks.STONE.getDefaultState(), false, false));
  }

  static Stream<Arguments> lightable() {
    return Stream.of(
        Arguments.of(Blocks.CANDLE.getDefaultState().with(CandleBlock.LIT, true), false),
        Arguments.of(
            Blocks.CANDLE
                .getDefaultState()
                .with(CandleBlock.LIT, false)
                .with(CandleBlock.WATERLOGGED, true),
            false),
        Arguments.of(
            DecorBlocks.CANDELABRA.getDefaultState().with(LightableFurnitureBlock.LIT, false),
            true),
        Arguments.of(
            DecorBlocks.CANDELABRA.getDefaultState().with(LightableFurnitureBlock.LIT, true),
            false));
  }

  @ParameterizedTest(name = "{0}: {1}")
  @MethodSource("lightable")
  @DisplayName("a vampire lights only what flint and steel could light")
  void lightable(final BlockState state, final boolean lightable) {
    assertEquals(lightable, FurnitureUse.lightable(state));
  }

  @ParameterizedTest(name = "{0}: lit {1}, open {2}")
  @MethodSource("states")
  @DisplayName("a vampire reads whether a light burns and a drape is open from the block itself")
  void reads(final BlockState state, final boolean lit, final boolean open) {
    assertEquals(lit, FurnitureUse.lit(state));
    assertEquals(open, FurnitureUse.open(state));
  }
}
