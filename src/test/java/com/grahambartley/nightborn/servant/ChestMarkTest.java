package com.grahambartley.nightborn.servant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ChestMarkTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "FETCH, nightborn.servant.mark.fetch",
    "DELIVER, nightborn.servant.mark.deliver",
    "CLEARED, nightborn.servant.mark.cleared"
  })
  @DisplayName("each mark tells the owner what their servants will do there")
  void key(final ChestMark mark, final String key) {
    assertEquals(key, mark.translationKey());
  }
}
