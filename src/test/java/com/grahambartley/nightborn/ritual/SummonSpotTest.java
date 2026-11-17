package com.grahambartley.nightborn.ritual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class SummonSpotTest {

  private static final BlockPos ALTAR = new BlockPos(10, 64, 10);

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "EAST", "SOUTH", "WEST"})
  @DisplayName("candidates ring the altar, the front and furthest first, never the altar itself")
  void candidates(final Direction facing) {
    final List<BlockPos> spots = SummonSpot.candidates(ALTAR, facing);
    assertEquals(16, spots.size());
    assertEquals(ALTAR.offset(facing, 2), spots.getFirst());
    assertEquals(16, Set.copyOf(spots).size());
    assertTrue(spots.stream().noneMatch(spot -> spot.getX() == 10 && spot.getZ() == 10));
  }

  static Stream<Arguments> picks() {
    final BlockPos front = ALTAR.offset(Direction.NORTH, 2);
    final BlockPos behindNear = ALTAR.offset(Direction.SOUTH, 1);
    final BlockPos stepDown = ALTAR.offset(Direction.EAST, 2).down();
    return Stream.of(
        Arguments.of("open ground", Set.of(front, behindNear, stepDown), front),
        Arguments.of("front blocked", Set.of(behindNear, stepDown), stepDown),
        Arguments.of("only behind", Set.of(behindNear), behindNear),
        Arguments.of("walled in", Set.of(), ALTAR.up()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("picks")
  @DisplayName("the Eldest takes the first spot it can stand on, or the altar top")
  void pick(final String name, final Set<BlockPos> open, final BlockPos picked) {
    assertEquals(picked, SummonSpot.pick(ALTAR, Direction.NORTH, open::contains));
  }
}
