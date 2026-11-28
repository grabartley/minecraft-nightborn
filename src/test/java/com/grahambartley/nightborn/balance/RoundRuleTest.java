package com.grahambartley.nightborn.balance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RoundRuleTest {

  @ParameterizedTest(name = "{0} vampires and {1} hunters standing at tick {2}: {3}")
  @CsvSource({
    "3, 2, 10, ONGOING",
    "0, 2, 10, HUNTERS",
    "2, 0, 10, VAMPIRES",
    "0, 0, 10, DRAW",
    "1, 1, 3599, ONGOING",
    "1, 1, 3600, DRAW",
    "0, 1, 3600, HUNTERS"
  })
  void judge(final int vampires, final int hunters, final int ticks, final RoundRule.Verdict want) {
    assertEquals(want, RoundRule.judge(vampires, hunters, ticks));
  }

  @ParameterizedTest(name = "{0} is over {1} and won by {2}")
  @CsvSource({
    "ONGOING, false, ''",
    "VAMPIRES, true, VAMPIRES",
    "HUNTERS, true, HUNTERS",
    "DRAW, true, ''"
  })
  void verdict(final RoundRule.Verdict verdict, final boolean over, final String winner) {
    assertEquals(over, verdict.over());
    assertEquals(winner, verdict.winner().map(Side::name).orElse(""));
  }
}
