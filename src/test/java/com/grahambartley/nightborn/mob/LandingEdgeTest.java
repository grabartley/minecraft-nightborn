package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LandingEdgeTest {

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"ffff, ----", "tttt, ----", "ttff, --L-", "tftf, -L-L", "fttf, ---L", "ftft, --L-"})
  @DisplayName("landing fires once, on the tick levitation ends")
  void landed(final String levitating, final String expected) {
    final LandingEdge edge = new LandingEdge();
    final List<String> seen = new ArrayList<>();
    for (final char tick : levitating.toCharArray()) {
      seen.add(edge.landed(tick == 't') ? "L" : "-");
    }
    assertEquals(expected, String.join("", seen));
  }

  @ParameterizedTest(name = "land pose {0}, finished {1}: still landing {2}")
  @CsvSource({
    "true, false, true",
    "true, true, false",
    "false, false, false",
    "false, true, false"
  })
  @DisplayName(
      "the landing holds while its clip plays and clears once it ends or another pose wins")
  void latch(final boolean landPose, final boolean finished, final boolean stillLanding) {
    final LandingEdge edge = new LandingEdge();
    edge.landed(true);
    edge.landed(false);
    assertEquals(true, edge.landing());
    edge.shown(landPose, finished);
    assertEquals(stillLanding, edge.landing());
  }
}
