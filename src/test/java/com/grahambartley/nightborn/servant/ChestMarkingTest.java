package com.grahambartley.nightborn.servant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ChestMarkingTest {

  private static final UUID THRALL_OWNER = UUID.fromString("00000000-0000-0000-0000-00000000000a");
  private static final UUID FAMILIAR_OWNER =
      UUID.fromString("00000000-0000-0000-0000-00000000000b");

  @BeforeAll
  static void rosters() {
    ChestMarking.countServants((server, owner) -> owner.equals(THRALL_OWNER));
    ChestMarking.countServants((server, owner) -> owner.equals(FAMILIAR_OWNER));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({
    "00000000-0000-0000-0000-00000000000a, true",
    "00000000-0000-0000-0000-00000000000b, true",
    "00000000-0000-0000-0000-00000000000c, false"
  })
  @DisplayName("anyone who leads a thrall or a familiar may mark chests, and nobody else")
  void leadsServants(final UUID owner, final boolean leads) {
    assertEquals(leads, ChestMarking.leadsServants(null, owner));
  }
}
