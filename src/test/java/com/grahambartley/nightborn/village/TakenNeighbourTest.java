package com.grahambartley.nightborn.village;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import net.minecraft.nbt.NbtOps;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TakenNeighbourTest {

  @ParameterizedTest(name = "pair {0} survives a save and load")
  @ValueSource(ints = {1, 2, 3})
  void survivesASaveAndLoad(final int pair) {
    final TakenNeighbour taken = new TakenNeighbour(UUID.randomUUID(), UUID.randomUUID());
    assertEquals(
        taken,
        TakenNeighbour.CODEC
            .parse(
                NbtOps.INSTANCE,
                TakenNeighbour.CODEC.encodeStart(NbtOps.INSTANCE, taken).getOrThrow())
            .getOrThrow());
  }
}
