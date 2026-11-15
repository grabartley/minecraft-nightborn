package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EndingEdgeTest {

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"ffff, ----", "tttt, ----", "ttff, --E-", "tftf, -E-E", "fttf, ---E", "ftft, --E-"})
  @DisplayName("the edge fires once, on the tick the state ends")
  void landed(final String active, final String expected) {
    final EndingEdge edge = new EndingEdge();
    final List<String> seen = new ArrayList<>();
    for (final char tick : active.toCharArray()) {
      seen.add(edge.ended(tick == 't') ? "E" : "-");
    }
    assertEquals(expected, String.join("", seen));
  }

  @ParameterizedTest(name = "ending pose {0}, finished {1}: still ending {2}")
  @CsvSource({
    "true, false, true",
    "true, true, false",
    "false, false, false",
    "false, true, false"
  })
  @DisplayName("the ending holds while its clip plays and clears once it ends or another pose wins")
  void latch(final boolean endingPose, final boolean finished, final boolean stillEnding) {
    final EndingEdge edge = new EndingEdge();
    edge.ended(true);
    edge.ended(false);
    assertEquals(true, edge.ending());
    edge.shown(endingPose, finished);
    assertEquals(stillEnding, edge.ending());
  }
}
