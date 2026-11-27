package com.grahambartley.nightborn.guild.rite;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class RiteLandingTest {

  private static final BlockPos ENTRANCE = new BlockPos(100, 64, -40);

  private static BlockPos off(final int x, final int y, final int z) {
    return ENTRANCE.add(x, y, z);
  }

  static Stream<Arguments> choices() {
    return Stream.of(
        Arguments.of(
            "the entrance itself when it is safe", Set.of(off(0, 0, 0), off(1, 0, 0)), 0, 0, 0),
        Arguments.of(
            "the nearest safe spot beside it", Set.of(off(1, 0, 0), off(3, 0, 0)), 1, 0, 0),
        Arguments.of(
            "a step up before a spot further off", Set.of(off(0, 1, 0), off(2, 0, 0)), 0, 1, 0),
        Arguments.of(
            "a step down before a spot further off", Set.of(off(0, -2, 0), off(3, 0, 3)), 0, -2, 0),
        Arguments.of("the far corner of the search", Set.of(off(3, 2, 3)), 3, 2, 3),
        Arguments.of(
            "the same pick among equals",
            Set.of(off(1, 0, 0), off(-1, 0, 0), off(0, 0, 1)),
            -1,
            0,
            0));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("choices")
  @DisplayName("the hunter lands on the safe spot nearest the entrance")
  void choose(final String name, final Set<BlockPos> safe, final int x, final int y, final int z) {
    assertEquals(
        Optional.of(off(x, y, z)), RiteLanding.choose(ENTRANCE, 3, 2, safe::contains), name);
  }

  static Stream<Arguments> misses() {
    return Stream.of(
        Arguments.of("nothing safe", Set.<BlockPos>of()),
        Arguments.of("too far to the side", Set.of(off(4, 0, 0), off(0, 0, -4))),
        Arguments.of("too far above or below", Set.of(off(0, 3, 0), off(0, -3, 0))));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("misses")
  @DisplayName("with no safe spot in reach of the entrance nobody is moved")
  void none(final String name, final Set<BlockPos> safe) {
    assertEquals(Optional.empty(), RiteLanding.choose(ENTRANCE, 3, 2, safe::contains), name);
  }

  static Stream<Arguments> spots() {
    final BlockState stone = Blocks.STONE.getDefaultState();
    final BlockState air = Blocks.AIR.getDefaultState();
    final BlockState water = Blocks.WATER.getDefaultState();
    final BlockState lava = Blocks.LAVA.getDefaultState();
    final BlockState magma = Blocks.MAGMA_BLOCK.getDefaultState();
    final BlockState berries = Blocks.SWEET_BERRY_BUSH.getDefaultState();
    return Stream.of(
        Arguments.of("solid floor and two blocks of air", stone, air, air, true),
        Arguments.of("over a drop", air, air, air, false),
        Arguments.of("in a wall at the feet", stone, stone, air, false),
        Arguments.of("in a wall at the head", stone, air, stone, false),
        Arguments.of("standing in water", stone, water, air, false),
        Arguments.of("head in water", stone, air, water, false),
        Arguments.of("standing in lava", stone, lava, air, false),
        Arguments.of("on water", water, air, air, false),
        Arguments.of("on lava", lava, air, air, false),
        Arguments.of("on a floor that harms", magma, air, air, false),
        Arguments.of("in a bush that harms", stone, berries, air, false),
        Arguments.of("under a bush that harms", stone, air, berries, false));
  }

  @ParameterizedTest(name = "{0} -> {4}")
  @MethodSource("spots")
  @DisplayName("a safe spot is solid floor under two open blocks with nothing that harms")
  void stands(
      final String name,
      final BlockState floor,
      final BlockState feet,
      final BlockState head,
      final boolean stands) {
    assertEquals(
        stands,
        RiteLanding.stands(
            floor,
            feet,
            head,
            state -> state.isOf(Blocks.MAGMA_BLOCK) || state.isOf(Blocks.SWEET_BERRY_BUSH)),
        name);
  }
}
