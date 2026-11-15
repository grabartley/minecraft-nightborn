package com.grahambartley.nightborn.feeding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BottleStowingTest {

  @ParameterizedTest(name = "selected {0}, filled {1}, joinable {2}: slot {3}")
  @CsvSource(
      delimiter = '|',
      value = {
        "0 | '' | '' | 1",
        "3 | '' | '' | 0",
        "0 | 1,2 | '' | 3",
        "0 | 1,5 | 5 | 5",
        "5 | 1,5 | 5 | 0",
        "0 | 1,2,3 | '' | 4"
      })
  @DisplayName(
      "a filled bottle joins a stack with room, else the first free slot, never the feeding hand")
  void picksASlot(
      final int selected, final String filled, final String joinable, final int expected) {
    final Set<Integer> full = parse(filled);
    final Set<Integer> joins = parse(joinable);
    assertEquals(
        expected,
        BottleStowing.slotFor(9, selected, slot -> !full.contains(slot), joins::contains));
  }

  @ParameterizedTest(name = "selected {0}")
  @CsvSource({"0", "4"})
  @DisplayName("with every other slot full and nothing to join, the bottle has nowhere to go")
  void nowhere(final int selected) {
    assertEquals(-1, BottleStowing.slotFor(9, selected, slot -> false, slot -> false));
  }

  private static Set<Integer> parse(final String text) {
    return text.isBlank()
        ? Set.of()
        : Set.of(text.split(",")).stream()
            .map(String::trim)
            .map(Integer::valueOf)
            .collect(Collectors.toSet());
  }
}
