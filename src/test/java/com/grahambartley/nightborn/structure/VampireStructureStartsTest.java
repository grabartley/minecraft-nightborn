package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
}
