package com.grahambartley.nightborn.name;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NameDrawTest {

  private static final List<String> POOL = List.of("Aurelian", "Isolde", "Mircea");

  @ParameterizedTest(name = "roll {0} with {1} taken gives {2}")
  @CsvSource({
    "0, '', Aurelian",
    "2, '', Mircea",
    "0, Aurelian, Isolde",
    "1, Aurelian, Mircea",
    "0, Aurelian;Isolde, Mircea"
  })
  @DisplayName("a pick skips names already taken nearby")
  void skipsTaken(final int roll, final String taken, final String expected) {
    final Set<String> held = taken.isEmpty() ? Set.of() : Set.of(taken.split(";"));
    assertEquals(Optional.of(expected), NameDraw.pick(POOL, held, bound -> roll));
  }

  @ParameterizedTest(name = "roll {0}")
  @CsvSource({"0, Aurelian", "1, Isolde", "2, Mircea"})
  @DisplayName("once every name is taken, a repeat is allowed rather than leaving a mob nameless")
  void repeatsWhenFull(final int roll, final String expected) {
    assertEquals(Optional.of(expected), NameDraw.pick(POOL, Set.copyOf(POOL), bound -> roll));
  }

  @Test
  @DisplayName("an empty pool gives no name")
  void emptyPool() {
    assertEquals(Optional.empty(), NameDraw.pick(List.of(), Set.of(), bound -> 0));
  }
}
