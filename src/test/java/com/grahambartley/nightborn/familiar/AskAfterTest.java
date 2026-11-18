package com.grahambartley.nightborn.familiar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AskAfterTest {

  @ParameterizedTest(name = "{0}, {1}")
  @CsvSource({"nightborn:ask_after, nightborn:wheel/ask_after"})
  @DisplayName("asking after a familiar is its own wheel action")
  void names(final String id, final String icon) {
    assertEquals(id, new AskAfter().id().toString());
    assertEquals(icon, AskAfter.ICON.toString());
  }
}
