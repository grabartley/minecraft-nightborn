package com.grahambartley.nightborn.rank;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.faction.Oath;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RankOutlookTest {

  @ParameterizedTest(name = "rank {0}, earned {1}, deeds {2} -> next {3}, {4} left, deed {5}")
  @CsvSource({
    "0, 0, 0, hunter, 5, false",
    "0, 7, 2, hunter, 0, true",
    "1, 7, 2, veteran, 13, false",
    "1, 30, 4, veteran, 0, true",
    "2, 44, 0, master, 1, false",
    "3, 99, 14, '', 0, false"
  })
  @DisplayName(
      "a hunter is told the next rank, the points still needed and whether its deed is done")
  void next(
      final int rank,
      final int earned,
      final int deeds,
      final String next,
      final int left,
      final boolean deedDone) {
    final Optional<RankOutlook> outlook =
        RankOutlook.of(
            new Oath(true, true, rank, deeds), earned, RankFixtures.LADDER, RankFixtures.NEEDS);
    assertEquals(next.isEmpty(), outlook.isEmpty());
    outlook.ifPresent(
        shown -> {
          assertEquals(next, shown.next().id().getPath());
          assertEquals(left, shown.pointsLeft());
          assertEquals(deedDone, shown.deedDone());
          assertEquals(shown.next().rank().deed(), shown.deed());
        });
  }
}
