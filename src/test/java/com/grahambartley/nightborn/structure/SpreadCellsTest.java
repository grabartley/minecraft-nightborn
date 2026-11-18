package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SpreadCellsTest {

  @ParameterizedTest(name = "{0} blocks -> {1} chunks")
  @CsvSource({"0, 1", "1, 1", "16, 1", "17, 2", "256, 16", "1024, 64"})
  @DisplayName("a range in blocks searches enough chunks to cover it, and never none")
  void chunks(final int blocks, final int chunks) {
    assertEquals(chunks, SpreadCells.chunks(blocks));
  }

  @ParameterizedTest(name = "centre ({0}, {1}), {2} chunks, spacing {3}")
  @CsvSource({
    "0, 0, 64, 48",
    "-3, 7, 64, 48",
    "47, -48, 64, 48",
    "100, 100, 64, 34",
    "-200, 5, 256, 48",
    "0, 0, 1, 48",
    "12, -9, 64, 1"
  })
  @DisplayName("every placement cell that overlaps the range is probed once, and no other")
  void probes(final int x, final int z, final int chunks, final int spacing) {
    final List<ChunkPos> probes = SpreadCells.probes(new ChunkPos(x, z), chunks, spacing);
    final Set<ChunkPos> regions =
        probes.stream()
            .map(
                probe ->
                    new ChunkPos(Math.floorDiv(probe.x, spacing), Math.floorDiv(probe.z, spacing)))
            .collect(Collectors.toSet());
    assertEquals(probes.size(), regions.size());
    final Set<ChunkPos> covered =
        IntStream.rangeClosed(x - chunks, x + chunks)
            .boxed()
            .flatMap(
                cx ->
                    IntStream.rangeClosed(z - chunks, z + chunks)
                        .mapToObj(
                            cz ->
                                new ChunkPos(
                                    Math.floorDiv(cx, spacing), Math.floorDiv(cz, spacing))))
            .collect(Collectors.toSet());
    assertEquals(covered, regions);
  }

  @ParameterizedTest(name = "({0}, {1}) within {2} -> {3}")
  @CsvSource({
    "600, 800, 1000, true",
    "600, 801, 1000, false",
    "-1024, 0, 1024, true",
    "-1025, 0, 1024, false",
    "0, 0, 0, true"
  })
  @DisplayName("a house counts as in range by flat distance, whatever its height")
  void within(final int x, final int z, final int blocks, final boolean within) {
    assertEquals(
        within, SpreadCells.within(new BlockPos(0, 64, 0), new BlockPos(x, -40, z), blocks));
  }
}
