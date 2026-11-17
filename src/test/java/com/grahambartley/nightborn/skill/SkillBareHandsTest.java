package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SkillBareHandsTest {

  private static SkillRank rank(final double bareHands) {
    return new SkillRank(1, List.of(), bareHands);
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"'', 0", "'0', 0", "'2', 2", "'2,1.5', 3.5", "'0,2,0', 2"})
  @DisplayName("the bare hand bonus is the sum over every rank held of every owned skill")
  void total(final String bonuses, final double expected) {
    final List<SkillRank> ranks =
        bonuses.isEmpty()
            ? List.of()
            : Arrays.stream(bonuses.split(","))
                .map(value -> rank(Double.parseDouble(value)))
                .toList();
    assertEquals(expected, SkillBareHands.total(ranks), 0.0001);
  }
}
