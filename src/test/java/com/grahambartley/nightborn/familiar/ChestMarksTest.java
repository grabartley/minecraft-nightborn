package com.grahambartley.nightborn.familiar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ChestMarksTest {

  private static final GlobalPos A = GlobalPos.create(World.OVERWORLD, new BlockPos(1, 64, 1));
  private static final GlobalPos B = GlobalPos.create(World.OVERWORLD, new BlockPos(5, 64, 1));
  private static final GlobalPos C = GlobalPos.create(World.NETHER, new BlockPos(1, 64, 1));

  static Stream<Arguments> marks() {
    return Stream.of(
        Arguments.of(ChestMarks.NONE, A, marks(A, null), ChestMark.FETCH),
        Arguments.of(marks(A, null), B, marks(A, B), ChestMark.DELIVER),
        Arguments.of(marks(A, null), A, ChestMarks.NONE, ChestMark.CLEARED),
        Arguments.of(marks(A, B), B, marks(A, null), ChestMark.CLEARED),
        Arguments.of(marks(A, B), A, marks(null, B), ChestMark.CLEARED),
        Arguments.of(marks(A, B), C, marks(A, C), ChestMark.DELIVER),
        Arguments.of(marks(null, B), A, marks(A, B), ChestMark.FETCH));
  }

  @ParameterizedTest(name = "{0} marking {1} gives {2}")
  @MethodSource("marks")
  @DisplayName("the first chest marked fetches, the next delivers, and a marked chest clears")
  void mark(
      final ChestMarks before,
      final GlobalPos chest,
      final ChestMarks after,
      final ChestMark mark) {
    assertEquals(new ChestMarks.Marked(after, mark), before.mark(chest));
  }

  static Stream<Arguments> completeness() {
    return Stream.of(
        Arguments.of(ChestMarks.NONE, false),
        Arguments.of(marks(A, null), false),
        Arguments.of(marks(null, B), false),
        Arguments.of(marks(A, B), true));
  }

  @ParameterizedTest(name = "{0}: complete {1}")
  @MethodSource("completeness")
  @DisplayName("an errand needs both a fetch and a deliver chest")
  void complete(final ChestMarks marks, final boolean complete) {
    assertEquals(complete, marks.complete());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("completeness")
  @DisplayName("marks survive a save and load")
  void codec(final ChestMarks marks, final boolean ignored) {
    assertEquals(
        marks,
        ChestMarks.CODEC
            .parse(
                NbtOps.INSTANCE, ChestMarks.CODEC.encodeStart(NbtOps.INSTANCE, marks).getOrThrow())
            .getOrThrow());
  }

  static Stream<Arguments> forgets() {
    return Stream.of(
        Arguments.of(marks(A, B), A, marks(null, B)),
        Arguments.of(marks(A, B), B, marks(A, null)),
        Arguments.of(marks(A, B), C, marks(A, B)),
        Arguments.of(ChestMarks.NONE, A, ChestMarks.NONE));
  }

  @ParameterizedTest(name = "{0} without {1} is {2}")
  @MethodSource("forgets")
  @DisplayName("a chest that stops being an errand chest is forgotten")
  void without(final ChestMarks before, final GlobalPos chest, final ChestMarks after) {
    assertEquals(after, before.without(chest));
  }

  private static ChestMarks marks(final GlobalPos fetch, final GlobalPos deliver) {
    return new ChestMarks(Optional.ofNullable(fetch), Optional.ofNullable(deliver));
  }
}
