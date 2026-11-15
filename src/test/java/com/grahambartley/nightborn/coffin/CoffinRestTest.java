package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CoffinRestTest {

  private static final UUID SLEEPER = new UUID(0, 1);
  private static final UUID OTHER = new UUID(0, 2);

  static Stream<Arguments> rests() {
    return Stream.of(
        Arguments.of("an unassigned coffin", Optional.empty(), CoffinRest.CLAIM, true),
        Arguments.of("the sleeper's own coffin", Optional.of(SLEEPER), CoffinRest.RETURN, true),
        Arguments.of("another vampire's coffin", Optional.of(OTHER), CoffinRest.BORROW, false));
  }

  @ParameterizedTest(name = "{0}: {2}, sets respawn {3}")
  @MethodSource("rests")
  @DisplayName("resting claims a free coffin, returns to your own, and only borrows another's")
  void decidesWhatRestingDoes(
      final String name,
      final Optional<UUID> owner,
      final CoffinRest rest,
      final boolean setsRespawn) {
    assertEquals(rest, CoffinRest.of(owner, SLEEPER));
    assertEquals(setsRespawn, rest.setsRespawn());
  }
}
