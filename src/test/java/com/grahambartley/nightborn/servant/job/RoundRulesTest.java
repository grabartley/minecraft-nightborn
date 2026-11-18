package com.grahambartley.nightborn.servant.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RoundRulesTest {

  @ParameterizedTest(name = "delivered {0}, ready {1}, nothing left {2} -> {3}")
  @CsvSource({
    "true, true, false, WORK",
    "false, true, false, END",
    "true, false, false, END",
    "true, true, true, END",
    "false, false, true, END"
  })
  @DisplayName(
      "after the chest a servant works only if it emptied its pack, has its tools and there was"
          + " work left")
  void afterChest(
      final boolean delivered,
      final boolean ready,
      final boolean nothingLeft,
      final RoundRules.Leg leg) {
    assertEquals(leg, RoundRules.afterChest(delivered, ready, nothingLeft));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"WORKING, WORK", "BACK, CHEST", "NOTHING_LEFT, CHEST"})
  @DisplayName("a servant keeps working until it is full, out of tools or out of work")
  void afterStep(final RoundRules.Step step, final RoundRules.Leg leg) {
    assertEquals(leg, RoundRules.afterStep(step));
  }

  @ParameterizedTest(name = "{0} ticks -> stuck {1}")
  @CsvSource({"0, false", "200, false", "201, true"})
  @DisplayName("a servant gives up on a target or a chest it cannot reach")
  void stuck(final int ticks, final boolean stuck) {
    assertEquals(stuck, RoundRules.stuck(ticks));
  }

  @ParameterizedTest(name = "idle {0} -> {1} ticks")
  @CsvSource({"false, 100", "true, 1200"})
  @DisplayName("a servant with nothing to do rests far longer before it looks again")
  void restTicks(final boolean idle, final int ticks) {
    assertEquals(ticks, RoundRules.restTicks(idle));
  }
}
