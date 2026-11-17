package com.grahambartley.nightborn.name;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NamingRightsTest {

  private static final UUID OWNER = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID STRANGER = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @ParameterizedTest(name = "owned={0} by sender={1} alive={2}: {3}")
  @CsvSource({
    "true, true, true, true",
    "true, false, true, false",
    "false, true, true, false",
    "true, true, false, false"
  })
  @DisplayName("only the owner may name their own living familiar or thrall")
  void mayName(
      final boolean owned, final boolean ownerSends, final boolean alive, final boolean allowed) {
    assertEquals(
        allowed,
        NamingRights.mayName(
            owned ? Optional.of(OWNER) : Optional.empty(), ownerSends ? OWNER : STRANGER, alive));
  }
}
