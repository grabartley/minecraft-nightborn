package com.grahambartley.nightborn.ritual;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class AltarUseTest {

  @ParameterizedTest(
      name =
          "active {0}, set {1}, empty {2}, full {3}; hand empty {4}, chalice {5}, sneak {6};"
              + " vampire {7}, night {8} -> {9}")
  @CsvSource({
    "true, true, false, true, true, false, false, false, true, BUSY",
    "true, true, false, true, false, false, true, true, true, BUSY",
    "false, false, true, false, false, true, false, false, false, SET_CHALICE",
    "false, false, true, false, false, true, true, true, true, SET_CHALICE",
    "false, false, true, false, true, false, false, false, true, NO_CHALICE",
    "false, false, true, false, false, false, false, false, true, PASS",
    "false, true, true, false, false, false, false, false, true, POUR",
    "false, true, false, true, false, true, false, false, true, POUR",
    "false, true, false, true, true, false, true, false, true, TAKE_CHALICE",
    "false, true, true, false, true, false, true, true, false, TAKE_CHALICE",
    "false, true, false, true, true, false, false, false, true, START",
    "false, true, false, true, true, false, false, true, true, START",
    "false, true, false, true, true, false, false, true, false, POUR",
    "false, true, false, false, true, false, false, true, true, POUR",
    "false, true, true, false, true, false, false, true, true, NOT_FULL",
    "false, true, false, false, true, false, false, false, true, NOT_FULL",
    "false, true, false, true, true, false, false, false, false, NOT_NIGHT"
  })
  @DisplayName("the server decides what using the altar does")
  void decides(
      final boolean active,
      final boolean chaliceSet,
      final boolean empty,
      final boolean full,
      final boolean handEmpty,
      final boolean chalice,
      final boolean sneaking,
      final boolean vampire,
      final boolean night,
      final AltarUse expected) {
    assertEquals(
        expected,
        AltarUse.decide(
            new AltarUse.Altar(active, chaliceSet, empty, full),
            new AltarUse.Hand(handEmpty, chalice, sneaking),
            vampire,
            night));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(AltarUse.class)
  @DisplayName("only a refusal carries a message")
  void refusals(final AltarUse use) {
    assertEquals(
        use == AltarUse.NO_CHALICE || use == AltarUse.NOT_FULL || use == AltarUse.NOT_NIGHT,
        use.refusal().isPresent());
  }
}
