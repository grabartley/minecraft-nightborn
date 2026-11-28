package com.grahambartley.nightborn.balance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SideTest {

  @ParameterizedTest(name = "the other side of {0} is {1}")
  @CsvSource({"VAMPIRES, HUNTERS", "HUNTERS, VAMPIRES"})
  void other(final Side side, final Side other) {
    assertEquals(other, side.other());
  }
}
