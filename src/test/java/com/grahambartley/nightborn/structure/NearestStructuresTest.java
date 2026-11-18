package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NearestStructuresTest {

  @ParameterizedTest(name = "{0} blocks -> {1} chunks")
  @CsvSource({"0, 1", "1, 1", "16, 1", "17, 2", "256, 16", "1024, 64"})
  @DisplayName("a range in blocks searches enough chunks to cover it, and never none")
  void chunks(final int blocks, final int chunks) {
    assertEquals(chunks, NearestStructures.chunks(blocks));
  }
}
