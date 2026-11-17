package com.grahambartley.nightborn.landing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class LandingHazardsTest {

  private static final BlockState STONE = Blocks.STONE.getDefaultState();
  private static final BlockState AIR = Blocks.AIR.getDefaultState();

  private static BlockState campfire(final boolean soul, final boolean lit) {
    return (soul ? Blocks.SOUL_CAMPFIRE : Blocks.CAMPFIRE)
        .getDefaultState()
        .with(CampfireBlock.LIT, lit);
  }

  static Stream<Arguments> floors() {
    return Stream.of(
        Arguments.of("stone", STONE, false),
        Arguments.of("magma", Blocks.MAGMA_BLOCK.getDefaultState(), true),
        Arguments.of("lit campfire", campfire(false, true), true),
        Arguments.of("lit soul campfire", campfire(true, true), true),
        Arguments.of("unlit campfire", campfire(false, false), false),
        Arguments.of("unlit soul campfire", campfire(true, false), false),
        Arguments.of("netherrack", Blocks.NETHERRACK.getDefaultState(), false),
        Arguments.of("glass", Blocks.GLASS.getDefaultState(), false));
  }

  @ParameterizedTest(name = "{0}: {2}")
  @MethodSource("floors")
  @DisplayName("a magma or burning floor is no place to land")
  void floor(final String name, final BlockState floor, final boolean burns) {
    assertEquals(burns, LandingHazards.burns(floor, Stream.of(AIR, AIR)));
  }

  static Stream<Arguments> bodies() {
    return Stream.of(
        Arguments.of("air", AIR, false),
        Arguments.of("fire", Blocks.FIRE.getDefaultState(), true),
        Arguments.of("soul fire", Blocks.SOUL_FIRE.getDefaultState(), true),
        Arguments.of("lit campfire", campfire(false, true), true),
        Arguments.of("unlit campfire", campfire(false, false), false),
        Arguments.of("torch", Blocks.TORCH.getDefaultState(), false),
        Arguments.of("magma beside the body", Blocks.MAGMA_BLOCK.getDefaultState(), false),
        Arguments.of("tall grass", Blocks.SHORT_GRASS.getDefaultState(), false));
  }

  @ParameterizedTest(name = "{0}: {2}")
  @MethodSource("bodies")
  @DisplayName("a spot where the body would stand in flames is no place to land")
  void body(final String name, final BlockState inBody, final boolean burns) {
    assertEquals(burns, LandingHazards.burns(STONE, Stream.of(AIR, inBody)));
  }
}
