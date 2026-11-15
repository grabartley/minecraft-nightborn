package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DrapeColumnTest {

  static Stream<Arguments> columns() {
    return Stream.of(
        Arguments.of(Set.of(), List.of(0)),
        Arguments.of(Set.of(1, 2, -1), List.of(0, 1, 2, -1)),
        Arguments.of(Set.of(2, -2), List.of(0)),
        Arguments.of(
            Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, -1), List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1)));
  }

  @ParameterizedTest(name = "drapes at {0} join into {1}")
  @MethodSource("columns")
  void gathersTheWholeUnbrokenColumnUpThenDown(
      final Set<Integer> drapesAt, final List<Integer> expected) {
    assertEquals(expected, DrapeColumn.offsets(drapesAt::contains));
  }
}
