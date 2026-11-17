package com.grahambartley.nightborn.vanish;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VanishAttachmentTest {

  @ParameterizedTest(name = "now {0}, until {1}: {2}")
  @CsvSource(
      value = {
        "100, 105, false",
        "104, 105, false",
        "105, 105, true",
        "200, 105, true",
        "100, , false"
      })
  @DisplayName(
      "a vanished vampire comes back once its time is up, and one never gone has nothing due")
  void due(final long now, final Long until, final boolean due) {
    assertEquals(due, VanishAttachment.due(now, until));
  }
}
