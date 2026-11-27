package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class StructureEntranceTest {

  private static final BlockPos LOCATE = new BlockPos(16, 0, 32);
  private static final BlockPos PLANNED = new BlockPos(40, -22, 7);

  @ParameterizedTest(name = "a threshold at {0},{1},{2} facing {3} is stood on at {4},{5},{6}")
  @CsvSource({
    "3, 64, 9, SOUTH, 3, 65, 10",
    "3, 64, 9, NORTH, 3, 65, 8",
    "3, 64, 9, EAST, 4, 65, 9",
    "3, 64, 9, WEST, 2, 65, 9",
    "-120, -23, 450, SOUTH, -120, -22, 451",
    "77, 180, -5, WEST, 76, 181, -5"
  })
  @DisplayName("the standing spot is one step out of the threshold, with feet just above its floor")
  void standsOneStepOutside(
      final int x,
      final int y,
      final int z,
      final Direction out,
      final int feetX,
      final int feetY,
      final int feetZ) {
    assertEquals(
        new BlockPos(feetX, feetY, feetZ),
        StructureEntrance.standingSpot(new Threshold(new BlockPos(x, y, z), out)));
  }

  static Stream<Arguments> findings() {
    return Stream.of(
        Arguments.of(Optional.of(false), Optional.of(PLANNED), Optional.empty(), false),
        Arguments.of(Optional.of(true), Optional.of(PLANNED), Optional.of(PLANNED), true),
        Arguments.of(Optional.of(true), Optional.empty(), Optional.of(LOCATE), true),
        Arguments.of(Optional.empty(), Optional.of(PLANNED), Optional.of(PLANNED), true),
        Arguments.of(Optional.empty(), Optional.empty(), Optional.empty(), true));
  }

  @ParameterizedTest(name = "saved {0}, planned {1} -> {2}, planned it {3}")
  @MethodSource("findings")
  @DisplayName(
      "a saved start is shown at its planned entrance or else its locate point, an unsaved one only"
          + " where it can be planned, and a chunk saved without it is never planned")
  void found(
      final Optional<Boolean> saved,
      final Optional<BlockPos> planned,
      final Optional<BlockPos> expected,
      final boolean planningRuns) {
    final AtomicBoolean ran = new AtomicBoolean();
    assertEquals(
        expected,
        StructureEntrance.found(
            saved,
            () -> {
              ran.set(true);
              return planned;
            },
            LOCATE));
    assertEquals(planningRuns, ran.get());
  }
}
