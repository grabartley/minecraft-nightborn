package com.grahambartley.nightborn.familiar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HireRulesTest {

  @ParameterizedTest(name = "vampire {0}, adult villager {1}, keeps one {2}: {3}")
  @CsvSource({
    "true, true, false, HIRE",
    "true, true, true, ALREADY_KEEPS_ONE",
    "true, false, false, NOT_HIREABLE",
    "true, false, true, NOT_HIREABLE",
    "false, true, false, NOT_A_VAMPIRE",
    "false, false, true, NOT_A_VAMPIRE"
  })
  @DisplayName("only a vampire without a familiar can hire a grown villager")
  void decide(
      final boolean vampire,
      final boolean adultVillager,
      final boolean keeps,
      final HireRules.Outcome outcome) {
    assertEquals(outcome, HireRules.decide(vampire, adultVillager, keeps));
  }
}
