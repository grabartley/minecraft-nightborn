package com.grahambartley.nightborn.guild.rite;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.structure.Threshold;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class RiteLandingTest {

  private static final BlockPos FLOOR = new BlockPos(100, 63, -40);
  private static final Threshold DOOR = new Threshold(FLOOR, Direction.EAST);

  private static BlockPos off(final int out, final int up, final int side) {
    return FLOOR.add(out, up, side);
  }

  static Stream<Arguments> choices() {
    return Stream.of(
        Arguments.of("the step before the door", Set.of(off(1, 1, 0), off(2, 1, 0)), 1, 1, 0),
        Arguments.of("the next spot out when the step is blocked", Set.of(off(2, 1, 0)), 2, 1, 0),
        Arguments.of(
            "beside the step before further out", Set.of(off(1, 1, 1), off(3, 1, 0)), 1, 1, 1),
        Arguments.of("a step down outside", Set.of(off(1, -1, 0), off(4, 1, 3)), 1, -1, 0),
        Arguments.of("never the doorway itself", Set.of(off(0, 1, 0), off(3, 1, 2)), 3, 1, 2),
        Arguments.of("never inside, however near", Set.of(off(-1, 1, 0), off(4, 3, 3)), 4, 3, 3),
        Arguments.of("the same pick among equals", Set.of(off(1, 1, 1), off(1, 1, -1)), 1, 1, -1));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("choices")
  @DisplayName("the hunter lands on the safe spot nearest the step outside the door")
  void choose(final String name, final Set<BlockPos> safe, final int x, final int y, final int z) {
    assertEquals(Optional.of(off(x, y, z)), RiteLanding.choose(DOOR, 3, 2, safe::contains), name);
  }

  static Stream<Arguments> misses() {
    return Stream.of(
        Arguments.of("nothing safe", Set.<BlockPos>of()),
        Arguments.of(
            "only the doorway and the hall behind it",
            Set.of(off(0, 1, 0), off(-1, 1, 0), off(-2, 1, 1))),
        Arguments.of("too far out", Set.of(off(5, 1, 0), off(1, 1, -4))),
        Arguments.of("too far above or below", Set.of(off(1, 4, 0), off(1, -2, 0))));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("misses")
  @DisplayName("with no safe spot outside the door nobody is moved")
  void none(final String name, final Set<BlockPos> safe) {
    assertEquals(Optional.empty(), RiteLanding.choose(DOOR, 3, 2, safe::contains), name);
  }

  @ParameterizedTest(name = "a door facing {0}: ({1}, {2}) is outside {3}")
  @CsvSource({
    "EAST, 1, 0, true", "EAST, 0, 0, false", "EAST, -1, 0, false", "EAST, 0, 3, false",
    "WEST, -1, 0, true", "WEST, 1, 0, false", "NORTH, 0, -1, true", "NORTH, 0, 1, false",
    "SOUTH, 2, 1, true", "SOUTH, 2, 0, false", "UP, 1, 1, false", "DOWN, 0, 0, false"
  })
  @DisplayName("outside is at least one block past the doorway, the way the door faces")
  void outside(final Direction out, final int x, final int z, final boolean outside) {
    assertEquals(outside, RiteLanding.outside(new Threshold(FLOOR, out), FLOOR.add(x, 1, z)));
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
