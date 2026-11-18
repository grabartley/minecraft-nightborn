package com.grahambartley.nightborn.servant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MarkRulesTest {

  @ParameterizedTest(name = "errand chest {0}, owner may use {1}, unlocked {2}: {3}")
  @CsvSource({
    "true, true, true, true",
    "false, true, true, false",
    "true, false, true, false",
    "true, true, false, false",
    "false, false, false, false"
  })
  @DisplayName("an owner marks only an errand chest they could open themselves")
  void mayMark(
      final boolean container, final boolean mayUse, final boolean unlocked, final boolean marks) {
    assertEquals(marks, MarkRules.mayMark(container, mayUse, unlocked));
  }
}
