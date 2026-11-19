package com.grahambartley.nightborn.faith;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WieldersTest {

  @ParameterizedTest(name = "ally {0}, self {1} -> {2}")
  @CsvSource({"ally, self, ally", ", self, self", "ally, , ally", ", , "})
  @DisplayName("a blessing goes to an armed ally in sight first, then to the hunter's own weapon")
  void pick(final String ally, final String self, final String expected) {
    assertEquals(Optional.ofNullable(expected), Wielders.pick(ally, self));
  }

  @ParameterizedTest(name = "mob caster {0}, alive {1}, seen {2}, allied {3} -> {4}")
  @CsvSource({
    "false, true, true, true, true",
    "true, true, true, true, false",
    "false, false, true, true, false",
    "false, true, false, true, false",
    "false, true, true, false, false"
  })
  @DisplayName("only a living ally a player hunter can see is blessed in their place")
  void blessable(
      final boolean mob,
      final boolean alive,
      final boolean seen,
      final boolean allied,
      final boolean expected) {
    assertEquals(expected, Wielders.blessable(mob, alive, seen, allied));
  }
}
