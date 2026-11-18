package com.grahambartley.nightborn.servant.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class JobChestTest {

  @ParameterizedTest(name = "chest at ({0}, {1}, {2}), range {3} -> {4}")
  @CsvSource({
    "0, 0, 32, 32, true",
    "0, 0, 33, 32, false",
    "10, 10, 10, 32, true",
    "4, 0, 0, 4, true"
  })
  @DisplayName("a servant brings its work only to a chest within errand range of where it works")
  void inReach(final int x, final int y, final int z, final int range, final boolean reach) {
    assertEquals(reach, JobChest.inReach(BlockPos.ORIGIN, new BlockPos(x, y, z), range));
  }
}
