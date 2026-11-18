package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LineShareTest {

  @ParameterizedTest(name = "{0} of {1} -> {2}%")
  @CsvSource({"0, 5, 0", "1, 5, 20", "3, 6, 50", "5, 5, 100", "6, 5, 100", "0, 0, 0", "1, 0, 0"})
  void sharesAreWholePercentsOfTheLine(final int owned, final int total, final int expected) {
    assertEquals(expected, LineShare.percent(owned, total));
  }
}
