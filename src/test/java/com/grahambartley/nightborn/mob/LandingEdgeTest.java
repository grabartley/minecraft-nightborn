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
}
