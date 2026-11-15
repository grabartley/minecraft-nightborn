package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class DecorVariantsTest {

  @ParameterizedTest(name = "({0}, {1}, {2}) of {3}")
  @CsvSource({
    "0, 64, 0, 3",
    "-17, 5, 200, 3",
    "30000000, -60, -30000000, 4",
    "1, 1, 1, 4",
    "12, 70, -9, 1"
  })
  void picksTheSameVariantInRangeForTheSameSpot(
      final int x, final int y, final int z, final int count) {
    final BlockPos pos = new BlockPos(x, y, z);
    final int picked = DecorVariants.pick(pos, count);
    assertTrue(picked >= 0 && picked < count, "picked " + picked);
    assertEquals(picked, DecorVariants.pick(new BlockPos(x, y, z), count));
  }

  @ParameterizedTest(name = "every one of {0} variants shows up along a wall")
  @ValueSource(ints = {2, 3, 4})
  void neighbouringSpotsUseEveryVariant(final int count) {
    final Set<Integer> seen = new HashSet<>();
    for (int x = 0; x < 32; x++) {
      seen.add(DecorVariants.pick(new BlockPos(x, 64, 7), count));
    }
    assertEquals(count, seen.size());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(ints = {0, -1})
  void refusesNoVariants(final int count) {
    assertThrows(IllegalArgumentException.class, () -> DecorVariants.pick(BlockPos.ORIGIN, count));
  }
}
