package com.grahambartley.nightborn.client.sight;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class UnseenViewTest {

  @ParameterizedTest(name = "vanished {0}, in a shut coffin {1}, revealed {2} -> hidden {3}")
  @CsvSource({
    "false, false, false, false",
    "true, false, false, true",
    "false, true, false, true",
    "true, false, true, false",
    "false, true, true, false",
    "false, false, true, false"
  })
  @DisplayName(
      "a vanished vampire or one asleep under a shut lid is hidden, unless a hunter's sight"
          + " reveals it")
  void hides(
      final boolean gone, final boolean sheltered, final boolean revealed, final boolean hidden) {
    assertEquals(hidden, UnseenView.hides(gone, sheltered, revealed));
  }

  @ParameterizedTest(name = "vanished {0}, in a shut coffin {1} -> body hidden {2}")
  @CsvSource({"false, false, false", "true, false, true", "false, true, true", "true, true, true"})
  @DisplayName("a vanished vampire or one under a shut lid shows no body, only an outline")
  void bodyHidden(final boolean gone, final boolean sheltered, final boolean hidden) {
    assertEquals(hidden, UnseenView.bodyHidden(gone, sheltered));
  }
}
