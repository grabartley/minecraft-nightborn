package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LiningLayerTest {

  @ParameterizedTest(name = "lined {0}: {1}")
  @CsvSource(
      delimiter = ';',
      value = {"true; MAIL_LINING", "false; "})
  @DisplayName("worn clothing shows the mail lining only once it is lined")
  void showsMailOnceLined(final boolean lined, final String sheens) {
    assertEquals(
        sheens == null ? List.of() : Arrays.stream(sheens.split(" ")).map(Sheen::valueOf).toList(),
        LiningLayer.sheens(lined));
  }
}
