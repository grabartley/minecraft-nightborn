package com.grahambartley.nightborn.mob.household;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FurniturePickTest {

  private static final BlockPos NEAR = new BlockPos(1, 0, 0);
  private static final BlockPos MIDDLE = new BlockPos(4, 0, 0);
  private static final BlockPos FAR = new BlockPos(9, 0, 0);
  private static final List<BlockPos> SPOTS = List.of(FAR, NEAR, MIDDLE);

  static Stream<Arguments> nearest() {
    return Stream.of(
        Arguments.of(Set.of(), NEAR),
        Arguments.of(Set.of(NEAR), MIDDLE),
        Arguments.of(Set.of(NEAR, MIDDLE), FAR),
        Arguments.of(Set.of(NEAR, MIDDLE, FAR), null));
  }

  @ParameterizedTest(name = "skipping {0}: {1}")
  @MethodSource("nearest")
  @DisplayName("a chore goes to the nearest furniture that still needs it")
  void nearest(final Set<BlockPos> done, final BlockPos expected) {
    assertEquals(
        Optional.ofNullable(expected),
        FurniturePick.nearest(SPOTS, Vec3d.ZERO, spot -> !done.contains(spot)));
  }

  static Stream<Arguments> any() {
    return Stream.of(
        Arguments.of(Set.of(), 0, FAR),
        Arguments.of(Set.of(), 1, NEAR),
        Arguments.of(Set.of(), 5, MIDDLE),
        Arguments.of(Set.of(FAR), 0, NEAR),
        Arguments.of(Set.of(FAR, NEAR, MIDDLE), 3, null));
  }

  @ParameterizedTest(name = "taken {0}, roll {1}: {2}")
  @MethodSource("any")
  @DisplayName("a hobby picks any free spot, so housemates spread around the house")
  void any(final Set<BlockPos> taken, final int roll, final BlockPos expected) {
    assertEquals(
        Optional.ofNullable(expected),
        FurniturePick.any(SPOTS, spot -> !taken.contains(spot), roll));
  }
}
