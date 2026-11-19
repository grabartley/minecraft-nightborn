package com.grahambartley.nightborn.trap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FieldRulesTest {

  @ParameterizedTest(name = "listed {0}, inside {1}, reaches {2} -> {3}")
  @CsvSource({
    "true, true, true, true",
    "false, true, true, false",
    "true, false, true, false",
    "true, true, false, false"
  })
  @DisplayName("a killing ground bars only its listed powers, for one inside it may reach")
  void bars(
      final boolean listed, final boolean inside, final boolean reaches, final boolean barred) {
    assertEquals(barred, FieldRules.bars(listed, inside, reaches));
  }
}
