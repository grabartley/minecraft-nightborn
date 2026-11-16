package com.grahambartley.nightborn.familiar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HireRulesTest {

  @ParameterizedTest(name = "vampire {0}, grown villager {1}, trading {2}, keeps one {3}: {4}")
  @CsvSource({
    "true, true, false, false, HIRE",
    "true, true, false, true, ALREADY_KEEPS_ONE",
    "true, true, true, false, BUSY",
    "true, true, true, true, BUSY",
    "true, false, false, false, NOT_HIREABLE",
    "true, false, true, true, NOT_HIREABLE",
    "false, true, false, false, NOT_A_VAMPIRE",
    "false, false, true, true, NOT_A_VAMPIRE"
  })
  @DisplayName("only a vampire without a familiar can hire a grown villager")
  void decide(
      final boolean vampire,
      final boolean grownVillager,
      final boolean trading,
      final boolean keeps,
      final HireRules.Outcome outcome) {
    assertEquals(outcome, HireRules.decide(vampire, grownVillager, trading, keeps));
  }
}
