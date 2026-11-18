package com.grahambartley.nightborn.nemesis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.nemesis.NemesisLines.Moment;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NemesisLinesTest {

  static final NemesisLines LINES = new NemesisLines(List.of("s1", "s2"), List.of("d1"), List.of());

  @ParameterizedTest(name = "{0}, roll {1}: {2}")
  @CsvSource({
    "SIGHTED, 0, s1",
    "SIGHTED, 1, s2",
    "SIGHTED, 3, s2",
    "SIGHTED, -1, s2",
    "DOWNED, 5, d1",
    "FLED, 0, ''"
  })
  @DisplayName("each moment draws one of its own lines, and says nothing when it has none")
  void picks(final Moment moment, final int roll, final String line) {
    assertEquals(
        line.isEmpty() ? Optional.empty() : Optional.of(line), LINES.pick(moment, size -> roll));
  }
}
