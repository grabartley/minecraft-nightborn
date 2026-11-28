package com.grahambartley.nightborn.balance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MatchupTest {

  @ParameterizedTest(name = "row {0} is {1} with at least {2} seconds a fight")
  @CsvSource({
    "0, fledgling-initiate-1, 20",
    "1, fledgling-initiate-3, 30",
    "2, fledgling-initiate-5, 40",
    "11, ancient-master-5, 40",
    "12, vampire_lord-veteran_hunter-1, 20",
    "13, vampire_lord-guild_master-1, 20",
    "14, mixed-mixed-5, 40"
  })
  void rows(final int index, final String key, final int seconds) {
    assertEquals(15, Matchup.ALL.size());
    assertEquals(key, Matchup.ALL.get(index).key());
    assertEquals(seconds, Matchup.ALL.get(index).minimumSeconds());
  }

  @ParameterizedTest(name = "choosing ''{0}'' runs {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "'';15",
        "adult-hunter-3;adult-hunter-3",
        "mixed-mixed-5,elder-veteran-1;elder-veteran-1,mixed-mixed-5",
        "no-such-row;''"
      })
  void chosen(final String keys, final String expected) {
    final List<Matchup> chosen = Matchup.chosen(keys);
    if (expected.matches("\\d+")) {
      assertEquals(Integer.parseInt(expected), chosen.size());
    } else {
      assertEquals(expected, String.join(",", chosen.stream().map(Matchup::key).toList()));
    }
  }
}
