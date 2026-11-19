package com.grahambartley.nightborn.client.sight;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class UnseenViewTest {

  @ParameterizedTest(name = "body hidden {0}, revealed {1}, outlinable {2} -> skipped {3}")
  @CsvSource({
    "false, false, false, false",
    "false, true, true, false",
    "true, false, true, true",
    "true, true, true, false",
    "true, true, false, true",
    "true, false, false, true"
  })
  @DisplayName(
      "a hidden body is skipped unless a hunter's sight reveals it and it can be drawn as an"
          + " outline only")
  void skips(
      final boolean hidden,
      final boolean revealed,
      final boolean outlinable,
      final boolean skipped) {
    assertEquals(skipped, UnseenView.skips(hidden, revealed, outlinable));
  }

  @ParameterizedTest(name = "vanished {0}, in a shut coffin {1} -> body hidden {2}")
  @CsvSource({"false, false, false", "true, false, true", "false, true, true", "true, true, true"})
  @DisplayName("a vanished vampire or one under a shut lid shows no body")
  void bodyHidden(final boolean gone, final boolean sheltered, final boolean hidden) {
    assertEquals(hidden, UnseenView.bodyHidden(gone, sheltered));
  }
}
