package com.grahambartley.nightborn.ward;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.LongStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class WardCadenceTest {

  @ParameterizedTest(name = "time {0}, entity {1}, every {2}: {3}")
  @CsvSource({
    "0, 0, 10, true",
    "10, 0, 10, true",
    "5, 0, 10, false",
    "7, 3, 10, true",
    "8, 3, 10, false",
    "3, -3, 10, true",
    "5, 7, 1, true",
    "5, 7, 0, true"
  })
  @DisplayName("each entity scans on its own beat")
  void due(final long time, final int entity, final int interval, final boolean expected) {
    assertEquals(expected, WardCadence.due(time, entity, interval));
  }

  @ParameterizedTest(name = "entity {0}")
  @ValueSource(ints = {0, 1, 9, 12345, -7})
  @DisplayName("an entity scans exactly once in every interval")
  void oncePerInterval(final int entity) {
    assertEquals(
        1, LongStream.range(100, 110).filter(time -> WardCadence.due(time, entity, 10)).count());
  }
}
