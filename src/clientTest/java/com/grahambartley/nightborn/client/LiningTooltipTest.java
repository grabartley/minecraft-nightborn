package com.grahambartley.nightborn.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LiningTooltipTest {

  @ParameterizedTest(name = "lined {0}: line {1}")
  @CsvSource({"true, nightborn.lining.tooltip", "false, "})
  @DisplayName("only a lined piece of clothing says it is lined")
  void line(final boolean lined, final String key) {
    assertEquals(Optional.ofNullable(key), LiningTooltip.line(lined));
  }
}
