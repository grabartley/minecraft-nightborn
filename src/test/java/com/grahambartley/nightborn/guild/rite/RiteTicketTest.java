package com.grahambartley.nightborn.guild.rite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RiteTicketTest {

  @ParameterizedTest(name = "a search of {2} about ({0}, {1}) spans {3} chunks")
  @CsvSource({
    "8, 8, 3, 1",
    "8, 8, 0, 1",
    "1, 8, 3, 2",
    "8, 14, 3, 2",
    "1, 1, 3, 4",
    "15, 15, 3, 4",
    "-1, -1, 3, 4",
    "-8, -8, 3, 1"
  })
  @DisplayName("every chunk the search for a safe spot can touch is checked before it runs")
  void span(final int x, final int z, final int radius, final int chunks) {
    final BlockPos at = new BlockPos(x, 64, z);
    final List<ChunkPos> span = RiteTicket.span(at, radius);
    assertEquals(chunks, span.size());
    assertTrue(span.contains(new ChunkPos(at)));
    assertTrue(span.contains(new ChunkPos(at.add(radius, 0, radius))));
    assertTrue(span.contains(new ChunkPos(at.add(-radius, 0, -radius))));
    assertTrue(RiteTicket.RADIUS >= 1 && RiteTicket.EXPIRY_TICKS > 1);
  }
}
