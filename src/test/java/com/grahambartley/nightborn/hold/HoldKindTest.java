package com.grahambartley.nightborn.hold;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HoldKindTest {

  @ParameterizedTest(name = "{0}: stops attacks {1}")
  @CsvSource({"PIN, false", "STAGGER, true"})
  @DisplayName("only a stagger stops attacks")
  void attacks(final HoldKind kind, final boolean stops) {
    assertEquals(stops, kind.stopsAttacks());
  }

  @ParameterizedTest(name = "{0}, power moves {1}: stopped {2}")
  @CsvSource({
    "PIN, true, true",
    "PIN, false, false",
    "STAGGER, false, true",
    "STAGGER, true, true"
  })
  @DisplayName("a pin stops only powers that move, a stagger stops them all")
  void powers(final HoldKind kind, final boolean moves, final boolean stopped) {
    assertEquals(stopped, kind.stopsPower(moves));
  }
}
