package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GuildRestockTest {

  @ParameterizedTest(name = "time {0} is day {1}")
  @CsvSource({"0, 0", "23999, 0", "24000, 1", "100000, 4"})
  void countsWholeDays(final long time, final long day) {
    assertEquals(day, GuildRestock.day(time));
  }

  @ParameterizedTest(name = "last {0}, today {1}: due {2}")
  @CsvSource({"0, 0, false", "0, 1, true", "3, 7, true", "5, 5, false"})
  void restocksOnceANewDayBegins(final long last, final long today, final boolean due) {
    assertEquals(due, GuildRestock.due(last, today));
  }
}
