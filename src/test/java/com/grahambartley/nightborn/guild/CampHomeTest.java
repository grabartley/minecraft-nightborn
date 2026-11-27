package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CampHomeTest {

  @ParameterizedTest(name = "home {0}, fighting {1}, in a fight {2}: reach {3}")
  @CsvSource({
    "3, 12, false, 3",
    "3, 12, true, 12",
    "12, 12, true, 12",
    "12, 3, true, 12",
    "12, 3, false, 12"
  })
  @DisplayName("a resident ranges as far as its fighting reach only while it fights")
  void rangesFurtherOnlyInAFight(
      final int home, final int fighting, final boolean inFight, final int reach) {
    assertEquals(reach, CampHome.reach(home, fighting, inFight));
  }
}
