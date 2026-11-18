package com.grahambartley.nightborn.raid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.LongStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DayWatchTest {

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

  @Test
  @DisplayName("two houses do not keep watch on the same days")
  void housesDiffer() {
    final List<Boolean> first =
        LongStream.range(0, 3).mapToObj(day -> DayWatch.watchDay(day, 0L, 3)).toList();
    final List<Boolean> second =
        LongStream.range(0, 3).mapToObj(day -> DayWatch.watchDay(day, 1L, 3)).toList();
    assertTrue(!first.equals(second));
  }

  @Test
  @DisplayName("an empty house has nobody to keep watch")
  void emptyHouse() {
    assertEquals(Optional.empty(), DayWatch.watcher(Set.of(), 4L));
  }

  @ParameterizedTest(name = "day {0}")
  @CsvSource({"0", "1", "2", "57"})
  @DisplayName("exactly one member keeps watch, the same one whoever asks, from the household")
  void picksOne(final long day) {
    final Set<UUID> living = Set.of(new UUID(1L, 1L), new UUID(2L, 5L), new UUID(9L, 3L));
    final Optional<UUID> watcher = DayWatch.watcher(living, day);
    assertTrue(watcher.isPresent() && living.contains(watcher.get()));
    assertEquals(watcher, DayWatch.watcher(List.copyOf(living), day));
  }

  @Test
  @DisplayName("the watch passes between members over the days")
  void rotates() {
    final Set<UUID> living = Set.of(new UUID(1L, 1L), new UUID(2L, 5L), new UUID(9L, 3L));
    final long distinct =
        LongStream.range(0, 60).mapToObj(day -> DayWatch.watcher(living, day)).distinct().count();
    assertEquals(3, distinct);
  }
}
