package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ServantsTest {

  private static final UUID A = UUID.fromString("00000000-0000-0000-0000-00000000000a");
  private static final UUID B = UUID.fromString("00000000-0000-0000-0000-00000000000b");

  private static Optional<UUID> of(final String name) {
    return switch (name) {
      case "A" -> Optional.of(A);
      case "B" -> Optional.of(B);
      default -> Optional.empty();
    };
  }

  @ParameterizedTest(name = "master {0}, candidate A -> {1}")
  @CsvSource({"A, true", "B, false", "none, false"})
  @DisplayName("a servant serves only its own master")
  void serves(final String master, final boolean expected) {
    assertEquals(expected, Servants.serves(of(master), A));
  }

  @ParameterizedTest(name = "{0} and {1} -> {2}")
  @CsvSource({"A, A, true", "A, B, false", "A, none, false", "none, none, false"})
  @DisplayName("two servants share a master only when both have the same one")
  void sameMaster(final String one, final String other, final boolean expected) {
    assertEquals(expected, Servants.sameMaster(of(one), of(other)));
  }
}
