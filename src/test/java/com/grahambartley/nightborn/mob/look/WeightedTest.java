package com.grahambartley.nightborn.mob.look;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WeightedTest {

  record Entry(String name, int weight) {}

  private static final List<Entry> ENTRIES =
      List.of(new Entry("a", 1), new Entry("b", 0), new Entry("c", 3));

  @ParameterizedTest(name = "roll {0} picks {1}")
  @CsvSource({"0.0, a", "0.24, a", "0.25, c", "0.99, c", "1.0, c", "-1.0, a", "5.0, c"})
  @DisplayName("each entry is picked in proportion to its weight, and a zero weight never is")
  void picks(final double roll, final String name) {
    assertEquals(name, Weighted.pick(ENTRIES, Entry::weight, roll).orElseThrow().name());
  }

  @Test
  @DisplayName("nothing is picked from nothing or from all zero weights")
  void empty() {
    assertTrue(Weighted.pick(List.<Entry>of(), Entry::weight, 0.5).isEmpty());
    assertTrue(Weighted.pick(List.of(new Entry("z", 0)), Entry::weight, 0.5).isEmpty());
  }
}
