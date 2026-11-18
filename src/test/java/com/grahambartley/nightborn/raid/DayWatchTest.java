package com.grahambartley.nightborn.raid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.LongStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DayWatchTest {

  private static final Set<UUID> LIVING =
      Set.of(new UUID(1L, 1L), new UUID(2L, 5L), new UUID(9L, 3L));

  @ParameterizedTest(name = "every {0} days over {1} days: {2} watch days")
  @CsvSource({"3, 30, 10", "3, 3, 1", "1, 7, 7", "4, 40, 10", "0, 30, 0"})
  @DisplayName("a house keeps a watch on one day in the set number, and never when it is off")
  void countsWatchDays(final int every, final int days, final long expected) {
    for (final long house : new long[] {0L, 17L, -9_000_000_000L}) {
      assertEquals(
          expected,
          LongStream.range(0, days).filter(day -> DayWatch.watchDay(day, house, every)).count());
    }
  }

  @ParameterizedTest(name = "houses {0} and {1}")
  @CsvSource({"0, 1", "0, 2", "5, 7"})
  @DisplayName("two houses do not keep watch on the same days")
  void housesDiffer(final long first, final long second) {
    assertNotEquals(
        LongStream.range(0, 3).mapToObj(day -> DayWatch.watchDay(day, first, 3)).toList(),
        LongStream.range(0, 3).mapToObj(day -> DayWatch.watchDay(day, second, 3)).toList());
  }

  @ParameterizedTest(name = "day {0}")
  @CsvSource({"0", "4", "-3"})
  @DisplayName("an empty house has nobody to keep watch")
  void emptyHouse(final long day) {
    assertEquals(Optional.empty(), DayWatch.watcher(Set.of(), day));
  }

  @ParameterizedTest(name = "day {0}")
  @CsvSource({"0", "1", "2", "57"})
  @DisplayName("exactly one member keeps watch, the same one whoever asks, from the household")
  void picksOne(final long day) {
    final Optional<UUID> watcher = DayWatch.watcher(LIVING, day);
    assertTrue(watcher.isPresent() && LIVING.contains(watcher.get()));
    assertEquals(watcher, DayWatch.watcher(List.copyOf(LIVING), day));
  }

  @ParameterizedTest(name = "over {0} days: {1} different watchers")
  @CsvSource({"60, 3", "1, 1"})
  @DisplayName("the watch passes between members over the days")
  void rotates(final int days, final long distinct) {
    assertEquals(
        distinct,
        LongStream.range(0, days)
            .mapToObj(day -> DayWatch.watcher(LIVING, day))
            .distinct()
            .count());
  }
}
