package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.Optional;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.gen.chunk.placement.SpreadType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class SpreadStartTest {

  private static final long SEED = 12345L;
  private static final RandomSpreadStructurePlacement SPREAD =
      new RandomSpreadStructurePlacement(32, 8, SpreadType.LINEAR, 7);

  @ParameterizedTest(name = "the start for chunk {0},{1} offset by {2},{3} is found: {4}")
  @CsvSource({
    "0, 0, 0, 0, true",
    "40, -70, 0, 0, true",
    "-300, 512, 0, 0, true",
    "0, 0, 1, 0, false",
    "40, -70, 0, 5, false",
    "-300, 512, 16, 16, false"
  })
  @DisplayName("a locate point leads back to its start chunk, and any other point to none")
  void leadsBackToItsStart(
      final int chunkX, final int chunkZ, final int dx, final int dz, final boolean found) {
    final ChunkPos start = SPREAD.getStartChunk(SEED, chunkX, chunkZ);
    final BlockPos locate = SPREAD.getLocatePos(start).add(dx, 0, dz);
    assertEquals(
        found ? Optional.of(start) : Optional.empty(), SpreadStart.behind(SPREAD, SEED, locate));
  }
}
