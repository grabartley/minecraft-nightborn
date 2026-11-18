package com.grahambartley.nightborn.rank;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RankDeedsTest {

  @ParameterizedTest(name = "household cleared {0} -> {1}")
  @CsvSource({"false, STAKE", "true, 'STAKE, CLEAR_HOUSEHOLD'"})
  @DisplayName("every stake counts, and the stake that ends a household clears it too")
  void ofStake(final boolean cleared, final String acts) {
    assertEquals(
        List.of(acts.split(", ")).stream().map(RankDeed.Act::valueOf).toList(),
        RankDeeds.ofStake(cleared));
  }
}
