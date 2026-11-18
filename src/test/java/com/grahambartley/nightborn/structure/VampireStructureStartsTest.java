package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class VampireStructureStartsTest {

  private static final List<BlockBox> PIECES =
      List.of(new BlockBox(0, 0, 0, 9, 9, 9), new BlockBox(20, 0, 0, 29, 9, 9));

  @ParameterizedTest(name = "({0}, {1}, {2}): inside {3}")
  @CsvSource({
    "5, 5, 5, true",
    "0, 0, 0, true",
    "9, 9, 9, true",
    "25, 4, 4, true",
    "15, 4, 4, false",
    "5, 10, 5, false",
    "-1, 5, 5, false"
  })
  @DisplayName("a spot counts as inside a structure only within one of its pieces")
  void holds(final int x, final int y, final int z, final boolean inside) {
    assertEquals(inside, VampireStructureStarts.anyHolds(PIECES, new BlockPos(x, y, z)));
  }

  static Stream<Arguments> boxes() {
    return Stream.of(
        Arguments.of(new BlockBox(0, 0, 0, 15, 9, 15), List.of(new ChunkPos(0, 0))),
        Arguments.of(
            new BlockBox(10, 0, 0, 20, 9, 5), List.of(new ChunkPos(0, 0), new ChunkPos(1, 0))),
        Arguments.of(
            new BlockBox(-1, 0, -1, 0, 9, 0),
            List.of(
                new ChunkPos(-1, -1),
                new ChunkPos(-1, 0),
                new ChunkPos(0, -1),
                new ChunkPos(0, 0))),
        Arguments.of(new BlockBox(40, 0, 40, 40, 0, 40), List.of(new ChunkPos(2, 2))));
  }

  @ParameterizedTest(name = "{0} spans {1}")
  @MethodSource("boxes")
  @DisplayName("a box lists every chunk it reaches into")
  void chunksUnder(final BlockBox box, final List<ChunkPos> chunks) {
    assertEquals(chunks, VampireStructureStarts.chunksUnder(box));
  }
}
