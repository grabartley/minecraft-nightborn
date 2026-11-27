package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StructureEntranceTest {

  private static final BlockPos LOCATE = new BlockPos(16, 0, 32);
  private static final BlockPos PLANNED = new BlockPos(40, -22, 7);

  private static final BlockPos BUILT = new BlockPos(41, -22, 8);

  static Stream<Arguments> findings() {
    return Stream.of(
        Arguments.of(
            Optional.of(false), Optional.of(BUILT), Optional.of(PLANNED), Optional.empty(), "none"),
        Arguments.of(
            Optional.of(true),
            Optional.of(BUILT),
            Optional.of(PLANNED),
            Optional.of(BUILT),
            "built"),
        Arguments.of(
            Optional.of(true),
            Optional.empty(),
            Optional.of(PLANNED),
            Optional.of(PLANNED),
            "built planned"),
        Arguments.of(
            Optional.of(true),
            Optional.empty(),
            Optional.empty(),
            Optional.of(LOCATE),
            "built planned"),
        Arguments.of(
            Optional.empty(),
            Optional.of(BUILT),
            Optional.of(PLANNED),
            Optional.of(PLANNED),
            "planned"),
        Arguments.of(
            Optional.empty(), Optional.of(BUILT), Optional.empty(), Optional.empty(), "planned"));
  }

  @ParameterizedTest(name = "saved {0}, built {1}, planned {2} -> {3}, read {4}")
  @MethodSource("findings")
  @DisplayName(
      "a saved start is shown where it was built, else where it plans, else at its locate point;"
          + " an unsaved one only where it plans, and a chunk saved without it is never read")
  void found(
      final Optional<Boolean> saved,
      final Optional<BlockPos> built,
      final Optional<BlockPos> planned,
      final Optional<BlockPos> expected,
      final String read) {
    final List<String> asked = new ArrayList<>();
    assertEquals(
        expected,
        StructureEntrance.found(
            saved,
            () -> {
              asked.add("built");
              return built;
            },
            () -> {
              asked.add("planned");
              return planned;
            },
            LOCATE));
    assertEquals(read, asked.isEmpty() ? "none" : String.join(" ", asked));
  }
}
