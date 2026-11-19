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
}
