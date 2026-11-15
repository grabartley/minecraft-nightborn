package com.grahambartley.nightborn.familiar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ChestMarkTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "FETCH, nightborn.familiar.mark.fetch",
    "DELIVER, nightborn.familiar.mark.deliver",
    "CLEARED, nightborn.familiar.mark.cleared"
  })
  @DisplayName("each mark tells the owner what the familiar will do there")
  void key(final ChestMark mark, final String key) {
    assertEquals(key, mark.translationKey());
  }
}
