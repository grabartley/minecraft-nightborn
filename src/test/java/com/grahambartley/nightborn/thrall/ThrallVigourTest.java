package com.grahambartley.nightborn.thrall;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ThrallVigourTest {

  @ParameterizedTest(name = "factor {0}: +{1}")
  @CsvSource({"1.0, 0.0", "1.2, 0.2", "0.8, 0.0"})
  @DisplayName("a thrall gains its master's share of extra health, never less than its own")
  void bonus(final double factor, final double bonus) {
    assertEquals(bonus, ThrallVigour.bonus(factor), 1.0E-9);
  }
}
