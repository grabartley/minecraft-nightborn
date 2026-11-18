package com.grahambartley.nightborn.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NotorietyCommandTest {

  @ParameterizedTest(name = "{0} shows as {1}")
  @CsvSource({"0, 0", "1, 1", "31.25, 31.25", "7.5, 7.5", "2.333333, 2.33", "12.005, 12.01"})
  @DisplayName("operators read notoriety to two places, with no trailing zeros")
  void shows(final double notoriety, final String shown) {
    assertEquals(shown, NotorietyCommand.shown(notoriety));
  }
}
