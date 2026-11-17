package com.grahambartley.nightborn.servant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ServantDeathsTest {

  @ParameterizedTest(name = "death messages={0} owner online={1}: {2}")
  @CsvSource({
    "true, true, true",
    "true, false, false",
    "false, true, false",
    "false, false, false"
  })
  @DisplayName(
      "an owner hears of a servant's death as of a pet's, only while death messages are on")
  void tells(final boolean showDeathMessages, final boolean ownerOnline, final boolean told) {
    assertEquals(told, ServantDeaths.tells(showDeathMessages, ownerOnline));
  }
}
