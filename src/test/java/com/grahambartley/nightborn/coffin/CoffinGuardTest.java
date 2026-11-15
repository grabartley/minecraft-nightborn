package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CoffinGuardTest {

  private static final UUID OWNER = new UUID(0, 1);
  private static final UUID STRANGER = new UUID(0, 2);

  static Stream<Arguments> breakers() {
    return Stream.of(
        Arguments.of(
            "anyone breaks an unassigned coffin", Optional.empty(), STRANGER, false, false, true),
        Arguments.of(
            "an explosion breaks an unassigned coffin", Optional.empty(), null, false, false, true),
        Arguments.of(
            "the owner breaks their coffin", Optional.of(OWNER), OWNER, false, false, true),
        Arguments.of("a stranger cannot", Optional.of(OWNER), STRANGER, false, false, false),
        Arguments.of(
            "an explosion or piston cannot", Optional.of(OWNER), null, false, false, false),
        Arguments.of("a creative player can", Optional.of(OWNER), STRANGER, true, false, true),
        Arguments.of("an operator can", Optional.of(OWNER), STRANGER, false, true, true));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("breakers")
  @DisplayName("only the owner, a creative player or an operator breaks an assigned coffin")
  void decidesWhoMayBreakACoffin(
      final String name,
      final Optional<UUID> owner,
      final UUID breaker,
      final boolean creative,
      final boolean operator,
      final boolean allowed) {
    assertEquals(allowed, CoffinGuard.mayBreak(owner, breaker, creative, operator));
  }
}
