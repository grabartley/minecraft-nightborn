package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CampHomeTest {

  @ParameterizedTest(name = "range {0}, distance {1}, fighting reach {2}, in a fight {3}: {4}")
  @CsvSource({
    "3, 2, 12, false, true",
    "3, 4, 12, false, false",
    "3, 4, 12, true, true",
    "3, 11.9, 12, true, true",
    "3, 12, 12, true, false",
    "8, 7, 12, false, true",
    "8, 11, 12, true, true",
    "16, 15, 12, true, true",
    "16, 15, 12, false, true",
    "16, 17, 12, true, false",
    "-1, 500, 12, false, true",
    "-1, 500, 12, true, true"
  })
  @DisplayName("a resident ranges out to its fighting reach only in a fight, never below its own")
  void rangesFurtherOnlyInAFight(
      final float range,
      final double distance,
      final int fighting,
      final boolean inFight,
      final boolean reaches) {
    assertEquals(reaches, CampHome.reaches(range, distance * distance, fighting, inFight));
  }
}
